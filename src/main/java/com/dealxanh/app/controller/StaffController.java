package com.dealxanh.app.controller;

import com.dealxanh.app.concurrency.ResourceLockManager;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

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
    private com.dealxanh.app.repository.DealProductRepository dealProductRepository;

    @Autowired
    private com.dealxanh.app.repository.DealCategoryRepository dealCategoryRepository;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private com.dealxanh.app.service.ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.dealxanh.app.service.PayOSService payOSService;

    @Autowired
    private ResourceLockManager lockManager;

    @GetMapping({"", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String slot,
            Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();
        Long storeId = store.getStoreId();

        // Pickup slots from store config
        String[] slots = store.getPickupSlots() != null
                ? store.getPickupSlots().split(",")
                : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"};

        // Default active slot = first slot
        String activeSlot = slot != null ? slot.trim() : slots[0].trim();

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

        // Filter by selected slot (time range)
        if (activeSlot != null && activeSlot.contains("-")) {
            String[] parts = activeSlot.split("-");
            try {
                LocalTime slotStart = LocalTime.parse(parts[0].trim());
                LocalTime slotEnd = LocalTime.parse(parts[1].trim());
                Predicate<Order> slotFilter = o -> {
                    // If no scheduled pickup time, show in all slots (not assigned yet)
                    if (o.getScheduledPickupTime() == null) return true;
                    LocalTime orderTime = o.getScheduledPickupTime().toLocalTime();
                    return !orderTime.isBefore(slotStart) && !orderTime.isAfter(slotEnd);
                };
                pendingOrders = pendingOrders.stream().filter(slotFilter).toList();
                confirmedOrders = confirmedOrders.stream().filter(slotFilter).toList();
                readyOrders = readyOrders.stream().filter(slotFilter).toList();
            } catch (Exception ignored) {
                // Invalid slot format, skip filtering
            }
        }

        long servedToday = orderRepository.countByStoreStoreIdAndStatus(storeId, "COMPLETED");
        long pendingToday = pendingOrders.size() + confirmedOrders.size() + readyOrders.size();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("readyOrders", readyOrders);
        model.addAttribute("servedToday", servedToday);
        model.addAttribute("pendingToday", pendingToday);
        model.addAttribute("pickupSlots", slots);
        model.addAttribute("activeSlot", activeSlot);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("weakPassword", user.getWeakPassword());

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
        long cancelledCount = orderRepository.countByStoreStoreIdAndStatus(storeId, "CANCELLED");

        // Fetch orders — current page from DB
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var ordersPage = status != null && !status.isEmpty()
                ? orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable)
                : orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, pageable);

        List<Order> orders = ordersPage.getContent();

        // Filter by search term
        if (search != null && !search.isEmpty()) {
            var allPageable = PageRequest.of(0, 200, Sort.by("createdAt").descending());
            var allOrders = status != null && !status.isEmpty()
                    ? orderRepository.findByStoreStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, allPageable)
                    : orderRepository.findByStoreStoreIdOrderByCreatedAtDesc(storeId, allPageable);
            orders = allOrders.getContent();
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

        int total = (int) ordersPage.getTotalElements();
        if (search != null && !search.isEmpty()) total = orders.size();
        int totalPages = total > 0 ? (int) Math.ceil((double) total / size) : 0;
        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;
        List<Order> paged = orders;

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("orders", paged);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("readyCount", readyCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
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

        // Build deal price map for display
        LocalDateTime now = LocalDateTime.now();
        java.util.Map<Long, java.util.Map<String, Object>> dealProductMap = new java.util.HashMap<>();
        for (Product p : paged) {
            var dps = dealProductRepository.findByProduct(p);
            if (dps != null) {
                for (var dp : dps) {
                    var d = dp.getDeal();
                    if (d == null || !"ACTIVE".equals(d.getStatus()) || !"AUTO_APPLY".equals(d.getApplyMethod())) continue;
                    if (d.getStartTime() != null && d.getStartTime().isAfter(now)) continue;
                    if (d.getEndTime() != null && d.getEndTime().isBefore(now)) continue;
                    var di = new java.util.HashMap<String, Object>();
                    di.put("salePrice", dp.getSalePrice());
                    di.put("originalPrice", dp.getOriginalPrice());
                    di.put("dealName", d.getDealName());
                    di.put("discountType", d.getDiscountType());
                    di.put("discountValue", d.getDiscountValue());
                    // Also check platform deals
                    double platformDisc = 0;
                    if (p.getCategory() != null) {
                        var dcs = dealCategoryRepository.findActiveByCategory(p.getCategory());
                        if (dcs != null) {
                            for (var dc : dcs) {
                                var pd = dc.getDeal();
                                if (pd == null || !"ACTIVE".equals(pd.getStatus()) || !"AUTO_APPLY".equals(pd.getApplyMethod())) continue;
                                if (pd.getStartTime() != null && pd.getStartTime().isAfter(now)) continue;
                                if (pd.getEndTime() != null && pd.getEndTime().isBefore(now)) continue;
                                if ("PERCENT".equals(pd.getDiscountType())) {
                                    double amt = p.getOriginalPrice() * pd.getDiscountValue() / 100.0;
                                    if (pd.getMaxDiscountAmount() != null) amt = Math.min(amt, pd.getMaxDiscountAmount());
                                    platformDisc += amt;
                                } else {
                                    platformDisc += pd.getDiscountValue();
                                }
                            }
                        }
                    }
                    di.put("platformDiscount", Math.round(platformDisc));
                    double fp = ((Number)di.get("salePrice")).doubleValue() - platformDisc;
                    if (fp < p.getOriginalPrice() * 0.15) fp = p.getOriginalPrice() * 0.15;
                    di.put("finalPrice", Math.round(fp));
                    dealProductMap.put(p.getProductId(), di);
                    break; // first active deal wins
                }
            }
        }
        model.addAttribute("dealProductMap", dealProductMap);

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

    // ============ CREATE PRODUCT ============

    @GetMapping("/products/create")
    public String createProductForm(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        Store store = user.getWorkStore();

        model.addAttribute("user", user);
        model.addAttribute("store", store);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("pendingToday",
            orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "PENDING")
            + orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "CONFIRMED")
            + orderRepository.countByStoreStoreIdAndStatus(store.getStoreId(), "READY_FOR_PICKUP"));
        model.addAttribute("pickupSlots", store.getPickupSlots() != null ? store.getPickupSlots().split(",") : new String[]{"16:00-18:00", "17:00-19:00", "18:00-20:00"});
        model.addAttribute("activePage", "products");
        return "staff/create-product";
    }

    @PostMapping("/products/{productId}/toggle")
    public String toggleProduct(@PathVariable Long productId, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) return "redirect:/login";
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.getStore().getStoreId().equals(user.getWorkStore().getStoreId())) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
                return "redirect:/staff/products";
            }
            productService.toggleActive(productId);
            redirectAttributes.addFlashAttribute("success", "Đã ngừng bán sản phẩm: " + product.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/products";
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
            @RequestParam(required = false) MultipartFile imageFile,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy cửa hàng");
            return "redirect:/staff/products";
        }

        try {
            String imgPath = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                String contentType = imageFile.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
                    redirectAttributes.addFlashAttribute("error", "File ảnh phải là JPG, PNG hoặc WEBP");
                    return "redirect:/staff/products/create";
                }
                if (imageFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "File ảnh tối đa 5MB");
                    return "redirect:/staff/products/create";
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
            product.setCreatedBy(user);
            product.setApprovalStatus("APPROVED");
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
                product.setExpiryDate(LocalDateTime.parse(expiryDateStr + "T23:59:59"));
            }

            productService.createProduct(product);
            String msg = "COMBO".equals(productType)
                ? "Tạo combo thành công!"
                : "Tạo sản phẩm thành công!";
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/products";
    }

    // ============ COMBO AVAILABLE API ============

    @GetMapping("/api/products/combo-available")
    @ResponseBody
    public java.util.Map<String, Object> getComboAvailableProducts(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("error", "Không tìm thấy cửa hàng");

        Long storeId = user.getWorkStore().getStoreId();
        List<Product> products = productRepository.findComboAvailableByStore(storeId);

        List<java.util.Map<String, Object>> result = products.stream().map(p -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("productId", p.getProductId());
            m.put("name", p.getName());
            m.put("originalPrice", Math.round(p.getOriginalPrice() != null ? p.getOriginalPrice() : 0));
            m.put("stockQuantity", p.getStockQuantity());
            m.put("expiryDate", p.getExpiryDate() != null ? p.getExpiryDate().toString() : null);
            return m;
        }).toList();

        return java.util.Map.of("products", result);
    }

    // ============ PRODUCT DETAIL API ============

    @GetMapping("/api/products/{productId}/detail")
    @ResponseBody
    public java.util.Map<String, Object> getProductDetail(@PathVariable Long productId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getDeleted())
            return java.util.Map.of("success", false, "message", "Không tìm thấy sản phẩm");
        if (!product.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
            return java.util.Map.of("success", false, "message", "Sản phẩm không thuộc cửa hàng của bạn");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("productId", product.getProductId());
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("imageUrl", product.getImageUrl());
        result.put("originalPrice", product.getOriginalPrice());
        result.put("currentPrice", product.getCurrentPrice());
        result.put("stockQuantity", product.getStockQuantity());
        result.put("productType", product.getProductType());
        result.put("approvalStatus", product.getApprovalStatus());
        result.put("rejectionReason", product.getRejectionReason());
        result.put("categoryName", product.getCategory() != null ? product.getCategory().getName() : null);
        result.put("categoryId", product.getCategory() != null ? product.getCategory().getCategoryId() : null);
        result.put("expiryDate", product.getExpiryDate() != null ? product.getExpiryDate().toString() : null);
        result.put("createdAt", product.getCreatedAt() != null ? product.getCreatedAt().toString() : null);
        result.put("createdByName", product.getCreatedBy() != null
            ? (product.getCreatedBy().getFullName() != null ? product.getCreatedBy().getFullName() : product.getCreatedBy().getUsername())
            : null);

        // --- Deal info (AUTO_APPLY only) ---
        LocalDateTime now = LocalDateTime.now();
        // Store deals
        java.util.List<java.util.Map<String, Object>> storeDeals = new java.util.ArrayList<>();
        java.util.List<com.dealxanh.app.entity.DealProduct> dealProducts = dealProductRepository.findByProduct(product);
        if (dealProducts != null) {
            for (com.dealxanh.app.entity.DealProduct dp : dealProducts) {
                var d = dp.getDeal();
                if (d == null || !"ACTIVE".equals(d.getStatus())) continue;
                if (!"AUTO_APPLY".equals(d.getApplyMethod())) continue;
                if (d.getStartTime() != null && d.getStartTime().isAfter(now)) continue;
                if (d.getEndTime() != null && d.getEndTime().isBefore(now)) continue;
                var di = new java.util.HashMap<String, Object>();
                di.put("dealName", d.getDealName());
                di.put("dealType", d.getDealType());
                di.put("discountType", d.getDiscountType());
                di.put("discountValue", d.getDiscountValue());
                di.put("salePrice", dp.getSalePrice());
                storeDeals.add(di);
            }
        }
        // Platform deals
        java.util.List<java.util.Map<String, Object>> platformDeals = new java.util.ArrayList<>();
        if (product.getCategory() != null) {
            var dealCategories = dealCategoryRepository.findActiveByCategory(product.getCategory());
            if (dealCategories != null) {
                for (var dc : dealCategories) {
                    var d = dc.getDeal();
                    if (d == null || !"ACTIVE".equals(d.getStatus())) continue;
                    if (!"AUTO_APPLY".equals(d.getApplyMethod())) continue;
                    if (d.getStartTime() != null && d.getStartTime().isAfter(now)) continue;
                    if (d.getEndTime() != null && d.getEndTime().isBefore(now)) continue;
                    var di = new java.util.HashMap<String, Object>();
                    di.put("dealName", d.getDealName());
                    di.put("dealType", d.getDealType());
                    di.put("discountType", d.getDiscountType());
                    di.put("discountValue", d.getDiscountValue());
                    double discAmt = 0;
                    if (product.getOriginalPrice() != null) {
                        if ("PERCENT".equals(d.getDiscountType())) {
                            discAmt = product.getOriginalPrice() * d.getDiscountValue() / 100.0;
                            if (d.getMaxDiscountAmount() != null) discAmt = Math.min(discAmt, d.getMaxDiscountAmount());
                        } else {
                            discAmt = d.getDiscountValue();
                        }
                    }
                    di.put("discountAmount", Math.round(discAmt));
                    platformDeals.add(di);
                }
            }
        }
        // Calculate final price
        double orig = product.getOriginalPrice() != null ? product.getOriginalPrice() : 0;
        double storeLowest = orig;
        for (var sd : storeDeals) {
            double sp = ((Number) sd.get("salePrice")).doubleValue();
            if (sp < storeLowest) storeLowest = sp;
        }
        double platformTotal = 0;
        for (var pd : platformDeals) {
            platformTotal += ((Number) pd.get("discountAmount")).doubleValue();
        }
        double finalPrice = storeLowest - platformTotal;
        if (finalPrice < orig * 0.15) finalPrice = orig * 0.15;
        result.put("finalPrice", Math.round(finalPrice));
        result.put("storeDeals", storeDeals);
        result.put("platformDeals", platformDeals);
        return result;
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
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
            jakarta.servlet.http.HttpServletRequest request) {
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
            if (newPassword == null || newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự");
                return "redirect:/staff/profile";
            }
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                return "redirect:/staff/profile";
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/staff/profile";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setWeakPassword(false);
            userRepository.save(user);
            try { request.getSession().invalidate(); } catch (Exception ignored) {}
            return "redirect:/staff/login?logout=password_changed";
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
    @Transactional
    public java.util.Map<String, Object> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus,
            Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null || user.getWorkStore() == null)
            return java.util.Map.of("success", false, "message", "Không tìm thấy cửa hàng");

        ReentrantLock lock = lockManager.acquireLock("ORDER", orderId);
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null || order.getStore() == null || !order.getStore().getStoreId().equals(user.getWorkStore().getStoreId()))
                return java.util.Map.of("success", false, "message", "Đơn hàng không tồn tại");

            String current = order.getStatus();
            boolean valid = false;
            if ("PENDING".equals(current) && ("CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus))) valid = true;
            if ("CONFIRMED".equals(current) && "READY_FOR_PICKUP".equals(newStatus)) valid = true;
            // Allow CONFIRMED->COMPLETED for prepaid orders (buyer paid upfront, skip READY)
            if ("CONFIRMED".equals(current) && "COMPLETED".equals(newStatus) && "PAID".equals(order.getPaymentStatus())) valid = true;
            if ("READY_FOR_PICKUP".equals(current) && "COMPLETED".equals(newStatus)) valid = true;

            if (!valid) return java.util.Map.of("success", false, "message",
                "Không thể chuyển từ " + current + " sang " + newStatus);

            // Pickup time validation for COMPLETED transition
            if ("COMPLETED".equals(newStatus)) {
                java.time.LocalDateTime pickupTime = order.getScheduledPickupTime();
                if (pickupTime != null) {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime windowStart = pickupTime.minusMinutes(30);
                    // Only block if too early; allow completion after the pickup window
                    if (now.isBefore(windowStart)) {
                        return java.util.Map.of("success", false, "message",
                            "Chưa đến giờ nhận hàng. Vui lòng đợi đến " + windowStart.toLocalTime());
                    }
                }
            }

            orderRepository.updateOrderStatus(orderId, newStatus);
            if ("CANCELLED".equals(newStatus)) {
                payOSService.cancelPaymentLink(orderId);
            }
            if ("READY_FOR_PICKUP".equals(newStatus)) {
                orderRepository.updateOrderQrCode(orderId,
                    "DX-" + orderId + "-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            }
            if ("COMPLETED".equals(newStatus)) {
                orderRepository.updateOrderPickupTime(orderId, java.time.LocalDateTime.now());
                // Stock already deducted at order creation (confirmCheckout)
                if (!"PAID".equals(order.getPaymentStatus())) {
                    payOSService.createSaleTransaction(order, order.getPaymentMethod());
                }
            }
            return java.util.Map.of("success", true, "message", "Đã cập nhật trạng thái đơn hàng");
        } finally {
            lock.unlock();
        }
    }

    /** Deduct stock and create sale transaction when staff completes an order */
    private void deductStockAndCreateTransaction(Order order) {
        if (order.getOrderItems() == null) return;
        for (com.dealxanh.app.entity.OrderItem item : order.getOrderItems()) {
            com.dealxanh.app.entity.Product p = item.getProduct();
            if (p != null && p.getStockQuantity() != null) {
                int newStock = p.getStockQuantity() - (item.getQuantity() != null ? item.getQuantity() : 0);
                p.setStockQuantity(Math.max(0, newStock));
                if (newStock <= 0) p.setActive(false);
                productRepository.save(p);
            }
        }
        payOSService.createSaleTransaction(order, order.getPaymentMethod() != null ? order.getPaymentMethod() : "BANK_TRANSFER");
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        String username = principal.getName();
        return userRepository.findByUsernameWithRole(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
    }
}
