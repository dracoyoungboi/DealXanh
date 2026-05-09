package com.dealxanh.app.controller.admin;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.entity.Order;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Get all stores first
        List<Store> allStores = storeRepository.findAll();

        // Filter by status
        List<Store> filteredStores;
        if (status != null && !status.isEmpty()) {
            if ("pending".equals(status)) {
                filteredStores = allStores.stream()
                        .filter(s -> "PENDING".equals(s.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            } else if ("approved".equals(status)) {
                filteredStores = allStores.stream()
                        .filter(s -> "ACTIVE".equals(s.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            } else if ("rejected".equals(status)) {
                filteredStores = allStores.stream()
                        .filter(s -> "REJECTED".equals(s.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                filteredStores = allStores;
            }
        } else {
            // Default: show pending stores first
            filteredStores = allStores.stream()
                    .filter(s -> "PENDING".equals(s.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply search filter (store name, owner email)
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = search.toLowerCase().trim();
            filteredStores = filteredStores.stream()
                    .filter(s -> (s.getStoreName() != null && s.getStoreName().toLowerCase().contains(searchLower)) ||
                               (s.getOwner() != null && s.getOwner().getEmail() != null && s.getOwner().getEmail().toLowerCase().contains(searchLower)) ||
                               (s.getOwner() != null && s.getOwner().getFullName() != null && s.getOwner().getFullName().toLowerCase().contains(searchLower)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply city filter
        if (city != null && !city.trim().isEmpty() && !"Tất cả thành phố".equals(city)) {
            final String cityFilter = city.trim();
            filteredStores = filteredStores.stream()
                    .filter(s -> s.getCity() != null && s.getCity().equals(cityFilter))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply sort
        if ("oldest".equals(sort)) {
            filteredStores.sort((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()));
        } else {
            // Default: newest first
            filteredStores.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
        }

        // Calculate pagination
        int totalStores = filteredStores.size();
        int totalPages = (int) Math.ceil((double) totalStores / size);
        int currentPage = page;

        // Ensure page is within valid range
        if (currentPage < 0) currentPage = 0;
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;

        // Get paginated stores
        int startIndex = currentPage * size;
        int endIndex = Math.min(startIndex + size, totalStores);

        List<Store> paginatedStores = filteredStores.subList(startIndex, endIndex);

        // Count stores by status for filter tabs
        long pendingCount = storeRepository.countByStatus("PENDING");
        long approvedCount = storeRepository.countByStatus("ACTIVE");
        long rejectedCount = storeRepository.countByStatus("REJECTED");

        // Get unique cities for filter dropdown
        List<String> cities = allStores.stream()
                .map(Store::getCity)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("pendingSellerCount", pendingCount);
        model.addAttribute("approvedSellerCount", approvedCount);
        model.addAttribute("rejectedSellerCount", rejectedCount);
        model.addAttribute("stores", paginatedStores);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeSidebar", "seller-verify");

        // Create stats object for sidebar badge
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingStores", pendingCount);
        model.addAttribute("stats", stats);

        // Pagination attributes
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalStores", totalStores);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);

        // Filter attributes
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("cities", cities);

        return "admin/seller-verify";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String users(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Set active sidebar
        model.addAttribute("activeSidebar", "users");

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");

        // Create stats object for sidebar
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingStores", pendingSellers);
        model.addAttribute("stats", stats);

        // DEBUG: List all available roles
        System.out.println("=== AVAILABLE ROLES IN DATABASE ===");
        List<com.dealxanh.app.entity.Role> allRoles = roleRepository.findAll();
        for (com.dealxanh.app.entity.Role r : allRoles) {
            System.out.println("Role ID: " + r.getRoleId() + ", Name: " + r.getName() + ", Description: " + r.getDescription());
        }
        System.out.println("=====================================");

        List<User> users;
        boolean hasSearchFilter = search != null && !search.isEmpty();

        // First filter by role
        if (role != null && !role.isEmpty()) {
            if ("buyer".equals(role)) {
                Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
                users = userRole != null ? userRepository.findByRole(userRole) : new java.util.ArrayList<>();
            } else if ("seller".equals(role)) {
                // Find users who own stores or work at stores
                List<Store> allStores = storeRepository.findAll();
                users = new java.util.ArrayList<>();

                // Add store owners
                for (Store store : allStores) {
                    if (store.getOwner() != null) {
                        users.add(store.getOwner());
                    }
                }

                // Add store staff (users who work at a store)
                for (Store store : allStores) {
                    if (store.getStaffList() != null && !store.getStaffList().isEmpty()) {
                        users.addAll(store.getStaffList());
                    }
                }

                // Remove duplicates
                users = users.stream().distinct().collect(Collectors.toList());

                System.out.println("=== SELLER FILTER DEBUG ===");
                System.out.println("Total stores in DB: " + allStores.size());
                System.out.println("Total sellers (owners + staff): " + users.size());
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

        // Then filter by active status for all roles
        if ("active".equals(status)) {
            users = users.stream()
                .filter(User::getActive)
                .collect(Collectors.toList());
            System.out.println("After active filter: " + users.size() + " users");
        } else if ("inactive".equals(status)) {
            users = users.stream()
                .filter(u -> !u.getActive())
                .collect(Collectors.toList());
            System.out.println("After inactive filter: " + users.size() + " users");
        }

        // Apply search filter (for all roles)
        if (hasSearchFilter) {
            final String searchLower = search.toLowerCase();
            users = users.stream()
                .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(searchLower)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)) ||
                           (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower)) ||
                           (u.getPhone() != null && u.getPhone().contains(searchLower)))
                .collect(Collectors.toList());
            System.out.println("After search filter: " + users.size() + " users");
        }

        System.out.println("=== FINAL USERS COUNT: " + users.size() + " ===");
        System.out.println("========================");

        // Get all stores ONCE for both stats calculation and role determination
        List<Store> allStores = storeRepository.findAll();

        // Calculate statistics
        long totalBuyers = userRepository.countByRoleName("ROLE_USER");
        long activeBuyers = userRepository.countByRoleAndActive(true);
        long inactiveBuyers = userRepository.countByRoleAndActive(false);

        // Calculate total sellers (store owners + staff) using the same allStores list
        java.util.Set<Long> sellerIds = new java.util.HashSet<>();
        for (Store store : allStores) {
            if (store.getOwner() != null) {
                sellerIds.add(store.getOwner().getUserId());
            }
            if (store.getStaffList() != null) {
                for (User staff : store.getStaffList()) {
                    sellerIds.add(staff.getUserId());
                }
            }
        }
        long totalSellers = sellerIds.size();

        // Calculate total admins (ROLE_ADMIN + ROLE_MODERATOR)
        long totalAdmins = 0L;
        try {
            totalAdmins += userRepository.countByRoleName("ROLE_ADMIN");
            totalAdmins += userRepository.countByRoleName("ROLE_MODERATOR");
        } catch (Exception e) {
            // If roles don't exist, default to 0
        }

        // Add displayRole to each user based on their actual role AND store relationships
        Map<Long, String> displayRoles = new HashMap<>();
        Map<Long, String> roleDescriptions = new HashMap<>();

        for (User user : users) {
            String displayRole = determineDisplayRole(user, allStores);
            String roleDescription = getRoleDescription(displayRole);
            displayRoles.put(user.getUserId(), displayRole);
            roleDescriptions.put(user.getUserId(), roleDescription);

            // Debug log
            System.out.println("User: " + user.getUsername() +
                             " | DB Role: " + (user.getRole() != null ? user.getRole().getName() : "null") +
                             " | Display Role: " + displayRole);
        }

        // Pagination logic
        int totalUsers = users.size();
        int totalPages = (int) Math.ceil((double) totalUsers / size);
        int currentPage = page;

        // Ensure page is within valid range
        if (currentPage < 0) currentPage = 0;
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;
        if (totalPages == 0) currentPage = 0;

        // Get paginated users
        int startIndex = currentPage * size;
        int endIndex = Math.min(startIndex + size, totalUsers);

        List<User> paginatedUsers = new java.util.ArrayList<>();
        if (totalUsers > 0 && startIndex < totalUsers) {
            paginatedUsers = users.subList(startIndex, endIndex);
        }

        model.addAttribute("users", paginatedUsers);
        model.addAttribute("displayRoles", displayRoles);
        model.addAttribute("roleDescriptions", roleDescriptions);
        model.addAttribute("currentRole", role != null ? role : "");
        model.addAttribute("currentStatus", status != null ? status : "");
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activeSidebar", "users");
        model.addAttribute("pendingSellerCount", pendingSellers);

        // Pagination attributes
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);

        // Add statistics
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalBuyers", totalBuyers);
        userStats.put("activeBuyers", activeBuyers);
        userStats.put("inactiveBuyers", inactiveBuyers);
        userStats.put("totalSellers", totalSellers);
        userStats.put("totalAdmins", totalAdmins);
        model.addAttribute("userStats", userStats);

        return "admin/users";
    }

    @PostMapping("/users/{userId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String toggleUserActive(
            @PathVariable Long userId,
            @RequestParam String currentRole,
            @RequestParam String currentStatus,
            @RequestParam(required = false, defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
            return buildUsersRedirectUrl(currentRole, currentStatus, null, page);
        }

        // Prevent admin from deactivating themselves
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (currentUsername.equals(user.getUsername()) || currentUsername.equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể thay đổi trạng thái của chính mình");
            return buildUsersRedirectUrl(currentRole, currentStatus, null, page);
        }

        // Toggle active status
        user.setActive(!user.getActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String action = user.getActive() ? "kích hoạt" : "vô hiệu hóa";
        redirectAttributes.addFlashAttribute("success", "Đã " + action + " người dùng " + user.getUsername() + " thành công");

        return buildUsersRedirectUrl(currentRole, currentStatus, null, page);
    }

    @PostMapping("/users/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(
            @PathVariable Long userId,
            @RequestParam String currentRole,
            @RequestParam(required = false, defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
            return buildUsersRedirectUrl(currentRole, null, null, page);
        }

        // Prevent admin from deleting themselves
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (currentUsername.equals(user.getUsername()) || currentUsername.equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể xóa tài khoản của chính mình");
            return buildUsersRedirectUrl(currentRole, null, null, page);
        }

        String username = user.getUsername() != null ? user.getUsername() : user.getEmail();

        // Check if user has orders
        if (user.getOrders() != null && !user.getOrders().isEmpty()) {
            // Instead of deleting, just deactivate
            user.setActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã vô hiệu hóa người dùng " + username + " (có đơn hàng, không thể xóa hoàn toàn)");
        } else {
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng " + username + " thành công");
        }

        return buildUsersRedirectUrl(currentRole, null, null, page);
    }

    /**
     * Helper method to build redirect URL for users page with all parameters.
     */
    private String buildUsersRedirectUrl(String role, String status, String search, int page) {
        StringBuilder url = new StringBuilder("redirect:/admin/users");
        boolean hasParam = false;

        if (role != null && !role.isEmpty()) {
            url.append("?role=").append(role);
            hasParam = true;
        }

        if (status != null && !status.isEmpty()) {
            url.append(hasParam ? "&" : "?").append("status=").append(status);
            hasParam = true;
        }

        if (search != null && !search.trim().isEmpty()) {
            url.append(hasParam ? "&" : "?").append("search=").append(search.trim());
            hasParam = true;
        }

        if (page > 0) {
            url.append(hasParam ? "&" : "?").append("page=").append(page);
        }

        return url.toString();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String getUserDetail(
            @PathVariable Long userId,
            @RequestParam(required = false) String currentRole,
            Model model,
            Authentication authentication) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/admin/users";
        }

        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");

        model.addAttribute("user", user);
        model.addAttribute("currentRole", currentRole != null ? currentRole : "");
        model.addAttribute("activeSidebar", "users");
        model.addAttribute("pendingSellerCount", pendingSellers);

        return "admin/user-detail";
    }

    @GetMapping("/dispute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String disputes(Model model, Authentication authentication) {
        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Placeholder for disputes page
        model.addAttribute("activeSidebar", "dispute");
        return "admin/dispute";
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String finance(Model model, Authentication authentication) {
        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Placeholder for finance page
        model.addAttribute("activeSidebar", "finance");
        return "admin/finance";
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String orders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model, Authentication authentication) {

        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Set active sidebar
        model.addAttribute("activeSidebar", "orders");

        // Get all stores that have orders
        java.util.List<Store> allStores = orderRepository.findAllStoresWithOrders();

        // Apply filters
        java.util.List<Store> filteredStores = allStores.stream()
                .filter(store -> {
                    // Filter by city
                    if (city != null && !city.trim().isEmpty() && !city.equals("Tất cả thành phố")) {
                        if (store.getCity() == null || !store.getCity().equals(city)) {
                            return false;
                        }
                    }
                    // Filter by search
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase().trim();
                        if (store.getStoreName() == null || !store.getStoreName().toLowerCase().contains(searchLower)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        // Calculate stats for each store
        java.util.List<java.util.Map<String, Object>> storeStats = filteredStores.stream()
                .map(store -> {
                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("storeId", store.getStoreId());
                    stats.put("storeName", store.getStoreName());
                    stats.put("logoUrl", store.getLogoUrl());
                    stats.put("city", store.getCity());
                    stats.put("district", store.getDistrict());

                    // Order statistics
                    long totalOrders = orderRepository.countByStoreStoreId(store.getStoreId());
                    Double revenue = orderRepository.sumCompletedRevenueByStore(store.getStoreId());
                    long pendingOrders = orderRepository.countPendingOrdersByStore(store.getStoreId());

                    stats.put("totalOrders", totalOrders);
                    stats.put("revenue", revenue != null ? revenue : 0.0);
                    stats.put("pendingOrders", pendingOrders);

                    return stats;
                })
                .collect(java.util.stream.Collectors.toList());

        // Sort by revenue descending
        storeStats.sort((a, b) -> {
            Double revenueA = (Double) a.get("revenue");
            Double revenueB = (Double) b.get("revenue");
            return revenueB.compareTo(revenueA);
        });

        // Pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, storeStats.size());
        java.util.List<java.util.Map<String, Object>> paginatedStats =
            (startIndex < storeStats.size())
                ? storeStats.subList(startIndex, endIndex)
                : new java.util.ArrayList<>();

        int totalPages = (int) Math.ceil((double) storeStats.size() / size);

        model.addAttribute("stores", paginatedStats);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalStores", storeStats.size());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);

        // Filter attributes
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("selectedCity", city != null ? city : "");

        // Get all cities for filter
        java.util.List<String> cities = allStores.stream()
                .map(Store::getCity)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("cities", cities);

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingSellers);

        // Create stats object for sidebar
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pendingStores", pendingSellers);
        model.addAttribute("stats", stats);

        return "admin/orders";
    }

    @GetMapping("/orders/shop/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String getShopOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, Authentication authentication) {

        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Get store
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return "redirect:/admin/orders";
        }

        // Get orders with pagination
        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());

        org.springframework.data.domain.Page<Order> ordersPage;
        if (status != null && !status.isEmpty()) {
            ordersPage = orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable);
        } else {
            ordersPage = orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, pageable);
        }

        model.addAttribute("store", store);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalOrders", ordersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", ordersPage.hasPrevious());
        model.addAttribute("hasNext", ordersPage.hasNext());
        model.addAttribute("currentStatus", status != null ? status : "");

        return "admin/order-detail :: orders-table";
    }

    @PostMapping("/seller-verify/{storeId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String approveStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) String currentStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes) {

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return buildRedirectUrl(currentStatus, search, city, sort, page);
        }

        if (!"PENDING".equals(store.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Cửa hàng này đã được xử lý rồi");
            return buildRedirectUrl(currentStatus, search, city, sort, page);
        }

        // Get current user info
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User reviewer = userRepository.findByUsername(currentUsername)
                .orElse(userRepository.findByEmail(currentUsername).orElse(null));

        // Update store status
        store.setStatus("ACTIVE");
        store.setApprovedBy(reviewer);
        store.setReviewedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);

        redirectAttributes.addFlashAttribute("success", "Đã duyệt cửa hàng " + store.getStoreName() + " thành công");
        return buildRedirectUrl(currentStatus, search, city, sort, page);
    }

    private String buildRedirectUrl(String status, String search, String city, String sort, Integer page) {
        StringBuilder url = new StringBuilder("redirect:/admin/seller-verify");
        boolean hasParam = false;

        if (status != null && !status.isEmpty()) {
            url.append("?status=").append(status);
            hasParam = true;
        }

        if (search != null && !search.trim().isEmpty()) {
            url.append(hasParam ? "&" : "?").append("search=").append(search.trim());
            hasParam = true;
        }

        if (city != null && !city.trim().isEmpty()) {
            url.append(hasParam ? "&" : "?").append("city=").append(city.trim());
            hasParam = true;
        }

        if (sort != null && !sort.trim().isEmpty()) {
            url.append(hasParam ? "&" : "?").append("sort=").append(sort.trim());
            hasParam = true;
        }

        if (page != null && page > 0) {
            url.append(hasParam ? "&" : "?").append("page=").append(page);
        }

        return url.toString();
    }

    @PostMapping("/seller-verify/{storeId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String rejectStore(
            @PathVariable Long storeId,
            @RequestParam String rejectionReason,
            @RequestParam(required = false) String currentStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes) {

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return buildRedirectUrl(currentStatus, search, city, sort, page);
        }

        if (!"PENDING".equals(store.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Cửa hàng này đã được xử lý rồi");
            return buildRedirectUrl(currentStatus, search, city, sort, page);
        }

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do từ chối");
            return buildRedirectUrl(currentStatus, search, city, sort, page);
        }

        // Get current user info
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User reviewer = userRepository.findByUsername(currentUsername)
                .orElse(userRepository.findByEmail(currentUsername).orElse(null));

        // Update store status
        store.setStatus("REJECTED");
        store.setRejectedBy(reviewer);
        store.setRejectionReason(rejectionReason.trim());
        store.setReviewedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);

        redirectAttributes.addFlashAttribute("success", "Đã từ chối cửa hàng " + store.getStoreName() + " thành công");
        return buildRedirectUrl(currentStatus, search, city, sort, page);
    }

    /**
     * Determines the display role for a user based on their database role AND store relationships.
     * This ensures that sellers (store owners/staff) are correctly identified even if their
     * database role is just ROLE_USER.
     */
    private String determineDisplayRole(User user, List<Store> allStores) {
        // First check database role for admin roles
        if (user.getRole() != null) {
            String roleName = user.getRole().getName();
            if ("ROLE_ADMIN".equals(roleName)) {
                return "ROLE_ADMIN";
            }
            if ("ROLE_MODERATOR".equals(roleName)) {
                return "ROLE_MODERATOR";
            }
        }

        // Then check store relationships for seller roles
        for (Store store : allStores) {
            // Check if user is store owner
            if (store.getOwner() != null && store.getOwner().getUserId().equals(user.getUserId())) {
                return "ROLE_STORE_OWNER";
            }
            // Check if user is store staff
            if (store.getStaffList() != null) {
                for (User staff : store.getStaffList()) {
                    if (staff.getUserId().equals(user.getUserId())) {
                        return "ROLE_STORE_STAFF";
                    }
                }
            }
        }

        // Default to buyer role
        return "ROLE_USER";
    }

    /**
     * Gets the Vietnamese description for a display role.
     */
    private String getRoleDescription(String displayRole) {
        switch (displayRole) {
            case "ROLE_ADMIN":
                return "Admin";
            case "ROLE_MODERATOR":
                return "Moderator";
            case "ROLE_STORE_OWNER":
                return "Chủ cửa hàng";
            case "ROLE_STORE_STAFF":
                return "Nhân viên";
            case "ROLE_USER":
            default:
                return "Buyer";
        }
    }

    // ============== PROFILE MANAGEMENT ==============

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String profile(Model model, Authentication authentication) {
        // Get current admin user
        String username = authentication.getName();
        User adminUser = userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Set active sidebar
        model.addAttribute("activeSidebar", "profile");

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");

        // Create stats object for sidebar
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingStores", pendingSellers);
        model.addAttribute("stats", stats);

        return "admin/profile";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String avatarUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser != null) {
            adminUser.setFullName(fullName);
            adminUser.setPhone(phone);
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                adminUser.setAvatarUrl(avatarUrl);
            }
            userRepository.save(adminUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/profile";
        }

        // Validate current password
        if (!adminUser.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/admin/profile";
        }

        // Validate new password
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/admin/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/admin/profile";
        }

        // Update password
        adminUser.setPassword(newPassword);
        userRepository.save(adminUser);

        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/admin/profile";
    }
}
