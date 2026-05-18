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
            if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                product.setExpiryDate(java.time.LocalDateTime.parse(expiryDateStr + "T23:59:59"));
            }

            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Tạo sản phẩm thành công! Sản phẩm đang chờ admin duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/products";
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
