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
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        java.nio.file.Path targetPath = uploadPath.resolve(fileName);
        // Nếu file đã tồn tại, dùng lại link cũ
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
}
