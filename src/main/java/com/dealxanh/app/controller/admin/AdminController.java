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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String dashboard(Model model, Authentication authentication) {
        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
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

        // Total users (buyers)
        long totalBuyers = userRepository.countByRoleName("ROLE_USER");
        stats.put("totalBuyers", totalBuyers);

        // Total sellers (store owners + staff)
        long totalSellers = 0L;
        totalSellers += userRepository.countByRoleName("ROLE_STORE_OWNER");
        totalSellers += userRepository.countByRoleName("ROLE_STORE_STAFF");
        stats.put("totalSellers", totalSellers);

        // Active stores
        long activeStores = storeRepository.countByStatus("ACTIVE");
        stats.put("activeStores", activeStores);

        // Pending stores
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

        // Pending stores for sidebar
        model.addAttribute("pendingSellerCount", pendingStores);

        // Recent pending stores (last 5)
        List<Store> recentPendingStores = storeRepository.findByStatus("PENDING");
        model.addAttribute("recentPendingStores", recentPendingStores.stream().limit(4).collect(java.util.stream.Collectors.toList()));

        // Top sellers today (by GMV)
        List<Store> topStores = storeRepository.findTop5ByOrderByAverageRatingDesc();
        model.addAttribute("topStores", topStores);

        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Dashboard Tổng quan");

        // Set active sidebar
        model.addAttribute("activeSidebar", "dashboard");

        return "admin/dashboard";
    }

    @GetMapping("/seller-verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
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

        // Count pending stores for badge
        long pendingCount = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingCount);

        model.addAttribute("stores", stores);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeSidebar", "seller-verify");

        return "admin/seller-verify";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String users(
            @RequestParam(required = false) String role,
            Model model) {

        List<User> users;
        if (role != null && !role.isEmpty()) {
            if ("buyer".equals(role)) {
                Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
                users = userRole != null ? userRepository.findByRole(userRole) : new java.util.ArrayList<>();
            } else if ("seller".equals(role)) {
                Role ownerRole = roleRepository.findByName("ROLE_STORE_OWNER").orElse(null);
                Role staffRole = roleRepository.findByName("ROLE_STORE_STAFF").orElse(null);
                users = new java.util.ArrayList<>();
                if (ownerRole != null) {
                    users.addAll(userRepository.findByRole(ownerRole));
                }
                if (staffRole != null) {
                    users.addAll(userRepository.findByRole(staffRole));
                }
            } else if ("admin".equals(role)) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
                Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR").orElse(null);
                users = new java.util.ArrayList<>();
                if (adminRole != null) {
                    users.addAll(userRepository.findByRole(adminRole));
                }
                if (moderatorRole != null) {
                    users.addAll(userRepository.findByRole(moderatorRole));
                }
            } else {
                users = userRepository.findAll();
            }
        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("currentRole", role);
        model.addAttribute("activeSidebar", "users");

        return "admin/users";
    }

    @GetMapping("/dispute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String disputes(Model model) {
        // Placeholder for disputes page
        model.addAttribute("activeSidebar", "dispute");
        return "admin/dispute";
    }

    @GetMapping("/finance")
    @PreAuthorize("hasRole('ADMIN')")
    public String finance(Model model) {
        // Placeholder for finance page
        model.addAttribute("activeSidebar", "finance");
        return "admin/finance";
    }
}
