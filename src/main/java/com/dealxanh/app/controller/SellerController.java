package com.dealxanh.app.controller;

import com.dealxanh.app.entity.*;
import com.dealxanh.app.repository.*;
import com.dealxanh.app.service.DealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
public class SellerController {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private DealService dealService;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.dealxanh.app.service.ProductService productService;

    @Autowired
    private com.dealxanh.app.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.dealxanh.app.repository.DealCategoryRepository dealCategoryRepository;

    @Autowired
    private com.dealxanh.app.repository.TransactionRepository transactionRepository;

    @Autowired
    private com.dealxanh.app.repository.RoleRepository roleRepository;

    // ============ DASHBOARD ============

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            return "redirect:/login";
        }

        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // KPIs
        long totalDeals = dealRepository.findByStoreStoreId(storeId).size();
        long activeDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size();
        long scheduledDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "SCHEDULED").size();
        long totalProducts = productRepository.countByStoreStoreIdAndDeletedFalse(storeId);
        long pendingOrders = orderRepository.countPendingOrdersByStore(storeId);
        long totalOrders = orderRepository.countByStoreStoreId(storeId);

        // Recent orders (last 5)
        org.springframework.data.domain.Pageable top5 =
            org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("createdAt").descending());
        var recentOrdersPage = orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, top5);
        java.util.List<com.dealxanh.app.entity.Order> recentOrders = recentOrdersPage.getContent();

        // Common model attrs for sidebar
        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("activeDeals", activeDeals);

        model.addAttribute("totalDeals", totalDeals);
        model.addAttribute("scheduledDeals", scheduledDeals);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("recentOrders", recentOrders);

        return "seller/dashboard";
    }

    // ============ MANAGE STAFF (only for store owner) ============

    @GetMapping("/manage-staff")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STORE_OWNER')")
    public String manageStaff(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // Get staff list for this store
        java.util.List<User> staffList = store.getStaffList() != null
                ? store.getStaffList().stream()
                    .filter(s -> s.getRole() != null && ("ROLE_STORE_STAFF".equals(s.getRole().getName()) || "STORE_STAFF".equals(s.getRole().getName())))
                    .toList()
                : java.util.List.of();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("staffList", staffList);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(storeId));
        return "seller/manage-staff";
    }

    @PostMapping("/manage-staff/add")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> addStaff(
            @RequestParam(defaultValue = "existing") String mode,
            @RequestParam(required = false) String usernameOrEmail,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            Principal principal) {
        User owner = getCurrentUser(principal);
        if (owner == null || owner.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Store store = owner.getWorkStore();

        if ("create".equals(mode)) {
            // ===== Create new staff account =====
            if (username == null || username.trim().isEmpty())
                return java.util.Map.of("success", false, "message", "Vui lòng nhập tên đăng nhập");
            if (password == null || password.length() < 6)
                return java.util.Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự");
            if (fullName == null || fullName.trim().isEmpty())
                return java.util.Map.of("success", false, "message", "Vui lòng nhập họ tên");

            String u = username.trim();
            if (userRepository.existsByUsername(u))
                return java.util.Map.of("success", false, "message", "Tên đăng nhập '" + u + "' đã tồn tại");
            if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email.trim()))
                return java.util.Map.of("success", false, "message", "Email '" + email.trim() + "' đã được sử dụng");

            com.dealxanh.app.entity.Role staffRole = findRoleByName("ROLE_STORE_STAFF", "STORE_STAFF");
            if (staffRole == null)
                return java.util.Map.of("success", false, "message", "Lỗi hệ thống: không tìm thấy role STAFF");

            User newStaff = new User();
            newStaff.setUsername(u);
            newStaff.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
            newStaff.setFullName(fullName.trim());
            newStaff.setEmail(email != null ? email.trim() : null);
            newStaff.setPhone(phone != null ? phone.trim() : null);
            newStaff.setRole(staffRole);
            newStaff.setWorkStore(store);
            newStaff.setActive(true);
            newStaff.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(newStaff);

            return java.util.Map.of("success", true, "message",
                    "Đã tạo tài khoản nhân viên: " + fullName.trim() + " (@" + u + ")");
        }

        // ===== Existing user mode =====
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty())
            return java.util.Map.of("success", false, "message", "Nhập username hoặc email");

        User targetUser = userRepository.findByUsername(usernameOrEmail.trim()).orElse(null);
        if (targetUser == null) targetUser = userRepository.findByEmail(usernameOrEmail.trim()).orElse(null);
        if (targetUser == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy người dùng: " + usernameOrEmail);

        // Check if already staff at this store
        if (targetUser.getWorkStore() != null && targetUser.getWorkStore().getStoreId().equals(store.getStoreId())
                && targetUser.getRole() != null && "ROLE_STORE_STAFF".equals(targetUser.getRole().getName()))
            return java.util.Map.of("success", false, "message", "Người này đã là nhân viên của cửa hàng");

        // Check if user is the store owner
        if (store.getOwner() != null && store.getOwner().getUserId().equals(targetUser.getUserId()))
            return java.util.Map.of("success", false, "message", "Không thể thêm chủ cửa hàng làm nhân viên");

        // Assign staff role and store
        com.dealxanh.app.entity.Role staffRole = findRoleByName("ROLE_STORE_STAFF", "STORE_STAFF");
        if (staffRole == null)
            return java.util.Map.of("success", false, "message", "Lỗi hệ thống: không tìm thấy role STAFF");

        targetUser.setRole(staffRole);
        targetUser.setWorkStore(store);
        userRepository.save(targetUser);

        return java.util.Map.of("success", true, "message",
                "Đã thêm " + (targetUser.getFullName() != null ? targetUser.getFullName() : targetUser.getUsername()) + " làm nhân viên");
    }

    @PostMapping("/manage-staff/{userId}/remove")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> removeStaff(
            @PathVariable Long userId, Principal principal) {
        User owner = getCurrentUser(principal);
        if (owner == null || owner.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy người dùng");

        // Verify staff belongs to owner's store
        if (targetUser.getWorkStore() == null || !targetUser.getWorkStore().getStoreId().equals(owner.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Nhân viên không thuộc cửa hàng của bạn");

        // Revert to user role
        com.dealxanh.app.entity.Role userRole = findRoleByName("ROLE_USER", "USER");
        if (userRole != null) targetUser.setRole(userRole);
        targetUser.setWorkStore(null);
        userRepository.save(targetUser);

        return java.util.Map.of("success", true, "message", "Đã gỡ nhân viên khỏi cửa hàng");
    }

    @PostMapping("/manage-staff/{userId}/toggle")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STORE_OWNER')")
    @ResponseBody
    public java.util.Map<String, Object> toggleStaff(@PathVariable Long userId, Principal principal) {
        User owner = getCurrentUser(principal);
        if (owner == null || owner.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy người dùng");

        if (targetUser.getWorkStore() == null || !targetUser.getWorkStore().getStoreId().equals(owner.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Nhân viên không thuộc cửa hàng của bạn");

        targetUser.setActive(!targetUser.getActive());
        userRepository.save(targetUser);

        return java.util.Map.of("success", true, "message",
                targetUser.getActive() ? "Đã kích hoạt tài khoản nhân viên" : "Đã vô hiệu hoá tài khoản nhân viên",
                "active", targetUser.getActive());
    }

    // ============ DEALS ============

    @GetMapping("/deals")
    public String deals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model,
            Principal principal) {

        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            return "redirect:/login";
        }

        Store store = user.getWorkStore();
        LocalDateTime now = LocalDateTime.now();

        // Get deals for this store
        List<Deal> allDeals = dealRepository.findByStoreStoreId(store.getStoreId());

        // Filter by status if provided
        List<Deal> filteredDeals = allDeals;
        if (status != null && !status.isEmpty()) {
            final String finalStatus = status;
            filteredDeals = allDeals.stream()
                    .filter(d -> finalStatus.equals(d.getStatus()))
                    .toList();
        }

        // Filter by search if provided
        if (search != null && !search.isEmpty()) {
            final String finalSearch = search.toLowerCase();
            filteredDeals = filteredDeals.stream()
                    .filter(d -> d.getDealName().toLowerCase().contains(finalSearch) ||
                               d.getDealCode().toLowerCase().contains(finalSearch))
                    .toList();
        }

        // Count deals by status
        long activeCount = allDeals.stream().filter(d -> "ACTIVE".equals(d.getStatus())).count();
        long scheduledCount = allDeals.stream().filter(d -> "SCHEDULED".equals(d.getStatus())).count();
        long endedCount = allDeals.stream().filter(d -> "ENDED".equals(d.getStatus())).count();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("activeDeals", activeCount);
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(store.getStoreId()));

        model.addAttribute("deals", filteredDeals);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("endedCount", endedCount);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);

        return "seller/deals";
    }

    @GetMapping("/deals/create")
    public String createDealForm(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("store", user.getWorkStore());
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(user.getWorkStore().getStoreId(), "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(user.getWorkStore().getStoreId()));
        return "seller/create-deal";
    }

    @PostMapping("/deals/create")
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
            @RequestParam(required = false) String bannerUrl,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile bannerFile,
            @RequestParam(defaultValue = "CODE_REQUIRED") String applyMethod,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
                return "redirect:/seller/deals";
            }

            // Handle banner file upload
            String bannerPath = bannerUrl;
            if (bannerFile != null && !bannerFile.isEmpty()) {
                if (!isImageFile(bannerFile)) {
                    redirectAttributes.addFlashAttribute("error", "File banner phải là ảnh (JPG, PNG, WEBP)");
                    return "redirect:/seller/deals/create";
                }
                if (!isValidFileSize(bannerFile, 5 * 1024 * 1024)) {
                    redirectAttributes.addFlashAttribute("error", "File banner không được vượt quá 5MB");
                    return "redirect:/seller/deals/create";
                }
                bannerPath = saveUploadedFile(bannerFile);
            }

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
            deal.setStartTime(startTime);
            deal.setEndTime(endTime);
            deal.setStore(user.getWorkStore());
            deal.setScope("SPECIFIC_STORES");
            deal.setBannerUrl(bannerPath);
            deal.setApplyMethod(applyMethod);
            deal.setPriority(50);
            deal.setCreatedBy(user);

            dealService.createDeal(deal);

            redirectAttributes.addFlashAttribute("success", "Tạo deal thành công! Vui lòng thêm sản phẩm vào deal.");
            return "redirect:/seller/deals";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/seller/deals/create";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + ex.getMessage());
            return "redirect:/seller/deals/create";
        }
    }

    // ========== File upload helpers ==========

    private String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) return null;

        // Compute MD5 hash of file content for dedup
        java.security.MessageDigest md5;
        try {
            md5 = java.security.MessageDigest.getInstance("MD5");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new java.io.IOException("MD5 not available", e);
        }
        byte[] digest = md5.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        String hash = sb.toString();

        // Extract extension from original filename
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = hash + ext;
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        java.nio.file.Path targetPath = uploadPath.resolve(fileName);
        // Only write if file doesn't already exist (content-based dedup)
        if (!java.nio.file.Files.exists(targetPath)) {
            java.nio.file.Files.copy(file.getInputStream(), targetPath);
        }
        return "/uploads/" + fileName;
    }

    private boolean isImageFile(org.springframework.web.multipart.MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg")
                || contentType.equals("image/png") || contentType.equals("image/webp"));
    }

    private String validateDocumentFile(org.springframework.web.multipart.MultipartFile file, String docName) {
        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            return docName + ": File quá lớn (tối đa 5MB). Kích thước hiện tại: " + String.format("%.1f", file.getSize() / (1024.0 * 1024.0)) + "MB";
        }
        // Check file type (images or PDF only)
        String contentType = file.getContentType();
        if (contentType == null) {
            return docName + ": Không thể xác định loại file";
        }
        boolean valid = contentType.equals("image/jpeg") || contentType.equals("image/png")
                || contentType.equals("image/webp") || contentType.equals("image/gif")
                || contentType.equals("image/bmp")
                || contentType.equals("application/pdf");
        if (!valid) {
            return docName + ": Chỉ chấp nhận file ảnh (JPG, PNG, WEBP) hoặc PDF. Loại file hiện tại: " + contentType;
        }
        return null; // OK
    }

    private boolean isValidFileSize(org.springframework.web.multipart.MultipartFile file, long maxSizeInBytes) {
        return file.getSize() <= maxSizeInBytes;
    }

    // ============ DEAL PRODUCTS API ============

    @GetMapping("/deals/{dealId}/available-products")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> getAvailableProductsForDeal(
            @PathVariable Long dealId,
            Principal principal) {

        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            return Map.of("error", "Không tìm thấy cửa hàng");
        }

        Deal deal = dealRepository.findById(dealId).orElse(null);
        if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
            return Map.of("error", "Deal không tồn tại hoặc không thuộc quyền quản lý");
        }

        // Get products that are NOT already in this deal
        List<Long> existingProductIds = dealProductRepository.findByDeal(deal)
                .stream()
                .map(dp -> dp.getProduct().getProductId())
                .toList();

        List<Product> availableProducts;
        if (existingProductIds.isEmpty()) {
            availableProducts = productRepository.findByStoreStoreIdAndDeletedFalse(user.getWorkStore().getStoreId());
        } else {
            availableProducts = productRepository.findByStoreStoreIdAndDeletedFalse(user.getWorkStore().getStoreId())
                    .stream()
                    .filter(p -> !existingProductIds.contains(p.getProductId()))
                    .toList();
        }

        return Map.of("products", availableProducts);
    }

    @PostMapping("/deals/{dealId}/assign-products")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> assignProductsToDeal(
            @PathVariable Long dealId,
            @RequestBody List<Map<String, Object>> productsData,
            Principal principal) {

        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null) {
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");
            }

            Deal deal = dealRepository.findById(dealId).orElse(null);
            if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
                return Map.of("success", false, "message", "Deal không tồn tại hoặc không thuộc quyền quản lý");
            }

            int addedCount = 0;
            for (Map<String, Object> productData : productsData) {
                Long productId = ((Number) productData.get("productId")).longValue();
                Double salePrice = ((Number) productData.get("salePrice")).doubleValue();
                Double originalPrice = ((Number) productData.get("originalPrice")).doubleValue();
                Integer maxQuantity = productData.get("maxQuantity") != null ?
                        ((Number) productData.get("maxQuantity")).intValue() : null;
                Integer priority = productData.get("priority") != null ?
                        ((Number) productData.get("priority")).intValue() : 0;

                dealService.addProductToDeal(dealId, productId, originalPrice, salePrice, maxQuantity, priority);
                addedCount++;
            }

            return Map.of("success", true, "message", "Đã thêm " + addedCount + " sản phẩm vào deal");

        } catch (IllegalArgumentException ex) {
            return Map.of("success", false, "message", ex.getMessage());
        } catch (Exception ex) {
            return Map.of("success", false, "message", "Lỗi: " + ex.getMessage());
        }
    }

    @GetMapping("/deals/{dealId}/products")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> getDealProducts(@PathVariable Long dealId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            return Map.of("error", "Không tìm thấy cửa hàng");
        }

        Deal deal = dealRepository.findById(dealId).orElse(null);
        if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
            return Map.of("error", "Deal không tồn tại");
        }

        List<DealProduct> products = dealService.getDealProducts(dealId);
        return Map.of("products", products);
    }

    @PostMapping("/deals/{dealId}/remove-product/{productId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> removeProductFromDeal(
            @PathVariable Long dealId,
            @PathVariable Long productId,
            Principal principal) {

        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null) {
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");
            }

            Deal deal = dealRepository.findById(dealId).orElse(null);
            if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
                return Map.of("success", false, "message", "Deal không tồn tại");
            }

            boolean removed = dealService.removeProductFromDeal(dealId, productId);
            if (removed) {
                return Map.of("success", true, "message", "Đã xóa sản phẩm khỏi deal");
            } else {
                return Map.of("success", false, "message", "Không tìm thấy sản phẩm trong deal");
            }

        } catch (Exception ex) {
            return Map.of("success", false, "message", "Lỗi: " + ex.getMessage());
        }
    }

    @PostMapping("/deals/{dealId}/pause")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> pauseDeal(@PathVariable Long dealId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            Deal deal = dealRepository.findById(dealId).orElse(null);

            if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
                return Map.of("success", false, "message", "Deal không tồn tại");
            }

            deal.setStatus("PAUSED");
            deal.setUpdatedAt(LocalDateTime.now());
            dealRepository.save(deal);

            return Map.of("success", true, "message", "Đã tạm dừng deal");
        } catch (Exception ex) {
            return Map.of("success", false, "message", "Lỗi: " + ex.getMessage());
        }
    }

    @PostMapping("/deals/{dealId}/resume")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_STAFF')")
    @ResponseBody
    public Map<String, Object> resumeDeal(@PathVariable Long dealId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            Deal deal = dealRepository.findById(dealId).orElse(null);

            if (deal == null || !user.getWorkStore().getStoreId().equals(deal.getStore().getStoreId())) {
                return Map.of("success", false, "message", "Deal không tồn tại");
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(deal.getStartTime()) || now.isAfter(deal.getEndTime())) {
                return Map.of("success", false, "message", "Deal không trong thời gian chạy");
            }

            deal.setStatus("ACTIVE");
            deal.setUpdatedAt(LocalDateTime.now());
            dealRepository.save(deal);

            return Map.of("success", true, "message", "Đã kích hoạt deal");
        } catch (Exception ex) {
            return Map.of("success", false, "message", "Lỗi: " + ex.getMessage());
        }
    }

    // ============ PRODUCT MANAGEMENT ============

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();

        java.util.List<Product> allProducts = productRepository.findByStoreStoreIdAndDeletedFalse(store.getStoreId());

        // Filters
        java.util.List<Product> filtered = allProducts;
        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                filtered = filtered.stream().filter(p -> p.getActive()).toList();
            } else if ("inactive".equals(status)) {
                filtered = filtered.stream().filter(p -> !p.getActive()).toList();
            } else if ("outofstock".equals(status)) {
                filtered = filtered.stream().filter(p -> p.getStockQuantity() <= 0).toList();
            }
        }
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            filtered = filtered.stream().filter(p -> p.getName().toLowerCase().contains(s)).toList();
        }
        if (categoryId != null) {
            filtered = filtered.stream().filter(p -> p.getCategory() != null && p.getCategory().getCategoryId().equals(categoryId)).toList();
        }

        // KPI (on filtered)
        long approvedCount = filtered.stream().filter(p -> "APPROVED".equals(p.getApprovalStatus())).count();
        long pendingCount = filtered.stream().filter(p -> "PENDING".equals(p.getApprovalStatus())).count();

        // Pagination
        int total = filtered.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;
        int start = page * size;
        int end = Math.min(start + size, total);
        java.util.List<Product> paged = total > 0 ? filtered.subList(start, end) : java.util.List.of();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("products", paged);
        model.addAttribute("totalProducts", total);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(store.getStoreId(), "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(store.getStoreId()));
        return "seller/products";
    }

    @PostMapping("/products/{productId}/toggle")
    public String toggleProduct(@PathVariable Long productId, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.getStore().getStoreId().equals(user.getWorkStore().getStoreId())) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/seller/products";
        }

        // Seller chỉ được DỪNG BÁN, không được kích hoạt lại
        if (!product.getActive()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm đã bị dừng bán. Liên hệ admin để kích hoạt lại.");
            return "redirect:/seller/products";
        }

        try {
            product.setActive(false);
            product.setUpdatedAt(java.time.LocalDateTime.now());
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Đã ngừng bán sản phẩm: " + product.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @GetMapping("/api/products/combo-available")
    @ResponseBody
    public Map<String, Object> getComboAvailableProducts(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        List<Product> products = productRepository.findComboAvailableByStore(storeId);

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", p.getProductId());
            m.put("name", p.getName());
            m.put("originalPrice", Math.round(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0));
            m.put("stockQuantity", p.getStockQuantity());
            m.put("expiryDate", p.getExpiryDate() != null ? p.getExpiryDate().toString() : null);
            return m;
        }).toList();

        return Map.of("products", result);
    }

    // ===== QUICK PUSH (ĐẨY HÀNG NHANH) =====

    @GetMapping("/api/products/expiring-soon")
    @ResponseBody
    public Map<String, Object> getExpiringSoonProducts(
            @RequestParam(defaultValue = "1") int days, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);

        // Get all active SPECIFIC_DEAL products expiring within N days
        List<Product> expiringProducts = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> p.getActive() && "SPECIFIC_DEAL".equals(p.getProductType()))
            .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(deadline) && p.getExpiryDate().isAfter(now))
            .toList();

        // Group by category
        Map<String, List<Map<String, Object>>> grouped = new java.util.LinkedHashMap<>();
        for (Product p : expiringProducts) {
            String catName = p.getCategory() != null ? p.getCategory().getName() : "Không danh mục";
            Long catId = p.getCategory() != null ? p.getCategory().getCategoryId() : null;
            String key = (catId != null ? catId : "none") + "|" + catName;
            grouped.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(Map.of(
                "productId", p.getProductId(),
                "name", p.getName(),
                "originalPrice", Math.round(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0),
                "expiryDate", p.getExpiryDate().toString(),
                "categoryName", catName,
                "categoryId", catId
            ));
        }

        // Build response: categories with enough products (>= 3) for combo
        List<Map<String, Object>> categories = new java.util.ArrayList<>();
        for (var entry : grouped.entrySet()) {
            if (entry.getValue().size() >= 3) {
                String[] parts = entry.getKey().split("\\|", 2);
                Map<String, Object> cat = new HashMap<>();
                cat.put("categoryId", "none".equals(parts[0]) ? null : Long.parseLong(parts[0]));
                cat.put("categoryName", parts[1]);
                cat.put("productCount", entry.getValue().size());
                cat.put("possibleCombos", entry.getValue().size() / 3);
                cat.put("products", entry.getValue());
                categories.add(cat);
            }
        }

        long totalExpiring = expiringProducts.size();
        long totalPossibleCombos = categories.stream().mapToLong(c -> ((Number) c.get("possibleCombos")).longValue()).sum();

        return Map.of(
            "success", true,
            "totalExpiring", totalExpiring,
            "totalPossibleCombos", totalPossibleCombos,
            "categories", categories
        );
    }

    @PostMapping("/api/products/quick-push")
    @ResponseBody
    public Map<String, Object> quickPushCombo(
            @RequestParam String productIds,
            @RequestParam(required = false) String comboName,
            Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null)
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

            String[] ids = productIds.split(",");
            if (ids.length < 3)
                return Map.of("success", false, "message", "Cần ít nhất 3 sản phẩm để tạo combo đẩy nhanh");

            List<Product> selectedProducts = new java.util.ArrayList<>();
            String categoryName = null;
            LocalDateTime earliestExpiry = null;
            double totalPrice = 0;

            for (String idStr : ids) {
                Long pid = Long.parseLong(idStr.trim());
                Product p = productRepository.findById(pid).orElse(null);
                if (p == null || !p.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
                    return Map.of("success", false, "message", "Sản phẩm #" + pid + " không tồn tại hoặc không thuộc cửa hàng");
                selectedProducts.add(p);
                totalPrice += p.getOriginalPrice() != null ? p.getOriginalPrice() : 0;
                if (p.getCategory() != null && categoryName == null) categoryName = p.getCategory().getName();
                if (p.getExpiryDate() != null) {
                    if (earliestExpiry == null || p.getExpiryDate().isBefore(earliestExpiry))
                        earliestExpiry = p.getExpiryDate();
                }
            }

            // Create combo with 30-40% discount off average
            double avgPrice = totalPrice / selectedProducts.size();
            long comboPrice = Math.round(avgPrice * 0.65); // 35% off

            Product combo = new Product();
            combo.setName(comboName != null && !comboName.isEmpty()
                ? comboName
                : "Combo " + (categoryName != null ? categoryName : "Đẩy nhanh") + " " + selectedProducts.size() + " món");
            combo.setDescription("Combo đẩy hàng nhanh gồm: "
                + selectedProducts.stream().map(Product::getName).reduce((a, b) -> a + ", " + b).orElse("")
                + ". Giá ưu đãi chỉ hôm nay!");
            combo.setOriginalPrice((double) comboPrice);
            combo.setCurrentPrice((double) comboPrice);
            combo.setStockQuantity(10); // Default stock for quick-push combo
            combo.setProductType("COMBO");
            combo.setStore(user.getWorkStore());
            combo.setApprovalStatus("PENDING");
            combo.setActive(true);
            combo.setDeleted(false);
            if (earliestExpiry != null) combo.setExpiryDate(earliestExpiry);
            // Set category to the first product's category
            if (!selectedProducts.isEmpty() && selectedProducts.get(0).getCategory() != null) {
                combo.setCategory(selectedProducts.get(0).getCategory());
            }

            productService.createProduct(combo);

            String msg = "Đã tạo combo đẩy nhanh: " + combo.getName() + " - " + fmtPrice(comboPrice);
            return Map.of("success", true, "message", msg);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    private String fmtPrice(double price) {
        return Math.round(price) + "đ";
    }

    @GetMapping("/products/create")
    public String createProductForm(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("store", user.getWorkStore());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(user.getWorkStore().getStoreId(), "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(user.getWorkStore().getStoreId()));
        return "seller/create-product";
    }

    @PostMapping("/products/create")
    public String createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double originalPrice,
            @RequestParam(required = false) Integer stockQuantity,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String expiryDateStr,
            @RequestParam(required = false) String comboProductIds,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile imageFile,
            @RequestParam(required = false) String imageUrl,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/seller/products";
        }

        try {
            String imgPath = imageUrl;
            if (imageFile != null && !imageFile.isEmpty()) {
                if (!isImageFile(imageFile)) {
                    redirectAttributes.addFlashAttribute("error", "File ảnh phải là JPG, PNG hoặc WEBP");
                    return "redirect:/seller/products/create";
                }
                imgPath = saveUploadedFile(imageFile);
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setOriginalPrice(originalPrice);
            product.setCurrentPrice(originalPrice);
            product.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
            product.setProductType(productType != null ? productType : "SPECIFIC_DEAL");
            product.setImageUrl(imgPath);
            product.setStore(user.getWorkStore());
            product.setApprovalStatus("PENDING");
            product.setActive(true);
            product.setDeleted(false);

            if (categoryId != null) {
                categoryRepository.findById(categoryId).ifPresent(product::setCategory);
            }

            // Combo: set expiry to earliest among selected products
            if ("COMBO".equals(productType) && comboProductIds != null && !comboProductIds.isEmpty()) {
                String[] ids = comboProductIds.split(",");
                LocalDateTime earliestExpiry = null;
                for (String idStr : ids) {
                    Long pid = Long.parseLong(idStr.trim());
                    Product comboItem = productRepository.findById(pid).orElse(null);
                    if (comboItem != null && comboItem.getExpiryDate() != null) {
                        if (earliestExpiry == null || comboItem.getExpiryDate().isBefore(earliestExpiry)) {
                            earliestExpiry = comboItem.getExpiryDate();
                        }
                    }
                }
                if (earliestExpiry != null) {
                    product.setExpiryDate(earliestExpiry);
                }
            } else if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                product.setExpiryDate(java.time.LocalDateTime.parse(expiryDateStr + "T23:59:59"));
            }

            productService.createProduct(product);
            String msg = "COMBO".equals(productType)
                ? "Tạo combo thành công! Sản phẩm đang chờ admin duyệt."
                : "Tạo sản phẩm thành công! Sản phẩm đang chờ admin duyệt.";
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @PostMapping("/api/products/{productId}/delete")
    @ResponseBody
    public Map<String, Object> deleteProduct(@PathVariable Long productId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null)
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
                return Map.of("success", false, "message", "Sản phẩm không tồn tại");

            if (!"PENDING".equals(product.getApprovalStatus()))
                return Map.of("success", false, "message", "Chỉ được xóa sản phẩm đang chờ duyệt");

            product.setDeleted(true);
            product.setActive(false);
            product.setUpdatedAt(java.time.LocalDateTime.now());
            productRepository.save(product);

            return Map.of("success", true, "message", "Đã xóa sản phẩm: " + product.getName());
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/api/products/{productId}/detail")
    @ResponseBody
    public java.util.Map<String, Object> getProductDetail(@PathVariable Long productId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("error", "Không tìm thấy cửa hàng");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return java.util.Map.of("error", "Sản phẩm không tồn tại");

        // Build response
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("productId", product.getProductId());
        data.put("name", product.getName());
        data.put("description", product.getDescription());
        data.put("imageUrl", product.getImageUrl());
        data.put("originalPrice", product.getOriginalPrice());
        data.put("stockQuantity", product.getStockQuantity());
        data.put("productType", product.getProductType());
        data.put("active", product.getActive());
        data.put("approvalStatus", product.getApprovalStatus());
        data.put("rejectionReason", product.getRejectionReason());
        data.put("expiryDate", product.getExpiryDate() != null ? product.getExpiryDate().toString() : null);
        if (product.getCategory() != null) {
            data.put("categoryName", product.getCategory().getName());
        }

        // Platform deals on this product's category
        java.util.List<java.util.Map<String, Object>> platformDeals = new java.util.ArrayList<>();
        if (product.getCategory() != null) {
            java.util.List<DealCategory> dealCategories = dealCategoryRepository.findActiveByCategory(product.getCategory());
            if (dealCategories != null) {
                for (DealCategory dc : dealCategories) {
                    Deal d = dc.getDeal();
                    if (d == null || !"ACTIVE".equals(d.getStatus())) continue;
                    java.util.Map<String, Object> di = new java.util.HashMap<>();
                    di.put("dealName", d.getDealName());
                    di.put("dealType", d.getDealType());
                    di.put("discountType", d.getDiscountType());
                    di.put("discountValue", d.getDiscountValue());
                    di.put("discountDisplay", "PERCENT".equals(d.getDiscountType()) ? d.getDiscountValue() + "%" : d.getDiscountValue() + "đ");
                    double discountAmount = 0;
                    if (product.getOriginalPrice() != null) {
                        if ("PERCENT".equals(d.getDiscountType())) {
                            discountAmount = product.getOriginalPrice() * d.getDiscountValue() / 100.0;
                            if (d.getMaxDiscountAmount() != null) discountAmount = Math.min(discountAmount, d.getMaxDiscountAmount());
                        } else {
                            discountAmount = d.getDiscountValue();
                        }
                    }
                    di.put("discountAmount", Math.round(discountAmount));
                    platformDeals.add(di);
                }
            }
        }
        data.put("platformDeals", platformDeals);

        // Store deals this product belongs to
        java.util.List<java.util.Map<String, Object>> storeDeals = new java.util.ArrayList<>();
        java.util.List<DealProduct> dealProducts = dealProductRepository.findByProduct(product);
        if (dealProducts != null) {
            for (DealProduct dp : dealProducts) {
                Deal d = dp.getDeal();
                if (d == null) continue;
                java.util.Map<String, Object> di = new java.util.HashMap<>();
                di.put("dealName", d.getDealName());
                di.put("dealType", d.getDealType());
                di.put("status", d.getStatus());
                di.put("salePrice", dp.getSalePrice());
                di.put("originalPrice", dp.getOriginalPrice());
                di.put("discountDisplay", dp.getSalePrice() != null ? dp.getSalePrice().longValue() + "đ (-" + Math.round((1 - dp.getSalePrice() / dp.getOriginalPrice()) * 100) + "%)" : "");
                storeDeals.add(di);
            }
        }
        data.put("storeDeals", storeDeals);

        // Calculate final price
        double originalPrice = product.getOriginalPrice() != null ? product.getOriginalPrice() : 0;
        double totalPlatformDiscount = 0;
        for (java.util.Map<String, Object> pd : platformDeals) {
            totalPlatformDiscount += ((Number) pd.get("discountAmount")).doubleValue();
        }
        double lowestStorePrice = originalPrice;
        for (java.util.Map<String, Object> sd : storeDeals) {
            double sp = ((Number) sd.get("salePrice")).doubleValue();
            if (sp < lowestStorePrice) lowestStorePrice = sp;
        }
        double finalPrice = lowestStorePrice - totalPlatformDiscount;
        if (finalPrice < originalPrice * 0.15) finalPrice = originalPrice * 0.15; // floor 15%

        data.put("finalPrice", Math.round(finalPrice));
        data.put("totalPlatformDiscount", Math.round(totalPlatformDiscount));
        data.put("totalDiscountPercent", originalPrice > 0 ? Math.round((1 - finalPrice / originalPrice) * 100) : 0);

        return data;
    }

    @PostMapping("/products/{productId}/edit")
    public String editProduct(
            @PathVariable Long productId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double originalPrice,
            @RequestParam(required = false) Integer stockQuantity,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String expiryDateStr,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile imageFile,
            @RequestParam(required = false) String existingImageUrl,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/seller/products";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.getStore().getStoreId().equals(user.getWorkStore().getStoreId())) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/seller/products";
        }

        try {
            String imgPath = existingImageUrl;
            if (imageFile != null && !imageFile.isEmpty()) {
                if (!isImageFile(imageFile)) {
                    redirectAttributes.addFlashAttribute("error", "File ảnh phải là JPG, PNG hoặc WEBP");
                    return "redirect:/seller/products";
                }
                imgPath = saveUploadedFile(imageFile);
            }

            product.setName(name);
            product.setDescription(description);
            product.setOriginalPrice(originalPrice);
            product.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
            if (productType != null) product.setProductType(productType);
            if (imgPath != null) product.setImageUrl(imgPath);
            if (categoryId != null) categoryRepository.findById(categoryId).ifPresent(product::setCategory);
            if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                product.setExpiryDate(java.time.LocalDateTime.parse(expiryDateStr + "T23:59:59"));
            }
            product.setApprovalStatus("PENDING"); // re-submit for approval
            productService.updateProduct(productId, product);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công! Sản phẩm sẽ được duyệt lại.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/products";
    }

    // ============ ORDER MANAGEMENT ============

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

        // Today's revenue
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().toLocalDate().atStartOfDay();
        java.time.LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
        Double todayRevenue = orderRepository.sumRevenueByPeriod(todayStart, todayEnd);
        Long todayOrders = orderRepository.countOrdersByPeriod(todayStart, todayEnd);

        // Orders list - fetch more for client-side search filtering
        int fetchSize = (search != null && !search.isEmpty()) ? 200 : size;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, fetchSize, org.springframework.data.domain.Sort.by("createdAt").descending());
        var ordersPage = status != null && !status.isEmpty()
                ? orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable)
                : orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, pageable);

        java.util.List<com.dealxanh.app.entity.Order> orders = ordersPage.getContent();

        // Filter by search term (order ID, customer name, product name)
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

        // Paginate filtered results
        int total = orders.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;
        int start = page * size;
        int end = Math.min(start + size, total);
        java.util.List<com.dealxanh.app.entity.Order> paged = total > 0 ? orders.subList(start, end) : java.util.List.of();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("orders", paged);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("readyCount", readyCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : 0);
        model.addAttribute("todayOrders", todayOrders != null ? todayOrders : 0);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalOrders", (long) total);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(storeId));
        return "seller/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    @ResponseBody
    public java.util.Map<String, Object> updateOrderStatus(
            @PathVariable Long orderId, @RequestParam String newStatus, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        com.dealxanh.app.entity.Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Đơn hàng không tồn tại");

        // Valid transitions: PENDING→CONFIRMED→READY_FOR_PICKUP→COMPLETED
        String current = order.getStatus();
        boolean valid = false;
        if ("PENDING".equals(current) && ("CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus))) valid = true;
        if ("CONFIRMED".equals(current) && "READY_FOR_PICKUP".equals(newStatus)) valid = true;
        if ("READY_FOR_PICKUP".equals(current) && "COMPLETED".equals(newStatus)) valid = true;

        if (!valid) return java.util.Map.of("success", false, "message",
            "Không thể chuyển từ " + current + " sang " + newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        if ("READY_FOR_PICKUP".equals(newStatus) && order.getPickupQrCode() == null) {
            String qrCode = "DX-" + orderId + "-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            order.setPickupQrCode(qrCode);
        }
        if ("COMPLETED".equals(newStatus)) order.setActualPickupTime(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return java.util.Map.of("success", true, "message", "Đã cập nhật trạng thái đơn hàng");
    }

    @GetMapping("/api/orders/{orderId}/detail")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable Long orderId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        com.dealxanh.app.entity.Order order = orderRepository.findByIdWithItems(orderId).orElse(null);
        if (order == null || !order.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return Map.of("success", false, "message", "Đơn hàng không tồn tại");

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("status", order.getStatus());
        data.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        data.put("updatedAt", order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null);
        data.put("scheduledPickupTime", order.getScheduledPickupTime() != null ? order.getScheduledPickupTime().toString() : null);
        data.put("actualPickupTime", order.getActualPickupTime() != null ? order.getActualPickupTime().toString() : null);
        data.put("finalAmount", Math.round(order.getFinalAmount() != null ? order.getFinalAmount() : 0));
        data.put("discountAmount", Math.round(order.getDiscountAmount() != null ? order.getDiscountAmount() : 0));
        data.put("totalAmount", Math.round(order.getTotalAmount() != null ? order.getTotalAmount() : 0));
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("paymentStatus", order.getPaymentStatus());
        data.put("couponCode", order.getCouponCode());
        data.put("cancellationReason", order.getCancellationReason());
        data.put("pickupQrCode", order.getPickupQrCode());
        data.put("customerName", order.getUser() != null ?
            (order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getUsername()) : "N/A");
        data.put("customerPhone", order.getUser() != null ? order.getUser().getPhone() : null);

        // Order items with product names and quantities
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        if (order.getOrderItems() != null) {
            for (com.dealxanh.app.entity.OrderItem item : order.getOrderItems()) {
                Map<String, Object> i = new HashMap<>();
                i.put("productName", item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm đã xóa");
                i.put("quantity", item.getQuantity());
                i.put("unitPrice", Math.round(item.getUnitPrice() != null ? item.getUnitPrice() : 0));
                i.put("subtotal", Math.round(item.getSubtotal()));
                items.add(i);
            }
        }
        data.put("items", items);

        // Auto-cancel / pickup deadline info
        LocalDateTime now = LocalDateTime.now();
        String deadlineInfo = "";
        String deadlineWarning = "";

        if ("PENDING".equals(order.getStatus())) {
            // Auto-cancel if not confirmed within 60 minutes of creation
            if (order.getCreatedAt() != null) {
                LocalDateTime deadline = order.getCreatedAt().plusMinutes(60);
                if (now.isAfter(deadline)) {
                    deadlineInfo = "Quá hạn xác nhận " + java.time.Duration.between(deadline, now).toMinutes() + " phút";
                    deadlineWarning = "overdue";
                } else {
                    long remaining = java.time.Duration.between(now, deadline).toMinutes();
                    deadlineInfo = "Cần xác nhận trong " + remaining + " phút nữa";
                    deadlineWarning = remaining < 15 ? "urgent" : "normal";
                }
            }
        } else if ("CONFIRMED".equals(order.getStatus()) || "READY_FOR_PICKUP".equals(order.getStatus())) {
            if (order.getScheduledPickupTime() != null) {
                LocalDateTime deadline = order.getScheduledPickupTime().plusHours(2); // 2h grace after scheduled pickup
                if (now.isAfter(deadline)) {
                    deadlineInfo = "Quá hạn pickup " + java.time.Duration.between(deadline, now).toMinutes() + " phút";
                    deadlineWarning = "overdue";
                } else if (now.isAfter(order.getScheduledPickupTime())) {
                    long remaining = java.time.Duration.between(now, deadline).toMinutes();
                    deadlineInfo = "Đã đến giờ pickup, còn " + remaining + " phút để khách đến lấy";
                    deadlineWarning = "urgent";
                } else {
                    long untilPickup = java.time.Duration.between(now, order.getScheduledPickupTime()).toMinutes();
                    deadlineInfo = "Pickup dự kiến sau " + untilPickup + " phút nữa";
                    deadlineWarning = "normal";
                }
            }
        }
        data.put("deadlineInfo", deadlineInfo);
        data.put("deadlineWarning", deadlineWarning);
        data.put("success", true);

        return data;
    }

    // ============ FINANCE & WALLET ============

    @GetMapping("/finance")
    public String finance(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // Wallet from transactions table
        Double totalEarned = transactionRepository.sumEarnedByStore(storeId);
        Double totalCommission = transactionRepository.sumCommissionByStore(storeId);
        Double totalPaidOut = transactionRepository.sumPaidOutByStore(storeId);
        Double pendingPayout = transactionRepository.sumPendingPayoutByStore(storeId);
        if (totalEarned == null) totalEarned = 0.0;
        if (totalCommission == null) totalCommission = 0.0;
        if (totalPaidOut == null) totalPaidOut = 0.0;
        if (pendingPayout == null) pendingPayout = 0.0;
        double availableBalance = totalEarned - totalCommission - totalPaidOut - pendingPayout;

        // Recent completed orders from Order table
        org.springframework.data.domain.Pageable top10 = org.springframework.data.domain.PageRequest.of(0, 10,
            org.springframework.data.domain.Sort.by("createdAt").descending());
        var recentOrders = orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, "COMPLETED", top10);

        // Pending payout requests (PAYOUT PENDING)
        var payoutPageable = org.springframework.data.domain.PageRequest.of(0, 20,
            org.springframework.data.domain.Sort.by("createdAt").descending());
        var pendingPayouts = transactionRepository.findByStoreAndType(storeId, "PAYOUT", payoutPageable);

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("totalEarned", Math.round(totalEarned));
        model.addAttribute("totalCommission", Math.round(totalCommission));
        model.addAttribute("totalPaidOut", Math.round(totalPaidOut));
        model.addAttribute("pendingPayoutAmount", Math.round(pendingPayout));
        model.addAttribute("availableBalance", Math.round(availableBalance));
        model.addAttribute("recentOrders", recentOrders.getContent());
        model.addAttribute("payoutRequests", pendingPayouts);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(storeId));
        return "seller/finance";
    }

    // ============ FINANCE API ============

    @GetMapping("/api/finance/wallet")
    @ResponseBody
    public Map<String, Object> getWallet(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        Double totalEarned = transactionRepository.sumEarnedByStore(storeId);
        Double totalCommission = transactionRepository.sumCommissionByStore(storeId);
        Double totalPaidOut = transactionRepository.sumPaidOutByStore(storeId);
        Double pendingPayout = transactionRepository.sumPendingPayoutByStore(storeId);
        if (totalEarned == null) totalEarned = 0.0;
        if (totalCommission == null) totalCommission = 0.0;
        if (totalPaidOut == null) totalPaidOut = 0.0;
        if (pendingPayout == null) pendingPayout = 0.0;
        double availableBalance = totalEarned - totalCommission - totalPaidOut - pendingPayout;

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("totalEarned", Math.round(totalEarned));
        data.put("totalCommission", Math.round(totalCommission));
        data.put("totalPaidOut", Math.round(totalPaidOut));
        data.put("pendingPayout", Math.round(pendingPayout));
        data.put("availableBalance", Math.round(availableBalance));
        data.put("commissionRate", Math.round(store.getCommissionRate() * 100));
        return data;
    }

    @GetMapping("/api/finance/transactions")
    @ResponseBody
    public Map<String, Object> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        List<Transaction> transactions;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            transactions = transactionRepository.findByStoreAndDateRangeAndType(storeId, start, end,
                    (type != null && !type.isEmpty()) ? type : null, pageable);
        } else {
            transactions = transactionRepository.findByStoreAndType(storeId,
                    (type != null && !type.isEmpty()) ? type : null, pageable);
        }

        List<Map<String, Object>> result = transactions.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("transactionId", t.getTransactionId());
            m.put("type", t.getType());
            m.put("amount", Math.round(t.getAmount() != null ? t.getAmount() : 0));
            m.put("platformFee", Math.round(t.getPlatformFee()));
            m.put("netAmount", Math.round(t.getNetAmount() != null ? t.getNetAmount() : 0));
            m.put("status", t.getStatus());
            m.put("description", t.getDescription());
            m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
            if (t.getOrder() != null) m.put("orderId", t.getOrder().getOrderId());
            return m;
        }).toList();

        return Map.of("success", true, "transactions", result);
    }

    @PostMapping("/api/finance/request-payout")
    @ResponseBody
    public Map<String, Object> requestPayout(
            @RequestParam Double amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String bankAccountNumber,
            Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null)
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

            Store store = user.getWorkStore();
            Long storeId = store.getStoreId();

            if (amount == null || amount <= 0)
                return Map.of("success", false, "message", "Số tiền phải lớn hơn 0");

            if (amount < 50000)
                return Map.of("success", false, "message", "Số tiền rút tối thiểu là 50.000đ");

            Double totalEarned = transactionRepository.sumEarnedByStore(storeId);
            Double totalCommission = transactionRepository.sumCommissionByStore(storeId);
            Double totalPaidOut = transactionRepository.sumPaidOutByStore(storeId);
            Double pendingPayout = transactionRepository.sumPendingPayoutByStore(storeId);
            if (totalEarned == null) totalEarned = 0.0;
            if (totalCommission == null) totalCommission = 0.0;
            if (totalPaidOut == null) totalPaidOut = 0.0;
            if (pendingPayout == null) pendingPayout = 0.0;
            double availableBalance = totalEarned - totalCommission - totalPaidOut - pendingPayout;

            if (amount > availableBalance)
                return Map.of("success", false, "message", "Số dư không đủ. Số dư khả dụng: " + Math.round(availableBalance) + "đ");

            if (bankName != null && !bankName.isEmpty()) store.setBankName(bankName);
            if (bankAccountNumber != null && !bankAccountNumber.isEmpty()) store.setBankAccountNumber(bankAccountNumber);
            storeRepository.save(store);

            Transaction txn = new Transaction();
            txn.setStore(store);
            txn.setType("PAYOUT");
            txn.setAmount(amount);
            txn.setPlatformFee(0.0);
            txn.setNetAmount(amount);
            txn.setStatus("PENDING");
            String desc = description != null && !description.isEmpty() ? description : "Yêu cầu đối soát";
            txn.setDescription(desc + " | TK: " + (store.getBankAccountNumber() != null ? store.getBankAccountNumber() : "Chưa cập nhật")
                    + " - " + (store.getBankName() != null ? store.getBankName() : ""));
            txn.setCreatedAt(LocalDateTime.now());
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            return Map.of("success", true, "message", "Đã gửi yêu cầu đối soát " + Math.round(amount) + "đ");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/api/finance/edit-payout/{transactionId}")
    @ResponseBody
    public Map<String, Object> editPayout(
            @PathVariable Long transactionId,
            @RequestParam Double amount,
            @RequestParam(required = false) String description,
            Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null)
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

            Transaction txn = transactionRepository.findById(transactionId).orElse(null);
            if (txn == null || !txn.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
                return Map.of("success", false, "message", "Yêu cầu không tồn tại");

            if (!"PENDING".equals(txn.getStatus()))
                return Map.of("success", false, "message", "Chỉ có thể sửa yêu cầu đang chờ xử lý");

            if (!"PAYOUT".equals(txn.getType()))
                return Map.of("success", false, "message", "Chỉ có thể sửa yêu cầu đối soát");

            if (amount == null || amount <= 0)
                return Map.of("success", false, "message", "Số tiền phải lớn hơn 0");

            if (amount < 50000)
                return Map.of("success", false, "message", "Số tiền rút tối thiểu là 50.000đ");

            Store store = user.getWorkStore();
            Long storeId = store.getStoreId();
            Double totalEarned = transactionRepository.sumEarnedByStore(storeId);
            Double totalCommission = transactionRepository.sumCommissionByStore(storeId);
            Double totalPaidOut = transactionRepository.sumPaidOutByStore(storeId);
            Double pendingPayout = transactionRepository.sumPendingPayoutByStore(storeId);
            if (totalEarned == null) totalEarned = 0.0;
            if (totalCommission == null) totalCommission = 0.0;
            if (totalPaidOut == null) totalPaidOut = 0.0;
            if (pendingPayout == null) pendingPayout = 0.0;
            double availableBalance = totalEarned - totalCommission - totalPaidOut - pendingPayout + txn.getAmount();

            if (amount > availableBalance)
                return Map.of("success", false, "message", "Số dư không đủ. Số dư khả dụng: " + Math.round(availableBalance) + "đ");

            txn.setAmount(amount);
            txn.setNetAmount(amount);
            if (description != null && !description.isEmpty())
                txn.setDescription(description + " | TK: " + (store.getBankAccountNumber() != null ? store.getBankAccountNumber() : "Chưa cập nhật")
                        + " - " + (store.getBankName() != null ? store.getBankName() : ""));
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            return Map.of("success", true, "message", "Đã cập nhật yêu cầu đối soát");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/api/finance/cancel-payout/{transactionId}")
    @ResponseBody
    public Map<String, Object> cancelPayout(@PathVariable Long transactionId, Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null || user.getWorkStore() == null)
                return Map.of("success", false, "message", "Không tìm thấy cửa hàng");

            Transaction txn = transactionRepository.findById(transactionId).orElse(null);
            if (txn == null || !txn.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
                return Map.of("success", false, "message", "Yêu cầu không tồn tại");

            if (!"PENDING".equals(txn.getStatus()))
                return Map.of("success", false, "message", "Chỉ có thể hủy yêu cầu đang chờ xử lý");

            if (!"PAYOUT".equals(txn.getType()))
                return Map.of("success", false, "message", "Chỉ có thể hủy yêu cầu đối soát");

            txn.setStatus("FAILED");
            txn.setDescription((txn.getDescription() != null ? txn.getDescription() : "") + " [Đã hủy bởi người bán]");
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            return Map.of("success", true, "message", "Đã hủy yêu cầu đối soát");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi: " + e.getMessage());
        }
    }

    // ============ ANALYTICS ============

    @GetMapping("/analytics")
    public String analytics(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(storeId));
        return "seller/analytics";
    }

    @GetMapping("/api/analytics/overview")
    @ResponseBody
    public Map<String, Object> getAnalyticsOverview(
            @RequestParam(defaultValue = "30") int days, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = now.minusDays(days);

        Double periodRevenue = orderRepository.sumRevenueByPeriod(periodStart, now);
        Long periodOrders = orderRepository.countOrdersByPeriod(periodStart, now);
        Long completedOrders = orderRepository.countByStoreStoreIdAndStatus(storeId, "COMPLETED");
        Long cancelledOrders = orderRepository.countByStoreStoreIdAndStatus(storeId, "CANCELLED");
        long totalOrders = completedOrders + cancelledOrders;
        double completionRate = totalOrders > 0 ? Math.round((double) completedOrders / totalOrders * 100) : 0;

        return Map.of(
            "success", true,
            "periodRevenue", periodRevenue != null ? Math.round(periodRevenue) : 0,
            "periodOrders", periodOrders != null ? periodOrders : 0,
            "avgOrderValue", periodOrders != null && periodOrders > 0 ? Math.round(periodRevenue / periodOrders) : 0,
            "activeDeals", dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size(),
            "totalProducts", productRepository.countByStoreStoreIdAndDeletedFalse(storeId),
            "completionRate", Math.round(completionRate),
            "commissionRate", Math.round(store.getCommissionRate() * 100),
            "totalEarned", Math.round(transactionRepository.sumEarnedByStore(storeId) != null ? transactionRepository.sumEarnedByStore(storeId) : 0)
        );
    }

    @GetMapping("/api/analytics/chart")
    @ResponseBody
    public Map<String, Object> getAnalyticsChart(
            @RequestParam(defaultValue = "14") int days, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> chartData = new java.util.ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
            Double dayRevenue = orderRepository.sumRevenueByPeriod(dayStart, dayEnd);
            Long dayOrders = orderRepository.countOrdersByPeriod(dayStart, dayEnd);
            // Filter for this store's orders only
            // Simple daily label
            Map<String, Object> point = new HashMap<>();
            point.put("date", dayStart.toLocalDate().toString());
            point.put("label", dayStart.getDayOfMonth() + "/" + dayStart.getMonthValue());
            point.put("revenue", dayRevenue != null ? Math.round(dayRevenue) : 0);
            point.put("orders", dayOrders != null ? dayOrders : 0);
            chartData.add(point);
        }

        return Map.of("success", true, "chartData", chartData);
    }

    @GetMapping("/api/analytics/top-products")
    @ResponseBody
    public Map<String, Object> getTopProducts(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        List<Product> products = productRepository.findByStoreStoreIdAndDeletedFalse(storeId);

        List<Map<String, Object>> result = products.stream()
            .sorted((a, b) -> Integer.compare(
                b.getStockQuantity() != null ? b.getStockQuantity() : 0,
                a.getStockQuantity() != null ? a.getStockQuantity() : 0))
            .limit(10)
            .map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("productId", p.getProductId());
                m.put("name", p.getName());
                m.put("originalPrice", Math.round(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0));
                m.put("stockQuantity", p.getStockQuantity());
                m.put("active", p.getActive());
                m.put("approvalStatus", p.getApprovalStatus());
                return m;
            }).toList();

        return Map.of("success", true, "products", result);
    }

    @GetMapping("/api/analytics/order-stats")
    @ResponseBody
    public Map<String, Object> getOrderStats(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        long pending = orderRepository.countByStoreStoreIdAndStatus(storeId, "PENDING");
        long confirmed = orderRepository.countByStoreStoreIdAndStatus(storeId, "CONFIRMED");
        long ready = orderRepository.countByStoreStoreIdAndStatus(storeId, "READY_FOR_PICKUP");
        long completed = orderRepository.countByStoreStoreIdAndStatus(storeId, "COMPLETED");
        long cancelled = orderRepository.countByStoreStoreIdAndStatus(storeId, "CANCELLED");

        return Map.of(
            "success", true,
            "pending", pending, "confirmed", confirmed,
            "ready", ready, "completed", completed, "cancelled", cancelled
        );
    }

    @GetMapping("/api/analytics/recommendations")
    @ResponseBody
    public Map<String, Object> getRecommendations(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return Map.of("error", "Không tìm thấy cửa hàng");

        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> recs = new java.util.ArrayList<>();

        // 1. Check low stock products
        List<Product> lowStock = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> p.getActive() && p.getStockQuantity() != null && p.getStockQuantity() <= 5)
            .toList();
        if (!lowStock.isEmpty()) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "warning");
            rec.put("icon", "📦");
            rec.put("title", "Sản phẩm sắp hết hàng");
            rec.put("detail", lowStock.size() + " sản phẩm còn ≤ 5: " + lowStock.stream().map(Product::getName).limit(3).reduce((a,b)->a+", "+b).orElse(""));
            rec.put("action", "Xem kho");
            rec.put("link", "/seller/products");
            recs.add(rec);
        }

        // 2. Check expiring deals (active deals ending within 3 days)
        List<Deal> expiringDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").stream()
            .filter(d -> d.getEndTime() != null && d.getEndTime().isBefore(now.plusDays(3)) && d.getEndTime().isAfter(now))
            .toList();
        if (!expiringDeals.isEmpty()) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "warning");
            rec.put("icon", "⏰");
            rec.put("title", "Deal sắp hết hạn");
            rec.put("detail", expiringDeals.size() + " deal sắp kết thúc trong 3 ngày");
            rec.put("action", "Xem deals");
            rec.put("link", "/seller/deals");
            recs.add(rec);
        }

        // 3. Check pending approval products
        long pendingProducts = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> "PENDING".equals(p.getApprovalStatus())).count();
        if (pendingProducts > 0) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "info");
            rec.put("icon", "⏳");
            rec.put("title", "Sản phẩm chờ duyệt");
            rec.put("detail", pendingProducts + " sản phẩm đang chờ admin phê duyệt");
            rec.put("action", "Xem kho");
            rec.put("link", "/seller/products?status=pending");
            recs.add(rec);
        }

        // 4. Check inactive products
        long inactiveProducts = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> !p.getActive()).count();
        if (inactiveProducts > 0) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "info");
            rec.put("icon", "🛑");
            rec.put("title", "Sản phẩm đã ngừng bán");
            rec.put("detail", inactiveProducts + " sản phẩm đang bị ẩn khỏi cửa hàng");
            rec.put("action", "Xem kho");
            rec.put("link", "/seller/products?status=inactive");
            recs.add(rec);
        }

        // 5. Tier upgrade suggestion
        if ("BRONZE".equals(store.getPartnerTier())) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "success");
            rec.put("icon", "🏆");
            rec.put("title", "Cơ hội nâng hạng đối tác");
            rec.put("detail", "Tăng doanh số để lên hạng Bạc, giảm phí hoa hồng từ 10% xuống 8%");
            rec.put("action", "Xem hồ sơ");
            rec.put("link", "/seller/profile");
            recs.add(rec);
        }

        // 6. No active deals
        long activeDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE").size();
        if (activeDeals == 0) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("type", "warning");
            rec.put("icon", "💡");
            rec.put("title", "Chưa có deal đang chạy");
            rec.put("detail", "Tạo deal để thu hút khách hàng và tăng doanh số");
            rec.put("action", "Tạo deal");
            rec.put("link", "/seller/deals/create");
            recs.add(rec);
        }

        return Map.of("success", true, "recommendations", recs);
    }

    // ============ STORE PAGE EDITOR ============

    @GetMapping("/store-page")
    public String storePageEditor(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(store.getStoreId(), "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(store.getStoreId()));
        return "seller/store-page";
    }

    @PostMapping("/store-page/update")
    public String updateStorePage(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String operatingDays,
            @RequestParam(required = false) String openTime,
            @RequestParam(required = false) String closeTime,
            @RequestParam(required = false) String pickupSlots,
            @RequestParam(required = false) Integer maxSlotsPerTime,
            @RequestParam(required = false) Integer pickupDurationMinutes,
            @RequestParam(required = false) String approvalMode,
            @RequestParam(required = false) String sectionOrder,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile coverFile,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/seller/profile";
        }

        Store store = user.getWorkStore();
        try {
            if (description != null) store.setDescription(description.trim());
            if (categories != null) store.setCategories(categories.trim());
            if (operatingDays != null) store.setOperatingDays(operatingDays.trim());
            if (openTime != null && !openTime.isEmpty()) store.setOpenTime(java.time.LocalTime.parse(openTime));
            if (closeTime != null && !closeTime.isEmpty()) store.setCloseTime(java.time.LocalTime.parse(closeTime));
            if (pickupSlots != null) store.setPickupSlots(pickupSlots.trim());
            if (maxSlotsPerTime != null) store.setMaxSlotsPerTime(maxSlotsPerTime);
            if (pickupDurationMinutes != null) store.setPickupDurationMinutes(pickupDurationMinutes);
            if (approvalMode != null) store.setApprovalMode(approvalMode);

            if (coverFile != null && !coverFile.isEmpty()) {
                if (!isImageFile(coverFile)) {
                    redirectAttributes.addFlashAttribute("error", "Ảnh bìa phải là JPG, PNG hoặc WEBP");
                    return "redirect:/seller/store-page";
                }
                String coverPath = saveUploadedFile(coverFile);
                store.setCoverImageUrl(coverPath);
            }

            storeRepository.save(store);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trang cửa hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/store-page";
    }

    // ============ PROFILE ============

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("activeDeals", dealRepository.findByStoreStoreIdAndStatus(store.getStoreId(), "ACTIVE").size());
        model.addAttribute("pendingOrders", orderRepository.countPendingOrdersByStore(store.getStoreId()));
        return "seller/profile";
    }

    @PostMapping("/profile/update-store")
    public String updateStore(
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String bankAccountNumber,
            @RequestParam(required = false) String bankAccountOwner,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile logoFile,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/seller/profile";
        }

        Store store = user.getWorkStore();
        try {
            if (storeName != null && !storeName.trim().isEmpty()) store.setStoreName(storeName.trim());
            if (address != null) store.setAddress(address.trim());
            if (city != null) store.setCity(city.trim());
            if (district != null) store.setDistrict(district.trim());
            if (phone != null) store.setPhone(phone.trim());
            if (businessType != null) store.setBusinessType(businessType.trim());
            if (categories != null) store.setCategories(categories.trim());
            if (bankName != null) store.setBankName(bankName.trim());
            if (bankAccountNumber != null) store.setBankAccountNumber(bankAccountNumber.trim());
            if (bankAccountOwner != null) store.setBankAccountOwner(bankAccountOwner.trim());

            if (logoFile != null && !logoFile.isEmpty()) {
                if (!isImageFile(logoFile)) {
                    redirectAttributes.addFlashAttribute("error", "Logo phải là ảnh JPG, PNG hoặc WEBP");
                    return "redirect:/seller/profile";
                }
                String logoPath = saveUploadedFile(logoFile);
                store.setLogoUrl(logoPath);
            }

            storeRepository.save(store);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/profile";
    }

    @PostMapping("/profile/update-documents")
    public String updateDocuments(
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile cccdFile,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile businessLicenseFile,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile vsattpFile,
            Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/seller/profile";
        }

        Store store = user.getWorkStore();
        try {
            // Validate & save each file (max 5MB, images or PDF only)
            if (cccdFile != null && !cccdFile.isEmpty()) {
                String error = validateDocumentFile(cccdFile, "CCCD/CMND");
                if (error != null) {
                    redirectAttributes.addFlashAttribute("error", error);
                    return "redirect:/seller/profile";
                }
                String path = saveUploadedFile(cccdFile);
                store.setCccdUrl(path);
            }
            if (businessLicenseFile != null && !businessLicenseFile.isEmpty()) {
                String error = validateDocumentFile(businessLicenseFile, "Giấy phép kinh doanh");
                if (error != null) {
                    redirectAttributes.addFlashAttribute("error", error);
                    return "redirect:/seller/profile";
                }
                String path = saveUploadedFile(businessLicenseFile);
                store.setBusinessLicenseUrl(path);
            }
            if (vsattpFile != null && !vsattpFile.isEmpty()) {
                String error = validateDocumentFile(vsattpFile, "VSATTP");
                if (error != null) {
                    redirectAttributes.addFlashAttribute("error", error);
                    return "redirect:/seller/profile";
                }
                String path = saveUploadedFile(vsattpFile);
                store.setVsattpUrl(path);
            }

            storeRepository.save(store);
            redirectAttributes.addFlashAttribute("success", "Cập nhật giấy tờ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/profile";
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
            return "redirect:/seller/profile";
        }

        try {
            if (currentPassword == null || currentPassword.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại");
                return "redirect:/seller/profile";
            }
            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "redirect:/seller/profile";
            }
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                return "redirect:/seller/profile";
            }

            // Verify current password
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            if (!encoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/seller/profile";
            }

            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/profile";
    }

    // ============ HELPER METHODS ============

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        String username = principal.getName();
        User user = userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
        // If user is a store owner but workStore is null, look up store by owner
        if (user != null && user.getWorkStore() == null) {
            Store ownedStore = storeRepository.findByOwner(user).orElse(null);
            if (ownedStore != null && "ACTIVE".equals(ownedStore.getStatus())) {
                user.setWorkStore(ownedStore);
            }
        }
        return user;
    }

    private com.dealxanh.app.entity.Role findRoleByName(String withPrefix, String withoutPrefix) {
        com.dealxanh.app.entity.Role role = roleRepository.findByName(withPrefix).orElse(null);
        if (role == null) {
            role = roleRepository.findByName(withoutPrefix).orElse(null);
        }
        return role;
    }
}
