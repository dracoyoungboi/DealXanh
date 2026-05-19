package com.dealxanh.app.controller;

import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.CategoryRepository;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STORE_STAFF')")
public class StaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // Today's orders by status
        var pendingPage = orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(
                storeId, "PENDING", org.springframework.data.domain.PageRequest.of(0, 50));
        var confirmedPage = orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(
                storeId, "CONFIRMED", org.springframework.data.domain.PageRequest.of(0, 50));
        var readyPage = orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(
                storeId, "READY_FOR_PICKUP", org.springframework.data.domain.PageRequest.of(0, 50));
        List<Order> pendingOrders = pendingPage.getContent();
        List<Order> confirmedOrders = confirmedPage.getContent();
        List<Order> readyOrders = readyPage.getContent();

        long servedToday = orderRepository.countByStoreStoreIdAndStatus(storeId, "COMPLETED");
        long pendingToday = pendingOrders.size() + confirmedOrders.size() + readyOrders.size();

        // Pickup slots from store config
        String[] slots = store.getPickupSlots() != null
                ? store.getPickupSlots().split(",")
                : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"};

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("readyOrders", readyOrders);
        model.addAttribute("servedToday", servedToday);
        model.addAttribute("pendingToday", pendingToday);
        model.addAttribute("pickupSlots", slots);
        model.addAttribute("activePage", "dashboard");

        return "staff/dashboard";
    }

    // ============ ORDERS ============

    @GetMapping("/orders")
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // Status counts
        long pendingCount = orderRepository.countByStoreStoreIdAndStatus(storeId, "PENDING");
        long confirmedCount = orderRepository.countByStoreStoreIdAndStatus(storeId, "CONFIRMED");
        long readyCount = orderRepository.countByStoreStoreIdAndStatus(storeId, "READY_FOR_PICKUP");
        long completedCount = orderRepository.countByStoreStoreIdAndStatus(storeId, "COMPLETED");

        // Fetch orders with search
        int fetchSize = (search != null && !search.isEmpty()) ? 200 : size;
        var pageable = PageRequest.of(0, fetchSize, Sort.by("createdAt").descending());
        var ordersPage = status != null && !status.isEmpty()
                ? orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable)
                : orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, pageable);

        List<Order> orders = ordersPage.getContent();

        // Filter by search
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase().trim();
            orders = orders.stream()
                .filter(o -> {
                    if (String.valueOf(o.getOrderId()).contains(s)) return true;
                    if (o.getUser() != null) {
                        if (o.getUser().getFullName() != null && o.getUser().getFullName().toLowerCase().contains(s)) return true;
                        if (o.getUser().getUsername() != null && o.getUser().getUsername().toLowerCase().contains(s)) return true;
                    }
                    if (o.getOrderItems() != null) {
                        return o.getOrderItems().stream().anyMatch(i ->
                            i.getProduct() != null && i.getProduct().getName() != null &&
                            i.getProduct().getName().toLowerCase().contains(s));
                    }
                    return false;
                })
                .toList();
        }

        // Paginate
        int total = orders.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Order> paged = total > 0 ? orders.subList(start, end) : List.of();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("orders", paged);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("readyCount", readyCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalOrders", (long) total);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("pendingToday", pendingCount + confirmedCount + readyCount);
        model.addAttribute("pickupSlots", store.getPickupSlots() != null ? store.getPickupSlots().split(",") : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"});
        model.addAttribute("activePage", "orders");

        return "staff/orders";
    }

    // ============ PRODUCTS ============

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        List<Product> allProducts = productRepository.findByStoreStoreIdAndDeletedFalse(storeId);

        // Filters
        List<Product> filtered = allProducts;
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase().trim();
            filtered = filtered.stream().filter(p -> p.getName().toLowerCase().contains(s)).toList();
        }
        if (categoryId != null) {
            filtered = filtered.stream().filter(p -> p.getCategory() != null && p.getCategory().getCategoryId().equals(categoryId)).toList();
        }

        // Pagination
        int total = filtered.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Product> paged = total > 0 ? filtered.subList(start, end) : List.of();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("products", paged);
        model.addAttribute("totalProducts", total);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("pendingToday", orderRepository.countPendingOrdersByStore(storeId));
        model.addAttribute("pickupSlots", store.getPickupSlots() != null ? store.getPickupSlots().split(",") : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"});
        model.addAttribute("activePage", "products");

        return "staff/products";
    }

    // ============ PROFILE ============

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/seller/login";
        Store store = user.getWorkStore();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("activePage", "profile");
        model.addAttribute("pendingToday",
            orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "PENDING")
            + orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "CONFIRMED")
            + orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "READY_FOR_PICKUP"));
        model.addAttribute("pickupSlots", store.getPickupSlots() != null ? store.getPickupSlots().split(",") : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"});
        return "staff/profile";
    }

    // ============ PROFILE ACTIONS ============

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile avatarFile,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/staff/profile";
        }
        try {
            if (fullName != null && !fullName.trim().isEmpty()) user.setFullName(fullName.trim());
            if (email != null) user.setEmail(email.trim().isEmpty() ? null : email.trim());
            if (phone != null) user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String contentType = avatarFile.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                    redirectAttributes.addFlashAttribute("error", "Ảnh đại diện phải là JPG, PNG hoặc WEBP");
                    return "redirect:/staff/profile";
                }
                if (avatarFile.getSize() > 2 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Ảnh đại diện tối đa 2MB");
                    return "redirect:/staff/profile";
                }
                String path = saveUploadedFile(avatarFile);
                user.setAvatarUrl(path);
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/staff/profile";
        }
        try {
            if (currentPassword == null || currentPassword.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại");
                return "redirect:/staff/profile";
            }
            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "redirect:/staff/profile";
            }
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                return "redirect:/staff/profile";
            }
            var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            if (!encoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/staff/profile";
            }
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/profile";
    }

    private String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) return null;
        java.security.MessageDigest md5;
        try { md5 = java.security.MessageDigest.getInstance("MD5"); }
        catch (java.security.NoSuchAlgorithmException e) { throw new java.io.IOException("MD5 not available", e); }
        byte[] digest = md5.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        String hash = sb.toString();
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) ext = originalName.substring(originalName.lastIndexOf("."));
        String fileName = hash + ext;
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) java.nio.file.Files.createDirectories(uploadPath);
        java.nio.file.Path targetPath = uploadPath.resolve(fileName);
        if (!java.nio.file.Files.exists(targetPath)) java.nio.file.Files.copy(file.getInputStream(), targetPath);
        return "/uploads/" + fileName;
    }

    // ============ ORDER DETAIL API ============

    @GetMapping("/api/orders/{orderId}/detail")
    @ResponseBody
    public java.util.Map<String, Object> getOrderDetail(@PathVariable Long orderId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Order order = orderRepository.findByIdWithAllDetails(orderId).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Không tìm thấy đơn hàng");
        if (!order.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Đơn hàng không thuộc cửa hàng của bạn");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("orderId", order.getOrderId());
        result.put("status", order.getStatus());
        result.put("customerName", order.getUser() != null ? (order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getUsername()) : "N/A");
        result.put("customerPhone", order.getUser() != null ? order.getUser().getPhone() : null);
        result.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        result.put("totalAmount", order.getTotalAmount());
        result.put("finalAmount", order.getFinalAmount());
        result.put("discountAmount", order.getDiscountAmount());
        result.put("paymentMethod", order.getPaymentMethod());
        result.put("paymentStatus", order.getPaymentStatus());
        result.put("couponCode", order.getCouponCode());
        result.put("scheduledPickupTime", order.getScheduledPickupTime() != null ? order.getScheduledPickupTime().toString() : null);
        result.put("actualPickupTime", order.getActualPickupTime() != null ? order.getActualPickupTime().toString() : null);
        result.put("cancellationReason", order.getCancellationReason());
        result.put("pickupQrCode", order.getPickupQrCode());

        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        if (order.getOrderItems() != null) {
            for (var item : order.getOrderItems()) {
                java.util.Map<String, Object> i = new java.util.HashMap<>();
                i.put("productName", item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm");
                i.put("quantity", item.getQuantity());
                i.put("unitPrice", item.getUnitPrice());
                i.put("subtotal", item.getSubtotal());
                items.add(i);
            }
        }
        result.put("items", items);
        return result;
    }

    // ============ ORDER STATUS UPDATE ============

    @PostMapping("/orders/{orderId}/status")
    @ResponseBody
    public java.util.Map<String, Object> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus,
            Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Không tìm thấy đơn hàng");
        if (!order.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Đơn hàng không thuộc cửa hàng của bạn");

        order.setStatus(newStatus);
        if ("READY_FOR_PICKUP".equals(newStatus) && order.getPickupQrCode() == null) {
            // Generate QR code
            String qrCode = "DX-" + orderId + "-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            order.setPickupQrCode(qrCode);
        }
        if ("COMPLETED".equals(newStatus)) {
            order.setActualPickupTime(java.time.LocalDateTime.now());
        }
        orderRepository.save(order);
        return java.util.Map.of("success", true, "message", "Đã cập nhật trạng thái đơn hàng");
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        String username = principal.getName();
        return userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
    }
}
