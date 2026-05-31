package com.dealxanh.app.controller;

import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.service.PayOSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PayOSService payOSService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Seller/Staff tạo mã QR PayOS cho đơn hàng.
     * Chỉ hoạt động khi order ở trạng thái READY_FOR_PICKUP.
     */
    @PostMapping("/create/{orderId}")
    public Map<String, Object> createPayment(@PathVariable Long orderId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Chưa đăng nhập");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return Map.of("success", false, "message", "Không tìm thấy đơn hàng");

        // Kiểm tra quyền: phải là store owner hoặc staff của store đó
        if (user.getWorkStore() == null ||
            !order.getStore().getStoreId().equals(user.getWorkStore().getStoreId())) {
            return Map.of("success", false, "message", "Bạn không có quyền tạo mã thanh toán");
        }

        // Chỉ tạo QR khi order đang READY_FOR_PICKUP
        if (!"READY_FOR_PICKUP".equals(order.getStatus())) {
            return Map.of("success", false, "message",
                "Đơn hàng phải ở trạng thái Sẵn sàng nhận. Hiện tại: " + order.getStatus());
        }

        return payOSService.createPaymentLink(orderId);
    }

    /**
     * Buyer tạo mã QR PayOS để thanh toán trước.
     * Cho phép PENDING/CONFIRMED/READY_FOR_PICKUP - buyer phải là chủ đơn.
     */
    @PostMapping("/buyer-create/{orderId}")
    public Map<String, Object> buyerCreatePayment(@PathVariable Long orderId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Chưa đăng nhập");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return Map.of("success", false, "message", "Không tìm thấy đơn hàng");

        // Kiểm tra quyền: buyer phải là chủ đơn
        if (order.getUser() == null || !user.getUserId().equals(order.getUser().getUserId())) {
            return Map.of("success", false, "message", "Bạn không có quyền tạo mã thanh toán cho đơn này");
        }

        // Không cho tạo QR nếu đã thanh toán hoặc đã hủy/hoàn thành
        if ("PAID".equals(order.getPaymentStatus())) {
            return Map.of("success", false, "message", "Đơn hàng đã được thanh toán");
        }
        if ("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
            return Map.of("success", false, "message", "Đơn hàng không thể thanh toán (đã " + order.getStatus() + ")");
        }

        return payOSService.createPaymentLink(orderId);
    }

    /**
     * Webhook nhận IPN từ PayOS. Public endpoint - không cần auth.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = payOSService.processWebhook(body);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * Kiểm tra trạng thái thanh toán - poll PayOS API.
     */
    @GetMapping("/status/{orderId}")
    public Map<String, Object> paymentStatus(@PathVariable Long orderId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Chưa đăng nhập");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return Map.of("success", false, "message", "Không tìm thấy đơn hàng");

        // Poll PayOS để kiểm tra trạng thái thực tế
        if (order.getPickupQrCode() != null && !"PAID".equals(order.getPaymentStatus())) {
            Map<String, Object> payosResult = payOSService.checkPaymentStatus(
                Long.parseLong(order.getPickupQrCode()), order);
            boolean paid = (boolean) payosResult.getOrDefault("paid", false);
            return Map.of(
                "success", true,
                "orderId", order.getOrderId(),
                "status", order.getStatus(),
                "paymentStatus", order.getPaymentStatus(),
                "paymentMethod", order.getPaymentMethod(),
                "finalAmount", order.getFinalAmount(),
                "paidFromPayOS", paid,
                "message", payosResult.getOrDefault("message", "")
            );
        }

        return Map.of(
            "success", true,
            "orderId", order.getOrderId(),
            "status", order.getStatus(),
            "paymentStatus", order.getPaymentStatus(),
            "paymentMethod", order.getPaymentMethod(),
            "finalAmount", order.getFinalAmount(),
            "paidFromPayOS", false
        );
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        String name = principal.getName();
        User user = userRepository.findByUsername(name).orElse(null);
        if (user == null) user = userRepository.findByEmail(name).orElse(null);
        return user;
    }
}
