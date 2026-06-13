package com.dealxanh.app.controller.admin;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.CartItem;
import com.dealxanh.app.entity.DealCategory;
import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.repository.DealRepository;
import com.dealxanh.app.repository.DealCategoryRepository;
import com.dealxanh.app.repository.DealProductRepository;
import com.dealxanh.app.repository.CategoryRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.DisputeRepository;
import com.dealxanh.app.repository.TransactionRepository;
import com.dealxanh.app.service.DealService;
import com.dealxanh.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private DealRepository dealRepository;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private DealCategoryRepository dealCategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private DealService dealService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.dealxanh.app.service.EmailService emailService;

    @Autowired
    private com.dealxanh.app.service.NotificationService notificationService;

    @Autowired
    private com.dealxanh.app.repository.CartItemRepository cartItemRepository;

    @Autowired
    private com.dealxanh.app.repository.AppConfigRepository appConfigRepository;

    @Autowired
    private com.dealxanh.app.security.MaintenanceInterceptor maintenanceInterceptor;

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

        // Maintenance mode status for sidebar toggle
        try {
            com.dealxanh.app.entity.AppConfig maintenanceConfig =
                appConfigRepository.findByConfigKey("maintenance_mode").orElse(null);
            model.addAttribute("maintenanceMode",
                maintenanceConfig != null && "true".equalsIgnoreCase(maintenanceConfig.getConfigValue()));

            com.dealxanh.app.entity.AppConfig messageConfig =
                appConfigRepository.findByConfigKey("maintenance_message").orElse(null);
            model.addAttribute("maintenanceMessage",
                messageConfig != null ? messageConfig.getConfigValue() : "");

            com.dealxanh.app.entity.AppConfig endTimeConfig =
                appConfigRepository.findByConfigKey("maintenance_end_time").orElse(null);
            model.addAttribute("maintenanceEndTime",
                endTimeConfig != null ? endTimeConfig.getConfigValue() : "");
        } catch (Exception e) {
            model.addAttribute("maintenanceMode", false);
            model.addAttribute("maintenanceMessage", "");
            model.addAttribute("maintenanceEndTime", "");
        }

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
                // DB có thể lưu "USER" hoặc "ROLE_USER"
                Role userRole = findRoleByName("ROLE_USER", "USER");
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
        Map<Long, String> qualityLabels = new HashMap<>();
        Map<Long, Double> storeRatings = new HashMap<>();
        Map<Long, Integer> storeTotalReviews = new HashMap<>();
        int autoDisabledCount = 0;

        for (User user : users) {
            String displayRole = determineDisplayRole(user, allStores);
            String roleDescription = getRoleDescription(displayRole);
            displayRoles.put(user.getUserId(), displayRole);
            roleDescriptions.put(user.getUserId(), roleDescription);

            // Build store quality info for sellers
            if ("ROLE_STORE_OWNER".equals(displayRole) || "ROLE_STORE_STAFF".equals(displayRole)) {
                Store userStore = findStoreForUser(user, displayRole, allStores);
                if (userStore != null) {
                    double rating = userStore.getAverageRating() != null ? userStore.getAverageRating() : 0;
                    int reviewCount = userStore.getTotalReviews() != null ? userStore.getTotalReviews() : 0;
                    storeRatings.put(user.getUserId(), rating);
                    storeTotalReviews.put(user.getUserId(), reviewCount);
                    qualityLabels.put(user.getUserId(), getQualityLabel(rating, reviewCount));

                    // Auto-disable seller if: total reviews >= 40 AND average rating <= 2.5
                    if (reviewCount >= 40 && rating <= 2.5 && user.getActive()) {
                        user.setActive(false);
                        user.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(user);
                        autoDisabledCount++;
                        System.out.println("AUTO-DISABLED: User " + user.getUsername() +
                                         " | Store: " + userStore.getStoreName() +
                                         " | Rating: " + rating +
                                         " | Reviews: " + reviewCount);
                    }
                }
            }

            // Debug log
            System.out.println("User: " + user.getUsername() +
                             " | DB Role: " + (user.getRole() != null ? user.getRole().getName() : "null") +
                             " | Display Role: " + displayRole);
        }

        if (autoDisabledCount > 0) {
            model.addAttribute("warning", "Đã tự động vô hiệu " + autoDisabledCount + " người dùng do chất lượng thực phẩm và dịch vụ không đảm bảo (đánh giá >= 40, rating <= 2.5 sao)");
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
        model.addAttribute("qualityLabels", qualityLabels);
        model.addAttribute("storeRatings", storeRatings);
        model.addAttribute("storeTotalReviews", storeTotalReviews);
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

    @PostMapping("/users/{userId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String editUser(
            @PathVariable Long userId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam(required = false) String address,
            @RequestParam String currentRole,
            @RequestParam String currentStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
            return buildUsersRedirectUrl(currentRole, currentStatus, search, page);
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin người dùng " + user.getUsername() + " thành công");
        return buildUsersRedirectUrl(currentRole, currentStatus, search, page);
    }

    @GetMapping("/api/users/{userId}/edit-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public Map<String, Object> getUserEditData(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Map.of("success", false, "message", "Người dùng không tồn tại");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("address", user.getAddress());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("active", user.getActive());
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        data.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);

        // Database role
        if (user.getRole() != null) {
            data.put("roleName", user.getRole().getName());
        }

        // Get store info if user is a seller (owner or staff)
        List<Store> allStores = storeRepository.findAll();
        String displayRole = determineDisplayRole(user, allStores);
        data.put("displayRole", displayRole);
        data.put("roleDescription", getRoleDescription(displayRole));

        // Find store for this seller
        Store userStore = null;
        if ("ROLE_STORE_OWNER".equals(displayRole)) {
            for (Store store : allStores) {
                if (store.getOwner() != null && store.getOwner().getUserId().equals(user.getUserId())) {
                    userStore = store;
                    break;
                }
            }
        } else if ("ROLE_STORE_STAFF".equals(displayRole)) {
            userStore = user.getWorkStore();
        }

        if (userStore != null) {
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("storeId", userStore.getStoreId());
            storeData.put("storeName", userStore.getStoreName());
            storeData.put("description", userStore.getDescription());
            storeData.put("address", userStore.getAddress());
            storeData.put("phone", userStore.getPhone());
            storeData.put("status", userStore.getStatus());
            storeData.put("businessType", userStore.getBusinessType());
            storeData.put("categories", userStore.getCategories());
            storeData.put("city", userStore.getCity());
            storeData.put("district", userStore.getDistrict());
            double rating = userStore.getAverageRating() != null ? userStore.getAverageRating() : 0;
            int reviewCount = userStore.getTotalReviews() != null ? userStore.getTotalReviews() : 0;
            storeData.put("averageRating", rating);
            storeData.put("totalReviews", reviewCount);
            storeData.put("logoUrl", userStore.getLogoUrl());
            storeData.put("createdAt", userStore.getCreatedAt() != null ? userStore.getCreatedAt().toString() : null);

            // Quality label
            String qualityLabel = getQualityLabel(rating, reviewCount);
            if (qualityLabel != null) {
                storeData.put("qualityLabel", qualityLabel);
                storeData.put("qualityText", getQualityDisplayText(qualityLabel));
            }
            data.put("store", storeData);
        }

        return data;
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
    public String disputes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model, Authentication authentication) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Dispute stats
        model.addAttribute("totalPending", disputeRepository.countPendingDisputes());
        model.addAttribute("totalReviewing", disputeRepository.countReviewingDisputes());
        model.addAttribute("totalResolvedRefund", disputeRepository.countResolvedRefundDisputes());
        model.addAttribute("totalResolvedRejected", disputeRepository.countResolvedRejectedDisputes());
        model.addAttribute("totalDisputes", disputeRepository.count());

        // Fetch disputes
        java.util.List<com.dealxanh.app.entity.Dispute> disputes;
        if (status != null && !status.isEmpty()) {
            disputes = disputeRepository.findByStatusWithComplainant(status);
        } else {
            disputes = disputeRepository.findAllWithComplainant();
        }

        // Search filter
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase().trim();
            disputes = disputes.stream()
                .filter(d -> (d.getComplainant() != null && d.getComplainant().getFullName() != null
                        && d.getComplainant().getFullName().toLowerCase().contains(s))
                    || (d.getReason() != null && d.getReason().toLowerCase().contains(s))
                    || (d.getDescription() != null && d.getDescription().toLowerCase().contains(s)))
                .collect(java.util.stream.Collectors.toList());
        }

        // Pagination
        int totalItems = disputes.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        int from = page * size;
        int to = Math.min(from + size, totalItems);
        List<com.dealxanh.app.entity.Dispute> paged = disputes.subList(from, to);

        model.addAttribute("disputes", paged);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("currentStatus", status != null ? status : "");
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activeSidebar", "dispute");

        long pendingSellers = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingSellers);

        return "admin/dispute";
    }

    @GetMapping("/api/dispute/{disputeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getDisputeDetail(@PathVariable Long disputeId) {
        com.dealxanh.app.entity.Dispute dispute = disputeRepository.findByIdWithDetails(disputeId);
        if (dispute == null) {
            return java.util.Map.of("success", false, "message", "Không tìm thấy khiếu nại");
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("disputeId", dispute.getDisputeId());
        data.put("reason", dispute.getReason());
        data.put("description", dispute.getDescription());
        data.put("status", dispute.getStatus());
        data.put("adminNote", dispute.getAdminNote());
        data.put("evidenceUrl", dispute.getEvidenceUrl());
        data.put("createdAt", dispute.getCreatedAt() != null ? dispute.getCreatedAt().toString() : null);
        data.put("updatedAt", dispute.getUpdatedAt() != null ? dispute.getUpdatedAt().toString() : null);

        if (dispute.getComplainant() != null) {
            data.put("userName", dispute.getComplainant().getFullName() != null
                    ? dispute.getComplainant().getFullName() : dispute.getComplainant().getUsername());
            data.put("userEmail", dispute.getComplainant().getEmail());
            data.put("userPhone", dispute.getComplainant().getPhone());
        }

        if (dispute.getOrder() != null) {
            data.put("orderId", dispute.getOrder().getOrderId());
            data.put("orderAmount", dispute.getOrder().getFinalAmount());
            data.put("orderStatus", dispute.getOrder().getStatus());
        }

        return java.util.Map.of("success", true, "data", data);
    }

    @PostMapping("/api/dispute/{disputeId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> resolveDispute(
            @PathVariable Long disputeId,
            @RequestParam String action, // "approve" or "reject"
            @RequestParam(required = false) String adminNote) {

        try {
            com.dealxanh.app.entity.Dispute dispute = disputeRepository.findById(disputeId).orElse(null);
            if (dispute == null) {
                return java.util.Map.of("success", false, "message", "Không tìm thấy khiếu nại");
            }

            if (!"PENDING".equals(dispute.getStatus()) && !"REVIEWING".equals(dispute.getStatus())) {
                return java.util.Map.of("success", false, "message", "Khiếu nại này đã được xử lý");
            }

            if ("approve".equals(action)) {
                dispute.setStatus("RESOLVED_REFUND");
                dispute.setAdminNote(adminNote);
            } else if ("reject".equals(action)) {
                dispute.setStatus("RESOLVED_REJECTED");
                dispute.setAdminNote(adminNote);
            } else {
                return java.util.Map.of("success", false, "message", "Hành động không hợp lệ");
            }

            dispute.setUpdatedAt(LocalDateTime.now());
            disputeRepository.save(dispute);

            return java.util.Map.of("success", true,
                    "message", "approve".equals(action) ? "Đã phê duyệt hoàn tiền" : "Đã từ chối khiếu nại");
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/api/dispute/{disputeId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> startReviewDispute(@PathVariable Long disputeId) {
        try {
            com.dealxanh.app.entity.Dispute dispute = disputeRepository.findById(disputeId).orElse(null);
            if (dispute == null) {
                return java.util.Map.of("success", false, "message", "Không tìm thấy khiếu nại");
            }
            dispute.setStatus("REVIEWING");
            dispute.setUpdatedAt(LocalDateTime.now());
            disputeRepository.save(dispute);
            return java.util.Map.of("success", true, "message", "Đã chuyển sang trạng thái đang xem xét");
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    // ========== ANALYTICS (Báo cáo & Phân tích) ==========

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String analytics(Model model, Authentication authentication) {
        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
        if (adminUser != null) model.addAttribute("adminUser", adminUser);

        model.addAttribute("activeSidebar", "analytics");
        long pendingSellers = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingSellers);
        return "admin/analytics";
    }

    @GetMapping("/api/analytics/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getAnalyticsOverview(
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        LocalDateTime prevFrom = from.minusDays(days);

        // Current period
        Double revenue = orderRepository.sumRevenueByPeriod(from, now);
        Long orders = orderRepository.totalOrdersBetween(from, now);
        Double avgOrder = orderRepository.averageOrderValueByPeriod(from, now);
        Long completedPickups = orderRepository.countCompletedPickups(from, now);
        Long cancelled = orderRepository.countCancelledOrders(from, now);
        Double commission = transactionRepository.sumCommission(from, now);

        // Previous period (for comparison)
        Double prevRevenue = orderRepository.sumRevenueByPeriod(prevFrom, from);
        Long prevOrders = orderRepository.totalOrdersBetween(prevFrom, from);

        // Deal stats
        Long activeDeals = dealRepository.countByStatus("ACTIVE");
        Long scheduledDeals = dealRepository.countByStatus("SCHEDULED");

        // Store & User stats
        long totalStores = storeRepository.count();
        long activeStores = storeRepository.countByStatus("ACTIVE");
        long totalBuyers = userRepository.countByRoleName("ROLE_USER");

        // Calculate rates
        double pickupRate = (orders != null && orders > 0) ? ((double) (completedPickups != null ? completedPickups : 0) / orders * 100) : 0;
        double cancelRate = (orders != null && orders > 0) ? ((double) (cancelled != null ? cancelled : 0) / orders * 100) : 0;

        // Revenue change
        double revenueChange = (prevRevenue != null && prevRevenue > 0 && revenue != null)
                ? ((revenue - prevRevenue) / prevRevenue * 100) : 0;
        double orderChange = (prevOrders != null && prevOrders > 0 && orders != null)
                ? ((double)(orders - prevOrders) / prevOrders * 100) : 0;

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("revenue", revenue != null ? revenue : 0);
        result.put("orders", orders != null ? orders : 0);
        result.put("avgOrder", avgOrder != null ? avgOrder : 0);
        result.put("commission", commission != null ? commission : 0);
        result.put("activeDeals", activeDeals);
        result.put("scheduledDeals", scheduledDeals);
        result.put("totalStores", totalStores);
        result.put("activeStores", activeStores);
        result.put("totalBuyers", totalBuyers);
        result.put("pickupRate", Math.round(pickupRate * 10) / 10.0);
        result.put("cancelRate", Math.round(cancelRate * 10) / 10.0);
        result.put("revenueChange", Math.round(revenueChange * 10) / 10.0);
        result.put("orderChange", Math.round(orderChange * 10) / 10.0);
        return result;
    }

    @GetMapping("/api/analytics/chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getAnalyticsChart(
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime now = LocalDateTime.now();
        java.util.List<java.util.Map<String, Object>> chartData = new java.util.ArrayList<>();
        double maxRevenue = 1;

        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate date = now.toLocalDate().minusDays(i);
            Double dayRevenue = orderRepository.sumRevenueByDate(date);
            Long dayOrders = orderRepository.countOrdersByDate(date);
            java.util.Map<String, Object> point = new java.util.HashMap<>();
            point.put("date", date.toString());
            point.put("label", date.getDayOfMonth() + "/" + date.getMonthValue());
            point.put("revenue", dayRevenue != null ? dayRevenue : 0);
            point.put("orders", dayOrders != null ? dayOrders : 0);
            chartData.add(point);
            if (dayRevenue != null && dayRevenue > maxRevenue) maxRevenue = dayRevenue;
        }

        return java.util.Map.of("success", true, "data", chartData, "maxRevenue", maxRevenue);
    }

    @GetMapping("/api/analytics/top-stores")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getTopStores(
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        java.util.List<Object[]> topStores = orderRepository.topStoresByRevenue(from, now);
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

        int rank = 1;
        for (Object[] row : topStores) {
            com.dealxanh.app.entity.Store store = (com.dealxanh.app.entity.Store) row[0];
            Double revenue = (Double) row[1];
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("rank", rank++);
            item.put("storeName", store.getStoreName());
            item.put("storeId", store.getStoreId());
            item.put("revenue", revenue);
            item.put("status", store.getStatus());
            item.put("rating", store.getAverageRating());
            result.add(item);
            if (result.size() >= 10) break; // Top 10
        }

        return java.util.Map.of("success", true, "data", result);
    }

    @GetMapping("/api/analytics/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getRecommendations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        java.util.List<java.util.Map<String, Object>> recommendations = new java.util.ArrayList<>();

        // 1. Check pending disputes
        Long pendingDisputes = disputeRepository.countPendingDisputes();
        if (pendingDisputes > 0) {
            recommendations.add(java.util.Map.of(
                "priority", "high", "icon", "⚠️", "title", "Khiếu nại chưa xử lý",
                "detail", "Có " + pendingDisputes + " khiếu nại đang chờ. Xử lý sớm để giữ uy tín.",
                "action", "Xem ngay", "link", "/admin/dispute?status=PENDING"
            ));
        }

        // 2. Check cancellation rate
        Long cancelled = orderRepository.countCancelledOrders(monthStart, now);
        Long totalOrders = orderRepository.totalOrdersBetween(monthStart, now);
        if (totalOrders != null && totalOrders > 0) {
            double cRate = (double) cancelled / totalOrders * 100;
            if (cRate > 15) {
                recommendations.add(java.util.Map.of(
                    "priority", "high", "icon", "📉", "title", "Tỷ lệ huỷ đơn cao: " + Math.round(cRate) + "%",
                    "detail", "Tỷ lệ huỷ đơn tháng này cao bất thường. Kiểm tra chất lượng SP & dịch vụ.",
                    "action", "Xem orders", "link", "/admin/orders"
                ));
            }
        }

        // 3. Check stores needing approval
        long pendingStores = storeRepository.countByStatus("PENDING");
        if (pendingStores > 0) {
            recommendations.add(java.util.Map.of(
                "priority", "high", "icon", "🏪", "title", pendingStores + " cửa hàng chờ duyệt",
                "detail", "Duyệt cửa hàng mới để mở rộng hệ thống.",
                "action", "Duyệt ngay", "link", "/admin/seller-verify?status=pending"
            ));
        }

        // 4. Check deals expiring soon
        LocalDateTime soon = now.plusDays(3);
        java.util.List<com.dealxanh.app.entity.Deal> expiringDeals = dealRepository.findExpiringSoon(now, soon);
        if (expiringDeals != null && !expiringDeals.isEmpty()) {
            recommendations.add(java.util.Map.of(
                "priority", "medium", "icon", "⏰", "title", expiringDeals.size() + " deal sắp hết hạn trong 3 ngày",
                "detail", "Tạo deal mới để duy trì lượng khách hàng. Gia hạn deal nếu cần.",
                "action", "Xem deals", "link", "/admin/deals"
            ));
        }

        // 5. Check products pending approval
        long pendingProducts = productRepository.countByDeletedFalseAndApprovalStatus("PENDING");
        if (pendingProducts > 0) {
            recommendations.add(java.util.Map.of(
                "priority", "medium", "icon", "📦", "title", pendingProducts + " sản phẩm chờ duyệt",
                "detail", "Duyệt sản phẩm để seller có thể tạo deal.",
                "action", "Xem SP", "link", "/admin/products?approval=PENDING"
            ));
        }

        // 6. GMV performance insight
        Double thisMonth = orderRepository.sumRevenueByPeriod(monthStart, now);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);
        Double lastMonth = orderRepository.sumRevenueByPeriod(lastMonthStart, lastMonthEnd);
        if (thisMonth != null && lastMonth != null && lastMonth > 0) {
            double gmvGrowth = (thisMonth - lastMonth) / lastMonth * 100;
            if (gmvGrowth < 0) {
                recommendations.add(java.util.Map.of(
                    "priority", "high", "icon", "📊", "title", "GMV giảm " + Math.round(Math.abs(gmvGrowth)) + "% so với tháng trước",
                    "detail", "Xem xét tạo thêm deal Flash Sale, tăng marketing. Phân tích nguyên nhân từ báo cáo bên dưới.",
                    "action", "Xem báo cáo", "link", "/admin/analytics"
                ));
            } else if (gmvGrowth > 20) {
                recommendations.add(java.util.Map.of(
                    "priority", "info", "icon", "🚀", "title", "GMV tăng trưởng " + Math.round(gmvGrowth) + "%!",
                    "detail", "Đà tăng trưởng tốt. Cân nhắc mở rộng thêm deal và cửa hàng.",
                    "action", "Xem chi tiết", "link", "/admin/analytics"
                ));
            }
        }

        // 7. Low-rated stores warning
        java.util.List<com.dealxanh.app.entity.Store> allStores = storeRepository.findAll();
        int lowRatedCount = 0;
        for (com.dealxanh.app.entity.Store s : allStores) {
            if (s.getTotalReviews() != null && s.getTotalReviews() >= 10
                && s.getAverageRating() != null && s.getAverageRating() <= 3.0) {
                lowRatedCount++;
            }
        }
        if (lowRatedCount > 0) {
            recommendations.add(java.util.Map.of(
                "priority", "medium", "icon", "⭐", "title", lowRatedCount + " cửa hàng có rating thấp (<= 3.0 sao)",
                "detail", "Liên hệ cửa hàng để cải thiện chất lượng. Cân nhắc tạm dừng nếu không cải thiện.",
                "action", "Xem users", "link", "/admin/users?role=seller"
            ));
        }

        return java.util.Map.of("success", true, "data", recommendations);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approval,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
        if (adminUser != null) model.addAttribute("adminUser", adminUser);

        model.addAttribute("activeSidebar", "products");

        // Get filter URL from request to determine active tab/filters
        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Product> products;

        // Filter by approval status
        if (approval != null && !approval.isEmpty()) {
            products = productService.getProductsByApprovalStatus(approval, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        // Stats (with correct semantics)
        long totalProducts = productRepository.countByDeletedFalse();
        long activeProducts = productService.getActiveProducts();
        long expiredProducts = productRepository.countExpiredOrOutOfStock();
        model.addAttribute("products", products);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("expiredProducts", expiredProducts);
        model.addAttribute("pendingApproval", productService.getPendingApprovalProducts());
        model.addAttribute("approvedProducts", productService.getApprovedProducts());
        model.addAttribute("rejectedProducts", productService.getRejectedProducts());

        // Categories for filter dropdown
        model.addAttribute("categories", categoryRepository.findAll());

        // Filter params
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedApproval", approval);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);

        return "admin/products";
    }

    @PostMapping("/products/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String approveProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productService.approveProduct(id);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("success", "Đã duyệt sản phẩm: " + product.getName());
        return "redirect:/admin/products?approval=PENDING";
    }

    @PostMapping("/products/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String rejectProduct(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        Product product = productService.rejectProduct(id, reason);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("success", "Đã từ chối sản phẩm: " + product.getName());
        return "redirect:/admin/products?approval=PENDING";
    }

    @PostMapping("/products/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.toggleActive(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
                return "redirect:/admin/products";
            }
            String status = product.getActive() ? "kích hoạt" : "ngừng bán";
            redirectAttributes.addFlashAttribute("success", "Đã " + status + " sản phẩm: " + product.getName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm thành công");
        return "redirect:/admin/products";
    }

    @GetMapping("/api/products/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return java.util.Map.of("success", false, "message", "Không tìm thấy sản phẩm");
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("productId", product.getProductId());
        data.put("name", product.getName());
        data.put("description", product.getDescription());
        data.put("imageUrl", product.getImageUrl());
        data.put("originalPrice", product.getOriginalPrice());
        data.put("dealPrice", product.getDealPrice());
        data.put("stockQuantity", product.getStockQuantity());
        data.put("productType", product.getProductType());
        data.put("active", product.getActive());
        data.put("deleted", product.getDeleted());
        data.put("approvalStatus", product.getApprovalStatus());
        data.put("rejectionReason", product.getRejectionReason());
        data.put("createdAt", product.getCreatedAt() != null ? product.getCreatedAt().toString() : null);
        data.put("updatedAt", product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null);
        data.put("expiryDate", product.getExpiryDate() != null ? product.getExpiryDate().toString() : null);
        data.put("dealStartTime", product.getDealStartTime() != null ? product.getDealStartTime().toString() : null);
        data.put("dealEndTime", product.getDealEndTime() != null ? product.getDealEndTime().toString() : null);
        data.put("pickupDeadline", product.getPickupDeadline() != null ? product.getPickupDeadline().toString() : null);

        if (product.getStore() != null) {
            data.put("storeName", product.getStore().getStoreName());
            data.put("storeId", product.getStore().getStoreId());
        }
        if (product.getCategory() != null) {
            data.put("categoryName", product.getCategory().getName());
        }

        return java.util.Map.of("success", true, "data", data);
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String finance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, Authentication authentication) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
        if (adminUser != null) {
            model.addAttribute("adminUser", adminUser);
        }

        // Default date range: current month
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime defaultStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime defaultEnd = now;
        if (startDate != null && !startDate.isEmpty()) {
            defaultStart = LocalDateTime.parse(startDate + "T00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            defaultEnd = LocalDateTime.parse(endDate + "T23:59:59");
        }

        // Finance stats
        Double gmv = transactionRepository.sumGMV(defaultStart, defaultEnd);
        Double commission = transactionRepository.sumCommission(defaultStart, defaultEnd);
        Double paidPayouts = transactionRepository.sumPaidPayouts(defaultStart, defaultEnd);
        Long pendingCount = transactionRepository.countPendingReconciliation();
        Long pendingPayoutCount = transactionRepository.countPendingPayouts();
        Double pendingPayoutAmount = transactionRepository.sumPendingPayouts();

        model.addAttribute("activeSidebar", "finance");
        model.addAttribute("startDate", startDate != null ? startDate : now.toLocalDate().withDayOfMonth(1).toString());
        model.addAttribute("endDate", endDate != null ? endDate : now.toLocalDate().toString());
        model.addAttribute("gmv", gmv != null ? gmv : 0);
        model.addAttribute("commission", commission != null ? commission : 0);
        model.addAttribute("paidPayouts", paidPayouts != null ? paidPayouts : 0);
        model.addAttribute("pendingCount", pendingCount != null ? pendingCount : 0);
        model.addAttribute("pendingPayoutCount", pendingPayoutCount != null ? pendingPayoutCount : 0);
        model.addAttribute("pendingPayoutAmount", pendingPayoutAmount != null ? pendingPayoutAmount : 0);

        // Commission rate
        if (gmv != null && gmv > 0 && commission != null) {
            model.addAttribute("commissionRate", Math.round(commission / gmv * 1000) / 10.0);
        } else {
            model.addAttribute("commissionRate", 0.0);
        }

        // Pending payouts list
        java.util.List<com.dealxanh.app.entity.Transaction> pendingPayouts =
                transactionRepository.findPendingPayoutsWithStore();
        model.addAttribute("pendingPayouts", pendingPayouts);

        // Daily chart data (last 7 days)
        java.util.List<java.util.Map<String, Object>> chartData = new java.util.ArrayList<>();
        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = now.toLocalDate().minusDays(i);
            Double dayGmv = transactionRepository.sumGMVByDate(date);
            Double dayCommission = transactionRepository.sumCommissionByDate(date);
            java.util.Map<String, Object> dayData = new java.util.HashMap<>();
            dayData.put("label", dayLabels[date.getDayOfWeek().getValue() - 1]);
            dayData.put("date", date.toString());
            dayData.put("gmv", dayGmv != null ? dayGmv : 0);
            dayData.put("commission", dayCommission != null ? dayCommission : 0);
            chartData.add(dayData);
        }
        model.addAttribute("chartData", chartData);

        // Max GMV for chart scaling
        double maxGmv = chartData.stream().mapToDouble(d -> (double) d.get("gmv")).max().orElse(1);
        model.addAttribute("maxGmv", maxGmv > 0 ? maxGmv : 1);

        // Pending sellers count for sidebar
        long pendingSellers = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingSellers);

        return "admin/finance";
    }

    @GetMapping("/api/finance/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = (startDate != null && !startDate.isEmpty()) ? LocalDateTime.parse(startDate + "T00:00:00") : now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = (endDate != null && !endDate.isEmpty()) ? LocalDateTime.parse(endDate + "T23:59:59") : now;

        java.util.List<com.dealxanh.app.entity.Transaction> transactions = transactionRepository.findByDateRange(start, end);
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.dealxanh.app.entity.Transaction t : transactions) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("transactionId", t.getTransactionId());
            item.put("storeName", t.getStore() != null ? t.getStore().getStoreName() : "N/A");
            item.put("type", t.getType());
            item.put("amount", t.getAmount());
            item.put("platformFee", t.getPlatformFee());
            item.put("netAmount", t.getNetAmount());
            item.put("status", t.getStatus());
            item.put("description", t.getDescription());
            item.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
            result.add(item);
        }

        return java.util.Map.of("success", true, "data", result);
    }

    @GetMapping("/finance/export-csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public void exportFinanceCsv(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDateTime.parse(startDate + "T00:00:00")
                : now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDateTime.parse(endDate + "T23:59:59")
                : now;

        java.util.List<com.dealxanh.app.entity.Transaction> transactions = transactionRepository.findByDateRange(start, end);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"finance_"
                + start.toLocalDate() + "_" + end.toLocalDate() + ".csv\"");

        java.io.Writer writer = response.getWriter();
        // BOM for Excel UTF-8
        writer.write('﻿');
        writer.write("ID,Cửa hàng,Loại,Số tiền,Phí nền tảng,Số tiền thực,Trạng thái,Mô tả,Ngày tạo\n");
        for (com.dealxanh.app.entity.Transaction t : transactions) {
            writer.write(String.format("%d,\"%s\",%s,%.0f,%.0f,%.0f,%s,\"%s\",%s\n",
                    t.getTransactionId(),
                    t.getStore() != null ? t.getStore().getStoreName().replace("\"", "\"\"") : "N/A",
                    t.getType(),
                    t.getAmount() != null ? t.getAmount() : 0,
                    t.getPlatformFee() != null ? t.getPlatformFee() : 0,
                    t.getNetAmount() != null ? t.getNetAmount() : 0,
                    t.getStatus(),
                    t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "",
                    t.getCreatedAt() != null ? t.getCreatedAt().toString() : ""
            ));
        }
        writer.flush();
    }

    @PostMapping("/api/finance/payout")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public java.util.Map<String, Object> processPayout() {
        try {
            // Calculate current wallet balance
            LocalDateTime allTime = LocalDateTime.now().minusYears(10);
            LocalDateTime now = LocalDateTime.now();
            Double totalCommission = transactionRepository.sumCommission(allTime, now);
            Double totalPaidOut = transactionRepository.sumPaidPayouts(allTime, now);
            Double pendingAmount = transactionRepository.sumPendingPayouts();

            double balance = (totalCommission != null ? totalCommission : 0)
                           - (totalPaidOut != null ? totalPaidOut : 0);

            double pending = pendingAmount != null ? pendingAmount : 0;

            if (pending <= 0) {
                return java.util.Map.of("success", false, "message", "Không có khoản payout nào đang chờ xử lý.");
            }

            if (pending > balance) {
                return java.util.Map.of("success", false,
                    "message", String.format(
                        "Số dư ví không đủ. Cần %,.0fđ nhưng chỉ có %,.0fđ. Cần thêm %,.0fđ.",
                        pending, Math.max(0, balance), pending - Math.max(0, balance)));
            }

            // Get all pending payout transactions
            java.util.List<com.dealxanh.app.entity.Transaction> pendingPayouts =
                    transactionRepository.findPendingPayoutsWithStore();

            int processed = 0;
            double totalAmount = 0;

            for (com.dealxanh.app.entity.Transaction t : pendingPayouts) {
                t.setStatus("COMPLETED");
                t.setUpdatedAt(LocalDateTime.now());
                t.setDescription(t.getDescription() + " | Đã thanh toán " + now.toLocalDate());
                transactionRepository.save(t);
                processed++;
                totalAmount += t.getAmount() != null ? t.getAmount() : 0;
            }

            return java.util.Map.of(
                    "success", true,
                    "message", "Đã xử lý " + processed + " khoản payout, tổng " + String.format("%,.0fđ", totalAmount),
                    "processed", processed,
                    "totalAmount", totalAmount
            );
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    // ========== WALLET (Ví nền tảng) ==========

    @GetMapping("/api/finance/wallet")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getWallet() {
        LocalDateTime allTime = LocalDateTime.now().minusYears(10); // All time
        LocalDateTime now = LocalDateTime.now();

        Double totalGmv = transactionRepository.sumGMV(allTime, now);
        Double totalCommission = transactionRepository.sumCommission(allTime, now);
        Double totalPaidOut = transactionRepository.sumPaidPayouts(allTime, now);
        Double pendingPayout = transactionRepository.sumPendingPayouts();

        // Balance = GMV (total revenue)
        double balance = totalGmv != null ? totalGmv : 0;

        return java.util.Map.of(
                "success", true,
                "balance", Math.max(0, balance),
                "totalGmv", totalGmv != null ? totalGmv : 0,
                "totalCommission", totalCommission != null ? totalCommission : 0,
                "totalPaidOut", totalPaidOut != null ? totalPaidOut : 0,
                "pendingPayout", pendingPayout != null ? pendingPayout : 0
        );
    }

    // ========== RECONCILIATION (Đối soát) ==========

    @GetMapping("/api/finance/reconciliation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getReconciliation(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = (startDate != null && !startDate.isEmpty()) ? LocalDateTime.parse(startDate + "T00:00:00") : now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = (endDate != null && !endDate.isEmpty()) ? LocalDateTime.parse(endDate + "T23:59:59") : now;

        // Get all COMPLETED SALE transactions by store for reconciliation
        java.util.List<com.dealxanh.app.entity.Transaction> transactions =
                transactionRepository.findByTypeAndDateRange("SALE", start, end);

        // Group by store and calculate reconciliation data
        java.util.Map<Long, java.util.Map<String, Object>> storeMap = new java.util.LinkedHashMap<>();
        for (com.dealxanh.app.entity.Transaction t : transactions) {
            if (!"COMPLETED".equals(t.getStatus())) continue;
            Long storeId = t.getStore() != null ? t.getStore().getStoreId() : null;
            if (storeId == null) continue;

            storeMap.computeIfAbsent(storeId, k -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("storeId", storeId);
                m.put("storeName", t.getStore().getStoreName());
                m.put("tier", t.getStore().getPartnerTierLabel());
                m.put("commissionRate", Math.round(t.getStore().getCommissionRate() * 100) + "%");
                m.put("revenue", 0.0);
                m.put("commission", 0.0);
                m.put("netAmount", 0.0);
                m.put("orderCount", 0);
                return m;
            });

            java.util.Map<String, Object> storeData = storeMap.get(storeId);
            storeData.put("revenue", (double) storeData.get("revenue") + (t.getAmount() != null ? t.getAmount() : 0));
            storeData.put("commission", (double) storeData.get("commission") + (t.getPlatformFee() != null ? t.getPlatformFee() : 0));
            storeData.put("netAmount", (double) storeData.get("netAmount") + (t.getNetAmount() != null ? t.getNetAmount() : 0));
            storeData.put("orderCount", (int) storeData.get("orderCount") + 1);
        }

        return java.util.Map.of(
                "success", true,
                "stores", new java.util.ArrayList<>(storeMap.values()),
                "total", storeMap.size()
        );
    }

    @PostMapping("/api/finance/reconcile")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> processReconciliation(
            @RequestParam(required = false) String storeIds,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00") : now.toLocalDate().withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59") : now;

            java.util.List<com.dealxanh.app.entity.Transaction> transactions;
            if (storeIds != null && !storeIds.isEmpty()) {
                String[] ids = storeIds.split(",");
                transactions = new java.util.ArrayList<>();
                for (String idStr : ids) {
                    Long storeId = Long.parseLong(idStr.trim());
                    transactions.addAll(transactionRepository.findByStoreAndDateRange(storeId, start, end));
                }
            } else {
                transactions = transactionRepository.findByDateRange(start, end);
            }

            int reconciled = 0;
            for (com.dealxanh.app.entity.Transaction t : transactions) {
                if ("PENDING".equals(t.getStatus()) && !"PAYOUT".equals(t.getType())) {
                    // Create a confirmation transaction as reconciliation proof
                    t.setStatus("COMPLETED");
                    t.setUpdatedAt(LocalDateTime.now());
                    t.setDescription((t.getDescription() != null ? t.getDescription() : "") + " | Đã đối soát " + now.toLocalDate());
                    transactionRepository.save(t);
                    reconciled++;
                }
            }

            return java.util.Map.of(
                    "success", true,
                    "message", "Đã đối soát " + reconciled + " giao dịch thành công",
                    "reconciled", reconciled
            );
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
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

    // ============== DEALS MANAGEMENT ==============

    @GetMapping("/deals")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String deals(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "PLATFORM") String scope,
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
        model.addAttribute("activeSidebar", "deals");

        // Get all deals with details
        List<Deal> allDeals = dealRepository.findAllWithDetails();

        // Apply filters
        List<Deal> filteredDeals = allDeals.stream()
                .filter(deal -> {
                    // Filter by type
                    if (type != null && !type.trim().isEmpty() && !type.equals("ALL")) {
                        if (!type.equals(deal.getDealType())) {
                            return false;
                        }
                    }
                    // Filter by status
                    if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                        if (!status.equals(deal.getStatus())) {
                            return false;
                        }
                    }
                    // Filter by scope (platform vs store)
                    if (scope != null && !scope.trim().isEmpty() && !scope.equals("ALL")) {
                        if ("PLATFORM".equals(scope) && deal.getStore() != null) {
                            return false;
                        }
                        if ("STORE".equals(scope) && deal.getStore() == null) {
                            return false;
                        }
                    }
                    // Filter by search (name, code, store name)
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase().trim();
                        boolean nameMatch = deal.getDealName() != null && deal.getDealName().toLowerCase().contains(searchLower);
                        boolean codeMatch = deal.getDealCode() != null && deal.getDealCode().toLowerCase().contains(searchLower);
                        boolean storeMatch = deal.getStore() != null && deal.getStore().getStoreName() != null
                                && deal.getStore().getStoreName().toLowerCase().contains(searchLower);
                        if (!nameMatch && !codeMatch && !storeMatch) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        // Pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, filteredDeals.size());
        List<Deal> paginatedDeals =
            (startIndex < filteredDeals.size())
                ? filteredDeals.subList(startIndex, endIndex)
                : new java.util.ArrayList<>();

        int totalPages = (int) Math.ceil((double) filteredDeals.size() / size);

        model.addAttribute("deals", paginatedDeals);

        // Build deal categories & products maps for card display
        java.util.Map<Long, java.util.List<String>> dealCategoriesMap = new java.util.HashMap<>();
        java.util.Map<Long, java.util.List<String>> dealProductsMap = new java.util.HashMap<>();

        for (Deal deal : paginatedDeals) {
            // Categories
            java.util.List<DealCategory> dealCategories = dealCategoryRepository.findByDealAndActiveTrue(deal);
            if (dealCategories != null && !dealCategories.isEmpty()) {
                java.util.List<String> catNames = dealCategories.stream()
                        .filter(dc -> dc.getCategory() != null)
                        .map(dc -> dc.getCategory().getName())
                        .collect(java.util.stream.Collectors.toList());
                if (!catNames.isEmpty()) {
                    dealCategoriesMap.put(deal.getDealId(), catNames);
                }
            }

            // Products
            java.util.List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
            if (dealProducts != null && !dealProducts.isEmpty()) {
                java.util.List<String> prodNames = dealProducts.stream()
                        .filter(dp -> dp.getProduct() != null)
                        .map(dp -> dp.getProduct().getName())
                        .collect(java.util.stream.Collectors.toList());
                if (!prodNames.isEmpty()) {
                    dealProductsMap.put(deal.getDealId(), prodNames);
                }
            }
        }

        model.addAttribute("dealCategoriesMap", dealCategoriesMap);
        model.addAttribute("dealProductsMap", dealProductsMap);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalDeals", filteredDeals.size());
        model.addAttribute("endIndex", Math.min((page + 1) * size, filteredDeals.size()));
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);

        // Filter attributes
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("selectedType", type != null ? type : "");
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("selectedScope", scope != null ? scope : "");

        // Get deal types for filter
        List<String> dealTypes = allDeals.stream()
                .map(Deal::getDealType)
                .filter(t -> t != null)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("dealTypes", dealTypes);

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");
        model.addAttribute("pendingSellerCount", pendingSellers);

        // Create stats object for sidebar
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pendingStores", pendingSellers);
        model.addAttribute("stats", stats);

        // Statistics
        long activeDeals = dealRepository.countByStatus("ACTIVE");
        long scheduledDeals = dealRepository.countByStatus("SCHEDULED");
        model.addAttribute("activeDealsCount", activeDeals);
        model.addAttribute("scheduledDealsCount", scheduledDeals);

        return "admin/deals";
    }

    @PostMapping("/deals/{dealId}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String updateDeal(
            @PathVariable Long dealId,
            @RequestParam String dealName,
            @RequestParam String dealCode,
            @RequestParam String dealType,
            @RequestParam(required = false) String description,
            @RequestParam String discountType,
            @RequestParam Double discountValue,
            @RequestParam(required = false) Double maxDiscountAmount,
            @RequestParam Double minOrderAmount,
            @RequestParam(required = false) Long maxUsageCount,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String bannerUrl,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile bannerFile,
            @RequestParam(defaultValue = "CODE_REQUIRED") String applyMethod,
            RedirectAttributes redirectAttributes) {

        try {
            Deal existingDeal = dealService.getDealById(dealId);
            if (existingDeal == null) {
                redirectAttributes.addFlashAttribute("error", "Deal không tồn tại");
                return "redirect:/admin/deals";
            }

            String bannerPath = bannerUrl;
            if (bannerFile != null && !bannerFile.isEmpty()) {
                if (!isImageFile(bannerFile)) {
                    redirectAttributes.addFlashAttribute("error", "File banner phải là ảnh (JPG, PNG, WEBP)");
                    return "redirect:/admin/deals";
                }
                if (!isValidFileSize(bannerFile, 5 * 1024 * 1024)) {
                    redirectAttributes.addFlashAttribute("error", "File banner không được vượt quá 5MB");
                    return "redirect:/admin/deals";
                }
                bannerPath = saveUploadedFile(bannerFile);
            }
            if (bannerPath == null || bannerPath.isEmpty()) {
                bannerPath = existingDeal.getBannerUrl();
            }

            existingDeal.setDealName(dealName);
            existingDeal.setDealCode(dealCode);
            existingDeal.setDealType(dealType);
            existingDeal.setDescription(description);
            existingDeal.setDiscountType(discountType);
            existingDeal.setDiscountValue(discountValue);
            existingDeal.setMaxDiscountAmount(maxDiscountAmount);
            existingDeal.setMinOrderAmount(minOrderAmount);
            existingDeal.setMaxUsageCount(maxUsageCount);
            existingDeal.setStartTime(startTime);
            existingDeal.setEndTime(endTime);
            existingDeal.setBannerUrl(bannerPath);
            existingDeal.setApplyMethod(applyMethod);
            existingDeal.setUpdatedAt(LocalDateTime.now());

            dealService.updateDeal(dealId, existingDeal);
            redirectAttributes.addFlashAttribute("success", "Cập nhật deal thành công!");

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + ex.getMessage());
        }
        return "redirect:/admin/deals";
    }

    @PostMapping("/deals/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String createDeal(
            @RequestParam String dealName,
            @RequestParam String dealCode,
            @RequestParam String dealType,
            @RequestParam(required = false) String description,
            @RequestParam String discountType,
            @RequestParam Double discountValue,
            @RequestParam(required = false) Double maxDiscountAmount,
            @RequestParam Double minOrderAmount,
            @RequestParam(required = false) Long maxUsageCount,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam String scope,
            @RequestParam(required = false) String bannerUrl,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile bannerFile,
            @RequestParam(defaultValue = "CODE_REQUIRED") String applyMethod,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        try {
            // Get current admin user
            String username = authentication.getName();
            User adminUser = userRepository.findByUsernameWithRole(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));

            // Handle banner file upload
            String bannerPath = bannerUrl;
            if (bannerFile != null && !bannerFile.isEmpty()) {
                if (!isImageFile(bannerFile)) {
                    redirectAttributes.addFlashAttribute("error", "File banner phải là ảnh (JPG, PNG, WEBP)");
                    return "redirect:/admin/deals";
                }
                if (!isValidFileSize(bannerFile, 5 * 1024 * 1024)) { // max 5MB
                    redirectAttributes.addFlashAttribute("error", "File banner không được vượt quá 5MB");
                    return "redirect:/admin/deals";
                }
                bannerPath = saveUploadedFile(bannerFile);
            }

            // Create new deal
            Deal deal = new Deal();
            deal.setDealName(dealName);
            deal.setDealCode(dealCode);
            deal.setDealType(dealType);
            deal.setDescription(description);
            deal.setDiscountType(discountType);
            deal.setDiscountValue(discountValue);
            deal.setMaxDiscountAmount(maxDiscountAmount);
            deal.setMinOrderAmount(minOrderAmount);
            deal.setMaxUsageCount(maxUsageCount);
            deal.setUsageCount(0L);
            deal.setStartTime(startTime);
            deal.setEndTime(endTime);
            deal.setScope(scope);
            deal.setBannerUrl(bannerPath);
            deal.setApplyMethod(applyMethod);
            deal.setPriority(50);
            deal.setCreatedBy(adminUser);

            // Save deal with validation
            dealService.createDeal(deal);

            redirectAttributes.addFlashAttribute("success", "Tạo deal thành công!");
            return "redirect:/admin/deals";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/deals";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + ex.getMessage());
            return "redirect:/admin/deals";
        }
    }

    // ========== DEAL PAUSE / RESUME ==========

    @PostMapping("/deals/{dealId}/pause")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> pauseDeal(@PathVariable Long dealId) {
        Deal deal = dealService.getDealById(dealId);
        if (deal == null) return java.util.Map.of("success", false, "message", "Deal không tồn tại");
        if (!"ACTIVE".equals(deal.getStatus())) return java.util.Map.of("success", false, "message", "Chỉ có thể tạm dừng deal đang ACTIVE");
        deal.setStatus("PAUSED");
        deal.setUpdatedAt(LocalDateTime.now());
        dealRepository.save(deal);

        // Notify buyers who have products from this deal in their cart
        try {
            notifyBuyersAboutPausedDeal(deal);
        } catch (Exception e) {
            // Log lỗi nhưng không block flow chính
            System.err.println("Lỗi khi gửi thông báo pause deal: " + e.getMessage());
        }

        return java.util.Map.of("success", true, "message", "Đã tạm dừng deal");
    }

    /** Gửi thông báo cho tất cả buyer có SP từ deal bị pause trong giỏ hàng */
    private void notifyBuyersAboutPausedDeal(Deal deal) {
        // Lấy danh sách product IDs thuộc deal này
        List<DealProduct> dealProducts = dealProductRepository.findByDealDealIdOrderByPriorityAsc(deal.getDealId());
        if (dealProducts == null || dealProducts.isEmpty()) return;

        List<Long> productIds = new ArrayList<>();
        Map<Long, String> productNameMap = new HashMap<>();
        for (DealProduct dp : dealProducts) {
            if (dp.getProduct() != null) {
                productIds.add(dp.getProduct().getProductId());
                productNameMap.put(dp.getProduct().getProductId(), dp.getProduct().getName());
            }
        }
        if (productIds.isEmpty()) return;

        // Tìm tất cả CartItem có product nằm trong danh sách
        List<CartItem> cartItems = cartItemRepository.findByProductIdsWithCartAndUser(productIds);
        if (cartItems == null || cartItems.isEmpty()) return;

        // Gom nhóm theo User → danh sách tên sản phẩm bị ảnh hưởng
        Map<User, Set<String>> userAffectedProducts = new LinkedHashMap<>();
        for (CartItem ci : cartItems) {
            User buyer = ci.getCart().getUser();
            String productName = productNameMap.get(ci.getProduct().getProductId());
            if (buyer != null && productName != null) {
                userAffectedProducts
                    .computeIfAbsent(buyer, k -> new LinkedHashSet<>())
                    .add(productName);
            }
        }

        // Gửi thông báo cho từng buyer
        for (Map.Entry<User, Set<String>> entry : userAffectedProducts.entrySet()) {
            notificationService.notifyDealPaused(
                entry.getKey(),
                deal.getDealName(),
                new ArrayList<>(entry.getValue())
            );
        }
    }

    @PostMapping("/deals/{dealId}/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> resumeDeal(@PathVariable Long dealId) {
        Deal deal = dealService.getDealById(dealId);
        if (deal == null) return java.util.Map.of("success", false, "message", "Deal không tồn tại");
        if (!"PAUSED".equals(deal.getStatus())) return java.util.Map.of("success", false, "message", "Chỉ có thể kích hoạt deal đang PAUSED");
        if (deal.getEndTime() != null && deal.getEndTime().isBefore(LocalDateTime.now())) {
            return java.util.Map.of("success", false, "message", "Không thể kích hoạt: deal đã hết hạn (" + deal.getEndTime().toLocalDate() + ")");
        }
        deal.setStatus("ACTIVE");
        deal.setUpdatedAt(LocalDateTime.now());
        dealRepository.save(deal);
        // Thông báo cho buyer gần cửa hàng
        dealService.notifyNearbyBuyers(deal);
        return java.util.Map.of("success", true, "message", "Đã kích hoạt deal");
    }

    // ========== File upload helpers for deal banner ==========

    private String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) return null;
        // MD5 hash for dedup
        java.security.MessageDigest md5;
        try { md5 = java.security.MessageDigest.getInstance("MD5"); }
        catch (java.security.NoSuchAlgorithmException e) { throw new java.io.IOException("MD5 not available", e); }
        byte[] digest = md5.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        String hash = sb.toString();
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains("."))
            ext = originalName.substring(originalName.lastIndexOf("."));
        String fileName = hash + ext;
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath))
            java.nio.file.Files.createDirectories(uploadPath);
        java.nio.file.Path targetPath = uploadPath.resolve(fileName);
        if (!java.nio.file.Files.exists(targetPath))
            java.nio.file.Files.copy(file.getInputStream(), targetPath);
        return "/uploads/" + fileName;
    }

    private boolean isImageFile(org.springframework.web.multipart.MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg")
                || contentType.equals("image/png") || contentType.equals("image/webp"));
    }

    private boolean isValidFileSize(org.springframework.web.multipart.MultipartFile file, long maxSizeInBytes) {
        return file.getSize() <= maxSizeInBytes;
    }

    @GetMapping("/api/deals/{dealId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getDealData(@PathVariable Long dealId) {
        Deal deal = dealService.getDealById(dealId);
        if (deal == null) return java.util.Map.of("success", false, "message", "Deal không tồn tại");

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("dealId", deal.getDealId());
        data.put("dealName", deal.getDealName());
        data.put("dealCode", deal.getDealCode());
        data.put("dealType", deal.getDealType());
        data.put("description", deal.getDescription());
        data.put("discountType", deal.getDiscountType());
        data.put("discountValue", deal.getDiscountValue());
        data.put("maxDiscountAmount", deal.getMaxDiscountAmount());
        data.put("minOrderAmount", deal.getMinOrderAmount());
        data.put("maxUsageCount", deal.getMaxUsageCount());
        data.put("applyMethod", deal.getApplyMethod());
        data.put("startTime", deal.getStartTime() != null ? deal.getStartTime().toString().replace("T", " ").substring(0, 16) : null);
        data.put("endTime", deal.getEndTime() != null ? deal.getEndTime().toString().replace("T", " ").substring(0, 16) : null);
        data.put("bannerUrl", deal.getBannerUrl());
        return java.util.Map.of("success", true, "data", data);
    }

    // ============ DEAL CATEGORIES (Admin gán categories vào platform deals) ============

    @GetMapping("/deals/{dealId}/available-categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.List<com.dealxanh.app.entity.Category> getAvailableCategoriesForDeal(
            @PathVariable Long dealId) {
        return dealService.getAvailableCategoriesForDeal(dealId);
    }

    @PostMapping("/deals/{dealId}/assign-categories")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public java.util.Map<String, Object> assignCategoriesToDeal(
            @PathVariable Long dealId,
            @RequestParam(required = false) java.util.List<Long> categoryIds,
            @RequestParam(required = false) Integer priority) {

        try {
            int assignedCount = 0;
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Long categoryId : categoryIds) {
                    com.dealxanh.app.entity.DealCategory dealCategory =
                        dealService.addCategoryToDeal(dealId, categoryId, priority);
                    if (dealCategory != null) {
                        assignedCount++;
                    }
                }
            }

            return java.util.Map.of(
                "success", true,
                "message", "Đã gán " + assignedCount + " danh mục vào deal"
            );
        } catch (Exception e) {
            return java.util.Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            );
        }
    }

    @GetMapping("/deals/{dealId}/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getDealCategories(@PathVariable Long dealId) {
        Deal deal = dealService.getDealById(dealId);

        // For platform deals, return warnings about products nearing 30% cap
        if (deal != null && "ALL_STORES".equals(deal.getScope()) && deal.getStore() == null) {
            java.util.List<java.util.Map<String, Object>> categoriesWithWarnings =
                dealService.getDealCategoriesWithWarnings(dealId);
            return java.util.Map.of("categories", categoriesWithWarnings);
        }

        java.util.List<com.dealxanh.app.entity.DealCategory> categories =
            dealService.getDealCategories(dealId);
        return java.util.Map.of("categories", categories);
    }

    @PostMapping("/deals/{dealId}/remove-category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseBody
    public java.util.Map<String, Object> removeCategoryFromDeal(
            @PathVariable Long dealId,
            @PathVariable Long categoryId) {

        boolean removed = dealService.removeCategoryFromDeal(dealId, categoryId);

        return java.util.Map.of(
            "success", removed,
            "message", removed ? "Đã xóa danh mục khỏi deal" : "Không thể xóa danh mục"
        );
    }

    // ============ DEAL PRODUCTS (Seller gán products vào store deals) ============

    @GetMapping("/deals/{dealId}/available-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @ResponseBody
    public java.util.List<Product> getAvailableProductsForDeal(
            @PathVariable Long dealId) {

        Deal deal = dealService.getDealById(dealId);
        if (deal == null || deal.getStore() == null) {
            return java.util.List.of();
        }

        // Get products from this store that are not already in the deal
        java.util.List<Product> allStoreProducts = productRepository.findByStoreStoreIdAndDeletedFalse(deal.getStore().getStoreId());

        java.util.List<Long> existingProductIds = dealProductRepository
                .findByDeal(deal)
                .stream()
                .map(dp -> dp.getProduct().getProductId())
                .toList();

        if (existingProductIds.isEmpty()) {
            return allStoreProducts;
        }

        return allStoreProducts.stream()
                .filter(p -> !existingProductIds.contains(p.getProductId()))
                .toList();
    }

    @PostMapping("/deals/{dealId}/assign-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> assignProductsToDeal(
            @PathVariable Long dealId,
            @RequestBody java.util.List<java.util.Map<String, Object>> productsData) {

        try {
            int assignedCount = 0;
            for (java.util.Map<String, Object> data : productsData) {
                Long productId = ((Number) data.get("productId")).longValue();
                Double originalPrice = ((Number) data.get("originalPrice")).doubleValue();
                Double salePrice = ((Number) data.get("salePrice")).doubleValue();
                Integer maxQuantity = data.get("maxQuantity") != null ?
                    ((Number) data.get("maxQuantity")).intValue() : null;
                Integer priority = data.get("priority") != null ?
                    ((Number) data.get("priority")).intValue() : 0;

                DealProduct dealProduct = dealService.addProductToDeal(
                    dealId, productId, originalPrice, salePrice, maxQuantity, priority
                );

                if (dealProduct != null) {
                    assignedCount++;
                }
            }

            return java.util.Map.of(
                "success", true,
                "message", "Đã thêm " + assignedCount + " sản phẩm vào deal"
            );
        } catch (IllegalArgumentException e) {
            return java.util.Map.of(
                "success", false,
                "message", e.getMessage()
            );
        } catch (Exception e) {
            return java.util.Map.of(
                "success", false,
                "message", "Lỗi: " + e.getMessage()
            );
        }
    }

    @GetMapping("/deals/{dealId}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> getDealProducts(@PathVariable Long dealId) {
        java.util.List<DealProduct> products = dealService.getDealProducts(dealId);

        return java.util.Map.of("products", products);
    }

    @PostMapping("/deals/{dealId}/remove-product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> removeProductFromDeal(
            @PathVariable Long dealId,
            @PathVariable Long productId) {

        boolean removed = dealService.removeProductFromDeal(dealId, productId);

        return java.util.Map.of(
            "success", removed,
            "message", removed ? "Đã xóa sản phẩm khỏi deal" : "Không thể xóa sản phẩm"
        );
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

    @PostMapping("/seller-verify/{storeId}/reset-to-pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String resetStoreToPending(
            @PathVariable Long storeId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String currentStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return buildRedirectUrl(currentStatus != null ? currentStatus : "", search, city, sort, page);
        }

        if (!"REJECTED".equals(store.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể đưa cửa hàng bị từ chối về trạng thái chờ duyệt");
            return buildRedirectUrl(currentStatus != null ? currentStatus : "", search, city, sort, page);
        }

        // Save the original rejection reason for email
        String originalReason = store.getRejectionReason();

        // Update rejection reason if admin provided a new one
        if (reason != null && !reason.trim().isEmpty()) {
            store.setRejectionReason(reason.trim());
        }

        // Reset store to PENDING status and clear rejection tracking
        store.setStatus("PENDING");
        store.setRejectedBy(null);
        store.setReviewedAt(null);
        store.setUpdatedAt(LocalDateTime.now());
        storeRepository.save(store);

        // Send email notification to seller
        if (store.getOwner() != null && store.getOwner().getEmail() != null && !store.getOwner().getEmail().isEmpty()) {
            try {
                String sellerName = store.getOwner().getFullName() != null ? store.getOwner().getFullName() : store.getOwner().getUsername();
                String emailReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : originalReason;
                emailService.sendSellerReReviewEmail(store.getOwner().getEmail(), sellerName, store.getStoreName(), emailReason);
            } catch (Exception e) {
                System.err.println("Không thể gửi email xét duyệt lại: " + e.getMessage());
            }
        }

        redirectAttributes.addFlashAttribute("success", "Đã chuyển cửa hàng \"" + store.getStoreName() + "\" về trạng thái chờ xét duyệt và gửi email thông báo cho seller");
        return buildRedirectUrl(currentStatus != null ? currentStatus : "", search, city, sort, page);
    }

    /**
     * Finds a role by trying both "ROLE_XXX" and "XXX" formats (DB inconsistency).
     */
    private Role findRoleByName(String withPrefix, String withoutPrefix) {
        Role role = roleRepository.findByName(withPrefix).orElse(null);
        if (role == null) {
            role = roleRepository.findByName(withoutPrefix).orElse(null);
        }
        return role;
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

    /**
     * Finds the store associated with a user based on their display role.
     */
    private Store findStoreForUser(User user, String displayRole, List<Store> allStores) {
        if ("ROLE_STORE_OWNER".equals(displayRole)) {
            for (Store store : allStores) {
                if (store.getOwner() != null && store.getOwner().getUserId().equals(user.getUserId())) {
                    return store;
                }
            }
        } else if ("ROLE_STORE_STAFF".equals(displayRole)) {
            return user.getWorkStore();
        }
        return null;
    }

    /**
     * Determines quality label based on store rating and review count.
     * - >= 40 reviews & rating <= 2.5: Kém chất lượng (auto-disable)
     * - rating 3.0 - 4.0: Chất lượng ổn
     * - rating >= 4.5: Chất lượng tốt
     */
    private String getQualityLabel(double rating, int totalReviews) {
        if (totalReviews == 0) return null; // No reviews yet
        if (totalReviews >= 40 && rating <= 2.5) return "POOR";
        if (rating >= 4.5) return "GOOD";
        if (rating >= 3.0) return "OK";
        return "POOR"; // below 3.0 with any review count
    }

    /**
     * Gets the Vietnamese display text for a quality label.
     */
    private String getQualityDisplayText(String label) {
        if (label == null) return null;
        switch (label) {
            case "POOR": return "Chất lượng thực phẩm và dịch vụ không đảm bảo";
            case "OK": return "Chất lượng ổn";
            case "GOOD": return "Chất lượng tốt";
            default: return null;
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
            RedirectAttributes redirectAttributes,
            jakarta.servlet.http.HttpServletRequest request) {

        String username = authentication.getName();
        User adminUser = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (adminUser == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/profile";
        }

        if (!passwordEncoder.matches(currentPassword, adminUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/admin/profile";
        }

        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            return "redirect:/admin/profile";
        }
        if (newPassword.length() > 50) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không được vượt quá 50 ký tự!");
            return "redirect:/admin/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp với mật khẩu mới!");
            return "redirect:/admin/profile";
        }

        adminUser.setPassword(passwordEncoder.encode(newPassword));
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser.setWeakPassword(false);
        userRepository.save(adminUser);

        try { request.getSession().invalidate(); } catch (Exception ignored) {}
        return "redirect:/admin/login?logout=password_changed";
    }

    // ========== MAINTENANCE MODE ==========

    @PostMapping("/settings/maintenance/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> toggleMaintenance(
            @RequestParam boolean mode,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String endTime) {

        try {
            // Update maintenance mode
            com.dealxanh.app.entity.AppConfig config =
                appConfigRepository.findByConfigKey("maintenance_mode")
                    .orElse(new com.dealxanh.app.entity.AppConfig("maintenance_mode", "false"));
            config.setConfigValue(mode ? "true" : "false");
            appConfigRepository.save(config);

            // Update message if provided
            if (message != null && !message.trim().isEmpty()) {
                com.dealxanh.app.entity.AppConfig msgConfig =
                    appConfigRepository.findByConfigKey("maintenance_message")
                        .orElse(new com.dealxanh.app.entity.AppConfig("maintenance_message", ""));
                msgConfig.setConfigValue(message.trim());
                appConfigRepository.save(msgConfig);
            }

            // Update end time if provided
            if (endTime != null) {
                com.dealxanh.app.entity.AppConfig timeConfig =
                    appConfigRepository.findByConfigKey("maintenance_end_time")
                        .orElse(new com.dealxanh.app.entity.AppConfig("maintenance_end_time", ""));
                timeConfig.setConfigValue(endTime.trim());
                appConfigRepository.save(timeConfig);
            }

            // Refresh interceptor cache
            maintenanceInterceptor.refreshCache();

            return java.util.Map.of(
                "success", true,
                "message", mode ? "Đã bật chế độ bảo trì" : "Đã tắt chế độ bảo trì",
                "maintenanceMode", mode
            );
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/api/settings/maintenance/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @ResponseBody
    public java.util.Map<String, Object> getMaintenanceStatus() {
        try {
            com.dealxanh.app.entity.AppConfig config =
                appConfigRepository.findByConfigKey("maintenance_mode").orElse(null);
            com.dealxanh.app.entity.AppConfig msgConfig =
                appConfigRepository.findByConfigKey("maintenance_message").orElse(null);
            com.dealxanh.app.entity.AppConfig timeConfig =
                appConfigRepository.findByConfigKey("maintenance_end_time").orElse(null);

            return java.util.Map.of(
                "success", true,
                "maintenanceMode", config != null && "true".equalsIgnoreCase(config.getConfigValue()),
                "maintenanceMessage", msgConfig != null ? msgConfig.getConfigValue() : "",
                "maintenanceEndTime", timeConfig != null ? timeConfig.getConfigValue() : ""
            );
        } catch (Exception e) {
            return java.util.Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }
}
