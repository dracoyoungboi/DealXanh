package com.dealxanh.app.controller.admin;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MODERATOR')")
    public String dashboard(Model model, Authentication authentication) {
        // Get current moderator user
        String username = authentication.getName();
        User moderatorUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (moderatorUser != null) {
            model.addAttribute("adminUser", moderatorUser);
        }

        // Get today's date range
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);

        // Get statistics for dashboard
        Map<String, Object> stats = new HashMap<>();

        // Today's GMV (Gross Merchandise Value)
        Double todayGMV = orderRepository.sumRevenueByPeriod(todayStart, todayEnd);
        stats.put("todayGMV", todayGMV != null ? todayGMV : 0.0);

        // Today's orders count
        Long todayOrders = orderRepository.countOrdersByPeriod(todayStart, todayEnd);
        stats.put("todayOrders", todayOrders != null ? todayOrders : 0L);

        // Commission (10% of GMV)
        Double commission = (todayGMV != null ? todayGMV : 0.0) * 0.10;
        stats.put("todayCommission", commission);

        // Pending stores (main focus for moderators)
        long pendingStores = storeRepository.countByStatus("PENDING");
        stats.put("pendingStores", pendingStores);

        // Get 7-day GMV data for chart
        Map<String, Double> weeklyGMV = new HashMap<>();
        Map<String, Long> weeklyOrders = new HashMap<>();

        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            Double dayGMV = orderRepository.sumRevenueByPeriod(dayStart, dayEnd);
            Long dayOrders = orderRepository.countOrdersByPeriod(dayStart, dayEnd);

            weeklyGMV.put(days[6 - i], dayGMV != null ? dayGMV : 0.0);
            weeklyOrders.put(days[6 - i], dayOrders != null ? dayOrders : 0L);
        }

        stats.put("weeklyGMV", weeklyGMV);
        stats.put("weeklyOrders", weeklyOrders);

        // Total orders
        try {
            stats.put("totalOrders", orderRepository.count());
        } catch (Exception e) {
            stats.put("totalOrders", 0L);
        }

        // Recent pending stores (last 5)
        List<Store> recentPendingStores = storeRepository.findByStatus("PENDING");
        model.addAttribute("recentPendingStores", recentPendingStores.stream().limit(5).toList());

        // Pending stores count for sidebar
        model.addAttribute("pendingSellerCount", pendingStores);

        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Moderator Dashboard");
        model.addAttribute("activeSidebar", "dashboard");

        return "moderator/dashboard";
    }

    @GetMapping("/seller-verify")
    @PreAuthorize("hasRole('MODERATOR')")
    public String sellerVerify(
            @RequestParam(required = false) String status,
            Model model) {

        // Get stores based on status filter
        List<Store> stores;
        if (status != null && !status.isEmpty()) {
            if ("pending".equals(status)) {
                stores = storeRepository.findByStatus("PENDING");
            } else if ("approved".equals(status)) {
                stores = storeRepository.findByStatus("ACTIVE");
            } else if ("rejected".equals(status)) {
                stores = storeRepository.findByStatus("REJECTED");
            } else {
                stores = storeRepository.findAll();
            }
        } else {
            // Default: show pending stores first
            stores = storeRepository.findByStatus("PENDING");
        }

        // Count stores by status for filter tabs
        long pendingCount = storeRepository.countByStatus("PENDING");
        long approvedCount = storeRepository.countByStatus("ACTIVE");
        long rejectedCount = storeRepository.countByStatus("REJECTED");

        model.addAttribute("pendingSellerCount", pendingCount);
        model.addAttribute("approvedSellerCount", approvedCount);
        model.addAttribute("rejectedSellerCount", rejectedCount);

        model.addAttribute("stores", stores);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeSidebar", "seller-verify");

        return "moderator/seller-verify";
    }

    @PostMapping("/seller-verify/{storeId}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public String approveStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) String currentStatus,
            RedirectAttributes redirectAttributes) {

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/moderator/seller-verify";
        }

        if (!"PENDING".equals(store.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Cửa hàng này đã được xử lý rồi");
            return "redirect:/moderator/seller-verify";
        }

        // Get current moderator info
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User moderator = userRepository.findByUsername(currentUsername)
                .orElse(userRepository.findByEmail(currentUsername).orElse(null));

        // Update store status
        store.setStatus("ACTIVE");
        store.setApprovedBy(moderator);
        store.setReviewedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);

        redirectAttributes.addFlashAttribute("success", "Đã duyệt cửa hàng " + store.getStoreName() + " thành công");
        return "redirect:/moderator/seller-verify" + (currentStatus != null && !currentStatus.isEmpty() ? "?status=" + currentStatus : "");
    }

    @PostMapping("/seller-verify/{storeId}/reject")
    @PreAuthorize("hasRole('MODERATOR')")
    public String rejectStore(
            @PathVariable Long storeId,
            @RequestParam String rejectionReason,
            @RequestParam(required = false) String currentStatus,
            RedirectAttributes redirectAttributes) {

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/moderator/seller-verify";
        }

        if (!"PENDING".equals(store.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Cửa hàng này đã được xử lý rồi");
            return "redirect:/moderator/seller-verify";
        }

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do từ chối");
            return "redirect:/moderator/seller-verify";
        }

        // Get current moderator info
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User moderator = userRepository.findByUsername(currentUsername)
                .orElse(userRepository.findByEmail(currentUsername).orElse(null));

        // Update store status
        store.setStatus("REJECTED");
        store.setRejectedBy(moderator);
        store.setRejectionReason(rejectionReason.trim());
        store.setReviewedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);

        redirectAttributes.addFlashAttribute("success", "Đã từ chối cửa hàng " + store.getStoreName() + " thành công");
        return "redirect:/moderator/seller-verify" + (currentStatus != null && !currentStatus.isEmpty() ? "?status=" + currentStatus : "");
    }
}
