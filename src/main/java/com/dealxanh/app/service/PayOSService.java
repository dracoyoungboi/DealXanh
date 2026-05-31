package com.dealxanh.app.service;

import com.dealxanh.app.concurrency.ResourceLockManager;
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
import java.util.concurrent.locks.ReentrantLock;

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
    private final ResourceLockManager lockManager;

    public PayOSService(OrderRepository orderRepository,
                        TransactionRepository transactionRepository,
                        ProductRepository productRepository,
                        ResourceLockManager lockManager) {
        this.restTemplate = new RestTemplate();
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.lockManager = lockManager;
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

        // Check if already paid
        String existingQr = order.getPickupQrCode();
        if (existingQr != null && !existingQr.isEmpty() && !existingQr.startsWith("DX-")) {
            try {
                long existingCode = Long.parseLong(existingQr);
                Map<String, Object> fetchResult = fetchPaymentInfo(existingCode);
                if (fetchResult != null && "PAID".equals(fetchResult.get("status"))) {
                    return Map.of("success", false, "message", "Đơn hàng đã được thanh toán");
                }
                // Already paid check passed, continue to create new payment link
                // (old one will auto-expire on PayOS after 30 min)
            } catch (NumberFormatException e) { /* not a PayOS code */ }
        }

        String description = "Thanh toan don hang DX-" + orderId;
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
     * Fetch existing PayOS payment info without triggering payment confirmation.
     */
    private Map<String, Object> fetchPaymentInfo(long orderCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl + "/v2/payment-requests/" + orderCode,
                HttpMethod.GET, request,
                (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> data = response.getBody();
            if (data != null && "00".equals(String.valueOf(data.get("code")))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> respData = (Map<String, Object>) data.get("data");
                return respData;
            }
        } catch (Exception e) {
            log.warn("fetchPaymentInfo error for orderCode {}: {}", orderCode, e.getMessage());
        }
        return null;
    }

    /**
     * Cancel a PayOS payment link when order is cancelled.
     */
    public void cancelPaymentLink(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getPickupQrCode() == null) return;

        // Only cancel if the QR code is a PayOS orderCode (numeric, no "DX-" prefix)
        String qrCode = order.getPickupQrCode();
        if (qrCode.startsWith("DX-")) return;

        try {
            long orderCode = Long.parseLong(qrCode);
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            restTemplate.exchange(
                apiUrl + "/v2/payment-requests/" + orderCode + "/cancel",
                HttpMethod.POST, request, Map.class);
            log.info("PayOS payment link cancelled for order #{}: orderCode={}", orderId, orderCode);
        } catch (NumberFormatException e) {
            // Not a PayOS orderCode, skip
        } catch (Exception e) {
            log.warn("Failed to cancel PayOS payment for order #{}: {}", orderId, e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán từ PayOS API.
     * Dùng khi chạy localhost không nhận được webhook.
     */
    @Transactional
    public Map<String, Object> checkPaymentStatus(long orderCode, Order order) {
        ReentrantLock lock = lockManager.acquireLock("ORDER", order.getOrderId());
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
                        if (!"PAID".equals(order.getPaymentStatus())) {
                            long expectedAmount = Math.round(order.getFinalAmount() != null ? order.getFinalAmount() : 0);
                            int amountPaid = respData.get("amount") != null ? ((Number) respData.get("amount")).intValue() : (int) expectedAmount;
                            String method = respData.get("counterAccountBankId") != null ? "BANK_TRANSFER" : "MOMO";
                            return confirmPaymentAndDeductStock(order, orderCode, method, amountPaid, expectedAmount);
                        }
                        return Map.of("paid", true, "message", "Don hang da duoc thanh toan.");
                    }
                    return Map.of("paid", false, "message", "Trang thai PayOS: " + status);
                }
            }
            return Map.of("paid", false, "message", "Chua co thong tin thanh toan");
        } catch (Exception e) {
            log.error("PayOS checkPaymentStatus error: {}", e.getMessage());
            return Map.of("paid", false, "message", "Loi kiem tra: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Critical section for payment confirmation. Protected by FIFO lock
     * and PESSIMISTIC_WRITE to ensure exactly-once stock deduction.
     */
    private Map<String, Object> confirmPaymentAndDeductStock(Order order, long orderCode,
            String paymentMethod, int amountPaid, long expectedAmount) {
        // Re-read with PESSIMISTIC_WRITE lock to get latest committed state
        Order lockedOrder = orderRepository.findByIdWithLock(order.getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found: " + order.getOrderId()));

        if ("PAID".equals(lockedOrder.getPaymentStatus())) {
            return Map.of("paid", true, "message", "Don hang da duoc thanh toan.");
        }

        if (amountPaid >= expectedAmount) {
            // BRANCH: prepay (PENDING/CONFIRMED) vs at-store (READY_FOR_PICKUP)
            if ("PENDING".equals(lockedOrder.getStatus()) || "CONFIRMED".equals(lockedOrder.getStatus())) {
                // PREPAY: buyer paid upfront -> auto-transition to READY_FOR_PICKUP
                lockedOrder.setStatus("READY_FOR_PICKUP");
                lockedOrder.setPaymentStatus("PAID");
                lockedOrder.setUpdatedAt(LocalDateTime.now());
                // NO stock deduction, NO transaction, NO actualPickupTime yet
                orderRepository.save(lockedOrder);
                return Map.of("paid", true, "message",
                    "Thanh toan thanh cong! Don hang dang cho cua hang xac nhan.");
            }

            // AT-STORE payment: order already READY_FOR_PICKUP -> complete
            lockedOrder.setStatus("COMPLETED");
            lockedOrder.setPaymentStatus("PAID");
            lockedOrder.setActualPickupTime(LocalDateTime.now());
            lockedOrder.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(lockedOrder);

            // Deduct stock
            if (lockedOrder.getOrderItems() != null) {
                for (var item : lockedOrder.getOrderItems()) {
                    Product product = item.getProduct();
                    if (product != null && product.getStockQuantity() != null) {
                        int newStock = product.getStockQuantity() - (item.getQuantity() != null ? item.getQuantity() : 0);
                        product.setStockQuantity(Math.max(0, newStock));
                        if (newStock <= 0) product.setActive(false);
                        productRepository.save(product);
                    }
                }
            }

            createSaleTransaction(lockedOrder, paymentMethod);

            String msg = amountPaid > expectedAmount
                ? "Thanh toan thanh cong. Ban da chuyen thua " + (amountPaid - expectedAmount) + "d."
                : "Thanh toan thanh cong! Da cap nhat don hang.";
            return Map.of("paid", true, "message", msg);
        } else {
            long remaining = expectedAmount - amountPaid;
            lockedOrder.setPaymentStatus("PARTIAL");
            lockedOrder.setNote((lockedOrder.getNote() != null ? lockedOrder.getNote() : "")
                + " [Da thanh toan: " + amountPaid + "d - Con thieu: " + remaining + "d]");
            lockedOrder.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(lockedOrder);
            return Map.of("paid", false, "message", "Con thieu " + remaining + "d.");
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
                return Map.of("success", true, "message", "Trang thai khong phai PAID, bo qua");
            }

            // FIFO queue: protect concurrent webhook + polling for same order
            ReentrantLock lock = lockManager.acquireLock("ORDER", order.getOrderId());
            try {
                long expectedAmount = Math.round(order.getFinalAmount() != null ? order.getFinalAmount() : 0);
                Map<String, Object> result = confirmPaymentAndDeductStock(
                    order, orderCode, paymentMethod, amountPaid, expectedAmount);
                return Map.of("success", true, "message", result.get("message"));
            } finally {
                lock.unlock();
            }

        } catch (Exception e) {
            log.error("PayOS webhook processing error: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Lỗi xử lý: " + e.getMessage());
        }
    }

    public void createSaleTransaction(Order order, String paymentMethod) {
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
