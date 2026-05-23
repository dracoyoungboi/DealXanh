package com.dealxanh.app.service;

import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.Transaction;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PayOSService {

    private static final Logger log = LoggerFactory.getLogger(PayOSService.class);

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    public PayOSService(OrderRepository orderRepository,
                        TransactionRepository transactionRepository,
                        ProductRepository productRepository) {
        this.restTemplate = new RestTemplate();
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    /**
     * Tạo payment link PayOS cho đơn hàng.
     * Trả về Map chứa checkoutUrl, qrCode, orderCode từ PayOS.
     */
    public Map<String, Object> createPaymentLink(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return Map.of("success", false, "message", "Không tìm thấy đơn hàng");

        int amount = (int) Math.round(order.getFinalAmount() != null ? order.getFinalAmount() : 0);
        if (amount <= 0) return Map.of("success", false, "message", "Số tiền không hợp lệ");

        String description = "Thanh toan don hang DX-" + orderId;
        // PayOS yêu cầu orderCode unique, dùng orderId + timestamp đuôi
        long orderCode = orderId * 1000 + (System.currentTimeMillis() / 1000) % 1000;
        String cancelUrl = "http://localhost:8084/buyer/orders/" + orderId;
        String returnUrl = "http://localhost:8084/buyer/orders/" + orderId;
        int expiredAt = (int) (System.currentTimeMillis() / 1000 + 30 * 60);

        log.info("PayOS createPaymentLink: orderCode={}, amount={}, checksumKey.len={}",
            orderCode, amount, checksumKey != null ? checksumKey.length() : 0);

        try {
            // Dùng LinkedHashMap giữ thứ tự insert ổn định
            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);
            body.put("expiredAt", expiredAt);
            // PayOS live có thể yêu cầu thêm thông tin người mua
            if (order.getUser() != null) {
                body.put("buyerName", order.getUser().getFullName() != null ? order.getUser().getFullName() : "Khach hang");
                body.put("buyerEmail", order.getUser().getEmail() != null ? order.getUser().getEmail() : "no-reply@dealxanh.vn");
            }

            // Tạo signature: sắp xếp key alphabetically, nối key=value bằng &
            String signature = createSignature(body);
            body.put("signature", signature);

            log.debug("PayOS request body: {}", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl + "/v2/payment-requests", request, Map.class);

            Map<String, Object> data = response.getBody();
            log.debug("PayOS response: {}", data);

            if (data != null && "00".equals(String.valueOf(data.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> respData = (Map<String, Object>) data.get("data");
                if (respData != null) {
                    log.info("PayOS response data keys: {}", respData.keySet());
                    log.info("PayOS qrCode value: {}", respData.get("qrCode"));
                    log.info("PayOS checkoutUrl value: {}", respData.get("checkoutUrl"));

                    order.setPickupQrCode(String.valueOf(orderCode));
                    orderRepository.save(order);

                    return Map.of(
                        "success", true,
                        "checkoutUrl", respData.getOrDefault("checkoutUrl", ""),
                        "qrCode", respData.getOrDefault("qrCode", ""),
                        "orderCode", orderCode,
                        "amount", amount,
                        "bin", respData.getOrDefault("bin", ""),
                        "accountNumber", respData.getOrDefault("accountNumber", ""),
                        "accountName", respData.getOrDefault("accountName", "")
                    );
                }
            }
            return Map.of("success", false, "message",
                "PayOS error: " + (data != null ? data.get("desc") : "No response"));
        } catch (Exception e) {
            log.error("PayOS createPaymentLink error: {}", e.getMessage());
            return Map.of("success", false, "message", "Lỗi kết nối PayOS: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán từ PayOS API.
     * Dùng khi chạy localhost không nhận được webhook.
     */
    @Transactional
    public Map<String, Object> checkPaymentStatus(long orderCode, Order order) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/v2/payment-requests/" + orderCode,
                HttpMethod.GET, request, Map.class);

            Map<String, Object> data = response.getBody();
            log.info("PayOS check status response: {}", data);

            if (data != null && "00".equals(String.valueOf(data.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> respData = (Map<String, Object>) data.get("data");
                if (respData != null) {
                    String status = (String) respData.get("status");
                    if ("PAID".equals(status)) {
                        // Đã thanh toán - cập nhật order nếu chưa
                        if (!"PAID".equals(order.getPaymentStatus())) {
                            order.setStatus("COMPLETED");
                            order.setPaymentStatus("PAID");
                            order.setActualPickupTime(LocalDateTime.now());
                            order.setUpdatedAt(LocalDateTime.now());
                            orderRepository.save(order);

                            // Trừ kho
                            if (order.getOrderItems() != null) {
                                for (var item : order.getOrderItems()) {
                                    Product product = item.getProduct();
                                    if (product != null && product.getStockQuantity() != null) {
                                        int newStock = product.getStockQuantity() - (item.getQuantity() != null ? item.getQuantity() : 0);
                                        product.setStockQuantity(Math.max(0, newStock));
                                        if (newStock <= 0) product.setActive(false);
                                        productRepository.save(product);
                                    }
                                }
                            }

                            // Tạo Transaction
                            String method = respData.get("counterAccountBankId") != null ? "BANK_TRANSFER" : "MOMO";
                            createSaleTransaction(order, method);

                            return Map.of("paid", true, "message", "✅ Thanh toán thành công! Đã cập nhật đơn hàng.");
                        }
                        return Map.of("paid", true, "message", "✅ Đơn hàng đã được thanh toán.");
                    }
                    return Map.of("paid", false, "message", "⏳ Trạng thái PayOS: " + status);
                }
            }
            return Map.of("paid", false, "message", "⏳ Chưa có thông tin thanh toán");
        } catch (Exception e) {
            log.error("PayOS checkPaymentStatus error: {}", e.getMessage());
            return Map.of("paid", false, "message", "Lỗi kiểm tra: " + e.getMessage());
        }
    }

    /**
     * Xử lý webhook PayOS khi có thanh toán.
     * PayOS gửi POST đến /api/payment/webhook với body JSON.
     */
    @Transactional
    public Map<String, Object> processWebhook(Map<String, Object> webhookData) {
        try {
            // Verify signature
            String receivedSignature = (String) webhookData.get("signature");
            if (receivedSignature == null) {
                return Map.of("success", false, "message", "Thiếu chữ ký");
            }

            // Extract data
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            if (data == null) return Map.of("success", false, "message", "Thiếu dữ liệu");

            long orderCode = Long.parseLong(String.valueOf(data.get("orderCode")));
            String status = (String) data.get("status"); // PAID, CANCELLED, PENDING
            int amountPaid = data.get("amount") != null ? ((Number) data.get("amount")).intValue() : 0;
            String paymentMethod = data.get("counterAccountBankId") != null
                ? "BANK_TRANSFER"
                : (data.get("paymentLinkId") != null ? "MOMO" : "CASH");

            // Tìm order bằng orderCode lưu trong pickupQrCode
            Order order = orderRepository.findByPickupQrCode(String.valueOf(orderCode)).orElse(null);
            if (order == null) {
                return Map.of("success", false, "message", "Không tìm thấy đơn hàng cho orderCode: " + orderCode);
            }

            // Chỉ xử lý khi trạng thái là PAID (đã thanh toán, không hủy)
            if (!"PAID".equals(status)) {
                log.info("Webhook status is '{}' for order {}, ignoring", status, order.getOrderId());
                return Map.of("success", true, "message", "Trạng thái không phải PAID, bỏ qua");
            }

            // Kiểm tra đã xử lý chưa
            if ("PAID".equals(order.getPaymentStatus())) {
                return Map.of("success", true, "message", "Đơn hàng đã được xử lý trước đó");
            }

            long expectedAmount = Math.round(order.getFinalAmount() != null ? order.getFinalAmount() : 0);
            String resultMessage;

            if (amountPaid >= expectedAmount) {
                // Đủ tiền → COMPLETED
                order.setStatus("COMPLETED");
                order.setPaymentStatus("PAID");
                order.setPaymentMethod(paymentMethod);
                order.setActualPickupTime(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                // Trừ kho
                if (order.getOrderItems() != null) {
                    for (var item : order.getOrderItems()) {
                        Product product = item.getProduct();
                        if (product != null && product.getStockQuantity() != null) {
                            int newStock = product.getStockQuantity() - (item.getQuantity() != null ? item.getQuantity() : 0);
                            product.setStockQuantity(Math.max(0, newStock));
                            if (newStock <= 0) product.setActive(false);
                            productRepository.save(product);
                        }
                    }
                }

                // Tạo Transaction record
                createSaleTransaction(order, paymentMethod);

                if (amountPaid > expectedAmount) {
                    resultMessage = "Thanh toán thành công. Bạn đã chuyển thừa " + (amountPaid - expectedAmount)
                        + "đ, vui lòng liên hệ hotline để được hoàn lại.";
                } else {
                    resultMessage = "Thanh toán thành công. Cảm ơn bạn đã mua sắm tại DealXanh!";
                }

            } else {
                // Chưa đủ tiền → lưu partial, tạo mã mới
                long remaining = expectedAmount - amountPaid;
                order.setPaymentStatus("PARTIAL");
                order.setNote((order.getNote() != null ? order.getNote() : "") +
                    " [Đã thanh toán: " + amountPaid + "đ - Còn thiếu: " + remaining + "đ - " +
                    LocalDateTime.now() + "]");
                order.setUpdatedAt(LocalDateTime.now());

                // Tạo mã PayOS mới cho số tiền còn lại
                String newQrCode = String.valueOf(System.currentTimeMillis() / 1000 + 1);
                order.setPickupQrCode(newQrCode);
                orderRepository.save(order);

                // Tạo transaction ghi nhận partial
                Transaction partialTx = new Transaction();
                partialTx.setStore(order.getStore());
                partialTx.setOrder(order);
                partialTx.setType("Sale");
                partialTx.setAmount((double) amountPaid);
                partialTx.setPlatformFee(calculatePlatformFee(amountPaid, order.getStore()));
                partialTx.setNetAmount(amountPaid - partialTx.getPlatformFee());
                partialTx.setStatus("COMPLETED");
                partialTx.setPaymentMethod(paymentMethod);
                partialTx.setTransactionRef("partial-" + orderCode);
                partialTx.setDescription("Thanh toán một phần - còn thiếu " + remaining + "đ");
                partialTx.setCreatedAt(LocalDateTime.now());
                transactionRepository.save(partialTx);

                resultMessage = "Bạn mới chuyển " + amountPaid + "đ, còn thiếu " + remaining
                    + "đ. Vui lòng quét mã mới để thanh toán phần còn lại.";
            }

            return Map.of("success", true, "message", resultMessage);

        } catch (Exception e) {
            log.error("PayOS webhook processing error: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi xử lý: " + e.getMessage());
        }
    }

    private void createSaleTransaction(Order order, String paymentMethod) {
        double amount = order.getFinalAmount() != null ? order.getFinalAmount() : 0;
        Store store = order.getStore();
        double platformFee = calculatePlatformFee(amount, store);
        double netAmount = amount - platformFee;

        Transaction tx = new Transaction();
        tx.setStore(store);
        tx.setOrder(order);
        tx.setType("Sale");
        tx.setAmount(amount);
        tx.setPlatformFee(platformFee);
        tx.setNetAmount(netAmount);
        tx.setStatus("COMPLETED");
        tx.setPaymentMethod(paymentMethod);
        tx.setTransactionRef(order.getPickupQrCode());
        tx.setDescription("Thanh toán đơn hàng DX-" + order.getOrderId());
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    private double calculatePlatformFee(double amount, Store store) {
        double rate = 0.10; // default BRONZE 10%
        if (store != null) {
            double storeRate = store.getCommissionRate();
            if (storeRate > 0) rate = storeRate;
        }
        return Math.round(amount * rate);
    }

    /**
     * Tạo chữ ký HMAC-SHA256 cho PayOS.
     * PayOS yêu cầu THỨ TỰ CỐ ĐỊNH: amount, cancelUrl, description, orderCode, returnUrl
     * (không bao gồm expiredAt trong chữ ký)
     */
    private String createSignature(Map<String, Object> data) {
        try {
            // PayOS signature format (theo official SDK: amount, cancelUrl, description, orderCode, returnUrl)
            String rawData = "amount=" + data.get("amount")
                + "&cancelUrl=" + data.get("cancelUrl")
                + "&description=" + data.get("description")
                + "&orderCode=" + data.get("orderCode")
                + "&returnUrl=" + data.get("returnUrl");

            log.info("PayOS signature raw data: {}", rawData);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            String signature = hexString.toString();
            log.info("PayOS generated signature: {}", signature);
            return signature;
        } catch (Exception e) {
            log.error("Signature creation error", e);
            return "";
        }
    }
}
