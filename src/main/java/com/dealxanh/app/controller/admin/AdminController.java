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
            Model model) {

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
            Model model) {

        // Get pending sellers count for sidebar badge
        long pendingSellers = storeRepository.countByStatus("PENDING");

        List<User> users;
        boolean hasSearchFilter = search != null && !search.isEmpty();

        // First filter by role
        if (role != null && !role.isEmpty()) {
            if ("buyer".equals(role)) {
                Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);
                users = userRole != null ? userRepository.findByRole(userRole) : new java.util.ArrayList<>();

                // Then filter by active status if specified
                if ("active".equals(status)) {
                    users = users.stream()
                        .filter(User::getActive)
                        .collect(Collectors.toList());
                } else if ("inactive".equals(status)) {
                    users = users.stream()
                        .filter(u -> !u.getActive())
                        .collect(Collectors.toList());
                }
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

        // Apply search filter
        if (hasSearchFilter && "buyer".equals(role)) {
            final String searchLower = search.toLowerCase();
            users = users.stream()
                .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(searchLower)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)) ||
                           (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }

        // Calculate statistics
        long totalBuyers = userRepository.countByRoleName("ROLE_USER");
        long activeBuyers = userRepository.countByRoleAndActive(true);
        long inactiveBuyers = userRepository.countByRoleAndActive(false);

        model.addAttribute("users", users);
        model.addAttribute("currentRole", role != null ? role : "");
        model.addAttribute("currentStatus", status != null ? status : "");
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activeSidebar", "users");
        model.addAttribute("pendingSellerCount", pendingSellers);

        // Add statistics
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalBuyers", totalBuyers);
        userStats.put("activeBuyers", activeBuyers);
        userStats.put("inactiveBuyers", inactiveBuyers);
        model.addAttribute("userStats", userStats);

        return "admin/users";
    }

    @PostMapping("/users/{userId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String toggleUserActive(
            @PathVariable Long userId,
            @RequestParam String currentRole,
            @RequestParam String currentStatus,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
            return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "");
        }

        // Prevent admin from deactivating themselves
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (currentUsername.equals(user.getUsername()) || currentUsername.equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể thay đổi trạng thái của chính mình");
            return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "");
        }

        // Toggle active status
        user.setActive(!user.getActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String action = user.getActive() ? "kích hoạt" : "vô hiệu hóa";
        redirectAttributes.addFlashAttribute("success", "Đã " + action + " người dùng " + user.getUsername() + " thành công");

        return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "") +
               (currentStatus != null && !currentStatus.isEmpty() ? "&status=" + currentStatus : "");
    }

    @PostMapping("/users/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(
            @PathVariable Long userId,
            @RequestParam String currentRole,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại");
            return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "");
        }

        // Prevent admin from deleting themselves
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (currentUsername.equals(user.getUsername()) || currentUsername.equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể xóa tài khoản của chính mình");
            return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "");
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

        return "redirect:/admin/users?role=" + (currentRole != null ? currentRole : "");
    }

    @GetMapping("/dispute")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String disputes(Model model) {
        // Placeholder for disputes page
        model.addAttribute("activeSidebar", "dispute");
        return "admin/dispute";
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
}
