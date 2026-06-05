package com.dealxanh.app.controller;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.Category;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Notification;
import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.OrderItem;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.entity.UserAddress;
import com.dealxanh.app.repository.CategoryRepository;
import com.dealxanh.app.repository.DealRepository;
import com.dealxanh.app.repository.DealProductRepository;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserAddressRepository;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.service.CartService;
import com.dealxanh.app.service.LocationService;
import com.dealxanh.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class HomeController {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.dealxanh.app.repository.DisputeRepository disputeRepository;

    @Autowired
    private com.dealxanh.app.repository.ReviewRepository reviewRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ResourceLockManager lockManager;

    @Autowired
    private LocationService locationService;

    /**
     * Global model attribute — adds login state + notification count for all buyer pages served by this controller.
     */
    @ModelAttribute
    public void addNotifCount(Model model, java.security.Principal principal) {
        int count = 0;
        String currentUserFullName = null;
        String currentUserInitials = null;
        if (principal != null) {
            try {
                User user = getCurrentUser(principal);
                if (user != null) {
                    count = (int) notificationService.getUnreadCount(user);
                    currentUserFullName = user.getFullName();
                    if (currentUserFullName != null && !currentUserFullName.trim().isEmpty()) {
                        String[] parts = currentUserFullName.trim().split("\\s+");
                        if (parts.length == 1) {
                            currentUserInitials = parts[0].substring(0, 1).toUpperCase();
                        } else {
                            currentUserInitials = (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("HomeController.addNotifCount: Failed — " + e.getMessage());
            }
        }
        model.addAttribute("notifCount", count);
        model.addAttribute("currentUserFullName", currentUserFullName);
        model.addAttribute("currentUserInitials", currentUserInitials);
    }

    @GetMapping("/")
    public String home(Model model, jakarta.servlet.http.HttpServletRequest request) {
        int cartCount = getCartItemCount(request);
        model.addAttribute("activeNav", "home");
        model.addAttribute("cartItemCount", cartCount);
        // Stats from DB
        model.addAttribute("totalStores", storeRepository.countByStatus("ACTIVE"));
        model.addAttribute("totalUsers", userRepository.countByActive(true));
        model.addAttribute("totalDeals", dealRepository.countActiveDeals());

        LocalDateTime now = LocalDateTime.now();

        // Get all active deals
        List<Deal> activeDeals = dealRepository.findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc(
                "ACTIVE", now, now);

        // Build product-centric lists: each deal's products as individual cards
        List<Map<String, Object>> allProducts = new ArrayList<>();
        List<Map<String, Object>> flashSaleProducts = new ArrayList<>();
        List<Map<String, Object>> comboProducts = new ArrayList<>();

        for (Deal deal : activeDeals) {
            Store store = deal.getStore();
            String storeName = store != null ? store.getStoreName() : "Toàn hệ thống";
            String storeLogo = store != null ? store.getLogoUrl() : null;
            String dealType = deal.getDealType();
            String discountType = deal.getDiscountType();
            double dealDiscountValue = deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
            String endTimeStr = deal.getEndTime() != null ? deal.getEndTime().toString() : null;
            long daysLeft = 0;
            if (deal.getEndTime() != null) {
                daysLeft = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(now, deal.getEndTime()));
            }

            // Get products assigned to this deal
            List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
            if (dealProducts == null) continue;

            for (DealProduct dp : dealProducts) {
                Product prod = dp.getProduct();
                if (prod == null || !prod.isAvailable()) continue;

                Map<String, Object> card = new HashMap<>();
                card.put("productId", prod.getProductId());
                card.put("dealId", deal.getDealId());
                card.put("productName", prod.getName());
                card.put("productImage", prod.getImageUrl());
                card.put("storeName", storeName);
                card.put("storeLogo", storeLogo);
                card.put("storeId", store != null ? store.getStoreId() : null);
                card.put("storeAddress", store != null ? store.getAddress() : "");
                card.put("dealType", dealType);
                card.put("originalPrice", Math.round(dp.getOriginalPrice()));
                card.put("salePrice", Math.round(dp.getSalePrice()));
                card.put("maxQuantity", dp.getMaxQuantity());
                card.put("endTime", endTimeStr);
                card.put("daysLeft", daysLeft);

                // Discount percent for badge
                double discountPercent = 0;
                if ("PERCENT".equals(discountType)) {
                    discountPercent = dealDiscountValue;
                } else if (dp.getOriginalPrice() > 0) {
                    discountPercent = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
                }
                card.put("discountPercent", Math.round(discountPercent));

                // Deal type badge text
                String typeLabel;
                switch (dealType != null ? dealType : "") {
                    case "FLASH_SALE": typeLabel = "Flash Sale"; break;
                    case "VOUCHER": typeLabel = "Voucher"; break;
                    case "COMBO": typeLabel = "Combo"; break;
                    case "SEASONAL": typeLabel = "Seasonal"; break;
                    default: typeLabel = "Deal";
                }
                card.put("typeLabel", typeLabel);

                allProducts.add(card);

                if ("FLASH_SALE".equals(dealType)) {
                    flashSaleProducts.add(card);
                } else if ("COMBO".equals(dealType)) {
                    comboProducts.add(card);
                }
            }
        }

        // Sort products by distance from user (nearest first)
        User homeUser = getCurrentUser(request.getUserPrincipal());
        double[] userLoc = locationService.getUserLocation(request, homeUser);
        if (userLoc != null && !allProducts.isEmpty()) {
            // Add distance to each card
            for (Map<String, Object> card : allProducts) {
                Long sid = card.get("storeId") != null ? ((Number) card.get("storeId")).longValue() : null;
                if (sid != null) {
                    var store = storeRepository.findById(sid).orElse(null);
                    if (store != null) {
                        double dist = locationService.distanceToStore(userLoc, store);
                        card.put("distanceKm", dist >= 0 ? Math.round(dist * 10.0) / 10.0 : -1);
                    }
                }
            }
            // Sort by distance (nearest first, unknown distance last)
            allProducts.sort((a, b) -> {
                double da = ((Number) a.getOrDefault("distanceKm", -1)).doubleValue();
                double db = ((Number) b.getOrDefault("distanceKm", -1)).doubleValue();
                if (da < 0 && db < 0) return 0;
                if (da < 0) return 1;
                if (db < 0) return -1;
                return Double.compare(da, db);
            });
            // Re-filter flash/combo from sorted list
            flashSaleProducts.clear();
            comboProducts.clear();
            for (Map<String, Object> card : allProducts) {
                String dt = (String) card.get("dealType");
                if ("FLASH_SALE".equals(dt)) flashSaleProducts.add(card);
                if ("COMBO".equals(dt)) comboProducts.add(card);
            }
            model.addAttribute("userLocation", userLoc);
        }

        model.addAttribute("allProducts", allProducts);
        model.addAttribute("flashSaleProducts", flashSaleProducts);
        model.addAttribute("hasFlashSale", !flashSaleProducts.isEmpty());
        model.addAttribute("comboProducts", comboProducts);
        model.addAttribute("hasCombo", !comboProducts.isEmpty());

        // Flash sale banner data: earliest endTime & deal name
        if (!flashSaleProducts.isEmpty()) {
            String earliestEnd = null;
            for (Map<String, Object> fp : flashSaleProducts) {
                String et = (String) fp.get("endTime");
                if (et != null && (earliestEnd == null || et.compareTo(earliestEnd) < 0)) {
                    earliestEnd = et;
                }
            }
            model.addAttribute("flashSaleEndTime", earliestEnd);
            model.addAttribute("flashSaleCount", flashSaleProducts.size());
        }

        // Categories for category grid
        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("categories", allCategories);

        return "buyer/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, jakarta.servlet.http.HttpServletRequest request) {
        return home(model, request);
    }

    private int getCartItemCount(jakarta.servlet.http.HttpServletRequest request) {
        java.security.Principal principal = request.getUserPrincipal();
        if (principal == null) return 0;
        User user = getCurrentUser(principal);
        if (user == null) return 0;
        return cartService.getItemCount(user);
    }

    @GetMapping("/category/{categoryId}")
    public String categoryPage(@PathVariable Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String priceRange,
            Model model) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return "redirect:/";

        // Get available products in this category (active, approved, in stock, not deleted, within deal window)
        List<Product> products = productRepository.findAvailableByCategory(categoryId, LocalDateTime.now(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Build featured stores with logo + product count
        java.util.Map<Long, String> storeMap = new java.util.LinkedHashMap<>();
        java.util.List<Map<String, Object>> featuredStores = new java.util.ArrayList<>();
        for (Product p : products) {
            if (p.getStore() != null) {
                storeMap.putIfAbsent(p.getStore().getStoreId(), p.getStore().getStoreName());
            }
        }
        for (Map.Entry<Long, String> entry : storeMap.entrySet()) {
            Store s = storeRepository.findById(entry.getKey()).orElse(null);
            if (s != null) {
                long count = products.stream()
                    .filter(p -> p.getStore() != null && p.getStore().getStoreId().equals(entry.getKey()))
                    .count();
                Map<String, Object> fs = new HashMap<>();
                fs.put("storeId", s.getStoreId());
                fs.put("storeName", s.getStoreName());
                fs.put("logoUrl", s.getLogoUrl() != null ? s.getLogoUrl() : "/img/default-avatar.svg");
                fs.put("productCount", count);
                featuredStores.add(fs);
            }
        }
        model.addAttribute("featuredStores", featuredStores);

        // Get deal products for these products (to show deal pricing)
        LocalDateTime now = LocalDateTime.now();
        List<Deal> activeDeals = dealRepository.findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc(
                "ACTIVE", now, now);
        Map<Long, Map<String, Object>> dealProductMap = new HashMap<>();
        for (Deal deal : activeDeals) {
            List<DealProduct> dps = dealProductRepository.findByDeal(deal);
            if (dps != null) {
                for (DealProduct dp : dps) {
                    if (dp.getProduct() != null && dp.getProduct().getCategory() != null
                        && dp.getProduct().getCategory().getCategoryId().equals(categoryId)) {
                        Long prodId = dp.getProduct().getProductId();
                        double discountPercent = 0;
                        if ("PERCENT".equals(deal.getDiscountType())) {
                            discountPercent = deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
                        } else if (dp.getOriginalPrice() > 0) {
                            discountPercent = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
                        }
                        String typeLabel;
                        switch (deal.getDealType() != null ? deal.getDealType() : "") {
                            case "FLASH_SALE": typeLabel = "Flash Sale"; break;
                            case "VOUCHER": typeLabel = "Voucher"; break;
                            case "COMBO": typeLabel = "Combo"; break;
                            case "SEASONAL": typeLabel = "Seasonal"; break;
                            default: typeLabel = "Deal";
                        }
                        Map<String, Object> info = new HashMap<>();
                        info.put("salePrice", Math.round(dp.getSalePrice()));
                        info.put("originalPrice", Math.round(dp.getOriginalPrice()));
                        info.put("discountPercent", Math.round(discountPercent));
                        info.put("dealType", deal.getDealType());
                        info.put("dealName", deal.getDealName());
                        info.put("typeLabel", typeLabel);
                        info.put("dealId", deal.getDealId());
                        info.put("storeName", deal.getStore() != null ? deal.getStore().getStoreName() : null);
                        info.put("storeLogo", deal.getStore() != null ? deal.getStore().getLogoUrl() : null);
                        info.put("storeId", deal.getStore() != null ? deal.getStore().getStoreId() : null);
                        dealProductMap.put(prodId, info);
                    }
                }
            }
        }

        // Filter by store
        if (storeId != null) {
            products = products.stream()
                .filter(p -> p.getStore() != null && p.getStore().getStoreId().equals(storeId))
                .toList();
        }

        // Filter by search
        if (search != null && !search.trim().isEmpty()) {
            String s = search.toLowerCase().trim();
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(s))
                .toList();
        }

        // Filter by price range (after deal pricing is computed)
        if (priceRange != null && !priceRange.isEmpty()) {
            products = products.stream().filter(p -> {
                double effectivePrice = getEffectivePrice(p, dealProductMap);
                switch (priceRange) {
                    case "under50k": return effectivePrice < 50000;
                    case "50k-100k": return effectivePrice >= 50000 && effectivePrice <= 100000;
                    case "100k-200k": return effectivePrice > 100000 && effectivePrice <= 200000;
                    case "over200k": return effectivePrice > 200000;
                    default: return true;
                }
            }).toList();
        }

        model.addAttribute("category", category);
        model.addAttribute("products", products);
        model.addAttribute("dealProductMap", dealProductMap);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedStoreId", storeId);
        model.addAttribute("selectedPriceRange", priceRange);
        return "buyer/category";
    }

    private double getEffectivePrice(Product p, Map<Long, Map<String, Object>> dealProductMap) {
        Map<String, Object> dealInfo = dealProductMap.get(p.getProductId());
        if (dealInfo != null && dealInfo.get("salePrice") != null) {
            return ((Number) dealInfo.get("salePrice")).doubleValue();
        }
        if (p.getCurrentPrice() != null && p.getCurrentPrice() > 0) return p.getCurrentPrice();
        return p.getOriginalPrice() != null ? p.getOriginalPrice() : 0;
    }

    @GetMapping("/store/{storeId}")
    public String storePage(@PathVariable Long storeId, Model model) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null || !"ACTIVE".equals(store.getStatus())) {
            return "redirect:/home";
        }

        LocalDateTime now = LocalDateTime.now();
        List<Deal> activeDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE");
        activeDeals = activeDeals.stream()
            .filter(d -> d.getStartTime() != null && d.getStartTime().isBefore(now)
                      && d.getEndTime() != null && d.getEndTime().isAfter(now))
            .toList();

        List<Product> products = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> p.getActive() && "APPROVED".equals(p.getApprovalStatus()))
            .toList();

        // Build deal product map for pricing display
        Map<Long, Map<String, Object>> dealProductMap = new HashMap<>();
        for (Deal deal : activeDeals) {
            List<DealProduct> dps = dealProductRepository.findByDeal(deal);
            if (dps != null) {
                for (DealProduct dp : dps) {
                    if (dp.getProduct() == null) continue;
                    Long pid = dp.getProduct().getProductId();
                    double dpct = "PERCENT".equals(deal.getDiscountType()) ? deal.getDiscountValue() : 0;
                    if (dpct == 0 && dp.getOriginalPrice() > 0)
                        dpct = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
                    String typeLabel;
                    switch (deal.getDealType() != null ? deal.getDealType() : "") {
                        case "FLASH_SALE": typeLabel = "⚡ Flash Sale"; break;
                        case "COMBO": typeLabel = "📦 Combo"; break;
                        case "VOUCHER": typeLabel = "🎫 Voucher"; break;
                        case "SEASONAL": typeLabel = "🌟 Seasonal"; break;
                        default: typeLabel = "Deal";
                    }
                    Map<String, Object> di = new HashMap<>();
                    di.put("salePrice", Math.round(dp.getSalePrice()));
                    di.put("originalPrice", Math.round(dp.getOriginalPrice()));
                    di.put("discountPercent", Math.round(dpct));
                    di.put("dealType", deal.getDealType());
                    di.put("dealId", deal.getDealId());
                    di.put("typeLabel", typeLabel);
                    dealProductMap.put(pid, di);
                }
            }
        }

        model.addAttribute("store", store);
        model.addAttribute("activeDeals", activeDeals);
        model.addAttribute("products", products);
        model.addAttribute("dealProductMap", dealProductMap);
        return "buyer/store";
    }

    @GetMapping("/buyer/cart")
    public String cartPage(jakarta.servlet.http.HttpSession session, Model model,
            java.security.Principal principal) {
        model.addAttribute("activeNav", "cart");
        model.addAttribute("isLoggedIn", principal != null);
        return "buyer/cart";
    }

    // ============ CART API (session-based) ============

    @GetMapping("/api/cart")
    @ResponseBody
    public java.util.Map<String, Object> getCart(java.security.Principal principal,
            jakarta.servlet.http.HttpSession session) {
        User user = getCurrentUser(principal);
        if (user == null) {
            java.util.Map<String, Object> empty = new java.util.HashMap<>();
            empty.put("items", java.util.List.of());
            empty.put("count", 0);
            return empty;
        }

        // Migrate any session cart to DB
        cartService.migrateSessionCart(session, user);

        java.util.Map<String, Object> cartData = cartService.getCartData(user);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> voucher = (java.util.Map<String, Object>) session.getAttribute("appliedVoucher");
        cartData.put("voucher", voucher);
        return cartData;
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public java.util.Map<String, Object> addToCart(@RequestParam Long dealId, @RequestParam Long productId,
            @RequestParam String productName, @RequestParam double salePrice, @RequestParam double originalPrice,
            @RequestParam String storeName, @RequestParam(required = false) String productImage,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String dealName,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null)
            return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập để thêm vào giỏ hàng");

        return cartService.addItem(user, dealId, productId, productName, salePrice, originalPrice,
                storeName, productImage, storeId, dealType, dealName);
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public java.util.Map<String, Object> removeFromCart(@RequestParam Long productId,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null)
            return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập");

        return cartService.removeItem(user, productId);
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public java.util.Map<String, Object> updateCartItem(@RequestParam Long productId, @RequestParam int quantity,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null)
            return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập");

        return cartService.updateItemQuantity(user, productId, quantity);
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public java.util.Map<String, Object> cartCount(java.security.Principal principal) {
        User user = getCurrentUser(principal);
        int count = user != null ? cartService.getItemCount(user) : 0;
        return java.util.Map.of("count", count);
    }

    // ============ VOUCHER API ============

    @GetMapping("/api/vouchers/available")
    @ResponseBody
    public java.util.Map<String, Object> availableVouchers() {
        LocalDateTime now = LocalDateTime.now();
        // Use findByDealType with unpaged, then filter by status and other conditions
        List<Deal> vouchers = dealRepository.findByDealType("VOUCHER",
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Deal v : vouchers) {
            if (!"ACTIVE".equals(v.getStatus())) continue;
            if (!"CODE_REQUIRED".equals(v.getApplyMethod())) continue;
            if (v.getStartTime() != null && v.getStartTime().isAfter(now)) continue;
            if (v.getEndTime() != null && v.getEndTime().isBefore(now)) continue;
            Map<String, Object> vi = new HashMap<>();
            vi.put("dealId", v.getDealId());
            vi.put("code", v.getDealCode());
            vi.put("dealName", v.getDealName());
            vi.put("discountType", v.getDiscountType());
            vi.put("discountValue", v.getDiscountValue());
            vi.put("maxDiscountAmount", v.getMaxDiscountAmount());
            vi.put("minOrderAmount", v.getMinOrderAmount());
            vi.put("endTime", v.getEndTime() != null ? v.getEndTime().toString() : null);
            vi.put("storeName", v.getStore() != null ? v.getStore().getStoreName() : "Toàn hệ thống");
            list.add(vi);
        }
        return java.util.Map.of("success", true, "vouchers", list);
    }

    @PostMapping("/api/cart/apply-voucher")
    @ResponseBody
    public java.util.Map<String, Object> applyVoucher(@RequestParam String code,
            jakarta.servlet.http.HttpSession session, java.security.Principal principal) {
        // Get cart data from DB (logged-in) or session (guest)
        java.util.List<java.util.Map<String, Object>> cart = null;
        User user = getCurrentUser(principal);
        if (user != null) {
            cartService.migrateSessionCart(session, user);
            java.util.Map<String, Object> cartData = cartService.getCartData(user);
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> items = (java.util.List<java.util.Map<String, Object>>) cartData.get("items");
            if (items != null) {
                cart = items.stream()
                    .filter(i -> !Boolean.TRUE.equals(i.get("unavailable")))
                    .collect(java.util.stream.Collectors.toList());
            }
        } else {
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> sessionCart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
            cart = sessionCart;
        }
        if (cart == null || cart.isEmpty())
            return java.util.Map.of("success", false, "message", "Giỏ hàng trống");

        // Calculate cart total
        double cartTotal = 0;
        for (java.util.Map<String, Object> item : cart) {
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            double salePrice = ((Number) item.getOrDefault("salePrice", 0)).doubleValue();
            cartTotal += salePrice * qty;
        }

        // Find active voucher by code
        Deal voucher = dealRepository.findByDealCodeAndStatus(code.trim().toUpperCase(), "ACTIVE").orElse(null);
        if (voucher == null)
            return java.util.Map.of("success", false, "message", "Mã giảm giá không tồn tại hoặc đã hết hạn");

        // Must be VOUCHER type with CODE_REQUIRED
        if (!"VOUCHER".equals(voucher.getDealType()) || !"CODE_REQUIRED".equals(voucher.getApplyMethod()))
            return java.util.Map.of("success", false, "message", "Mã này không phải mã giảm giá");

        // Check time validity
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartTime() != null && voucher.getStartTime().isAfter(now))
            return java.util.Map.of("success", false, "message", "Mã giảm giá chưa có hiệu lực");
        if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now))
            return java.util.Map.of("success", false, "message", "Mã giảm giá đã hết hạn");

        // Check max usage count (total across all users)
        if (voucher.getMaxUsageCount() != null) {
            long used = orderRepository.countByCouponCode(code.trim().toUpperCase());
            if (used >= voucher.getMaxUsageCount())
                return java.util.Map.of("success", false, "message", "Mã giảm giá đã hết lượt sử dụng");
        }

        // Check per-user usage (each account can only use this voucher once)
        if (user != null && orderRepository.existsByUserUserIdAndCouponCode(user.getUserId(), code.trim().toUpperCase()))
            return java.util.Map.of("success", false, "message", "Bạn đã sử dụng mã giảm giá này rồi");

        // Check minimum order amount
        double minOrder = voucher.getMinOrderAmount() != null ? voucher.getMinOrderAmount() : 0;
        if (cartTotal < minOrder)
            return java.util.Map.of("success", false, "message",
                "Đơn tối thiểu " + String.format("%,.0f", minOrder) + "đ để áp dụng mã này");

        // Calculate discount
        double voucherDiscount = 0;
        if ("PERCENT".equals(voucher.getDiscountType())) {
            voucherDiscount = cartTotal * (voucher.getDiscountValue() / 100.0);
            double maxDiscount = voucher.getMaxDiscountAmount() != null ? voucher.getMaxDiscountAmount() : 999999999;
            if (voucherDiscount > maxDiscount) voucherDiscount = maxDiscount;
        } else {
            voucherDiscount = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : 0;
        }
        if (voucherDiscount > cartTotal) voucherDiscount = cartTotal;

        // Store voucher in session
        java.util.Map<String, Object> voucherInfo = new java.util.HashMap<>();
        voucherInfo.put("dealId", voucher.getDealId());
        voucherInfo.put("code", code.trim().toUpperCase());
        voucherInfo.put("dealName", voucher.getDealName());
        voucherInfo.put("discountType", voucher.getDiscountType());
        voucherInfo.put("discountValue", voucher.getDiscountValue());
        voucherInfo.put("voucherDiscount", Math.round(voucherDiscount * 100.0) / 100.0);
        session.setAttribute("appliedVoucher", voucherInfo);

        return java.util.Map.of(
            "success", true,
            "message", "Đã áp dụng mã " + code.trim().toUpperCase(),
            "data", voucherInfo
        );
    }

    @PostMapping("/api/cart/remove-voucher")
    @ResponseBody
    public java.util.Map<String, Object> removeVoucher(jakarta.servlet.http.HttpSession session) {
        session.removeAttribute("appliedVoucher");
        return java.util.Map.of("success", true, "message", "Đã xoá mã giảm giá");
    }

    /** Lưu danh sách product IDs được chọn để checkout */
    @PostMapping("/api/cart/checkout-selection")
    @ResponseBody
    public java.util.Map<String, Object> saveCheckoutSelection(
            @RequestBody java.util.Map<String, Object> body,
            jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> ids = (java.util.List<Integer>) body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return java.util.Map.of("success", false, "message", "Vui lòng chọn ít nhất 1 sản phẩm");
        }
        // Convert to Long list for consistency
        java.util.List<Long> longIds = ids.stream().map(Integer::longValue).toList();
        session.setAttribute("checkoutItemIds", longIds);
        return java.util.Map.of("success", true, "message", "Đã lưu lựa chọn");
    }

    // ============ PRODUCT DETAIL ============

    @GetMapping("/products/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.isAvailable()) return "redirect:/";
        if (!"APPROVED".equals(product.getApprovalStatus())) return "redirect:/";

        // Get active deal info for this product
        LocalDateTime now = LocalDateTime.now();
        List<DealProduct> dealProducts = dealProductRepository.findByProduct(product);
        Map<String, Object> dealInfo = null;
        if (dealProducts != null) {
            for (DealProduct dp : dealProducts) {
                Deal deal = dp.getDeal();
                if (deal != null && "ACTIVE".equals(deal.getStatus())
                        && deal.getStartTime() != null && deal.getStartTime().isBefore(now)
                        && deal.getEndTime() != null && deal.getEndTime().isAfter(now)) {
                    dealInfo = new HashMap<>();
                    dealInfo.put("dealId", deal.getDealId());
                    dealInfo.put("dealName", deal.getDealName());
                    dealInfo.put("dealType", deal.getDealType());
                    dealInfo.put("discountType", deal.getDiscountType());
                    dealInfo.put("discountValue", deal.getDiscountValue());
                    dealInfo.put("salePrice", Math.round(dp.getSalePrice()));
                    dealInfo.put("originalPrice", Math.round(dp.getOriginalPrice()));
                    double discountPercent = 0;
                    if ("PERCENT".equals(deal.getDiscountType())) {
                        discountPercent = deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
                    } else if (dp.getOriginalPrice() > 0) {
                        discountPercent = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
                    }
                    dealInfo.put("discountPercent", Math.round(discountPercent));
                    String typeLabel;
                    switch (deal.getDealType() != null ? deal.getDealType() : "") {
                        case "FLASH_SALE": typeLabel = "Flash Sale"; break;
                        case "VOUCHER": typeLabel = "Voucher"; break;
                        case "COMBO": typeLabel = "Combo"; break;
                        case "SEASONAL": typeLabel = "Seasonal"; break;
                        default: typeLabel = "Deal";
                    }
                    dealInfo.put("typeLabel", typeLabel);
                    dealInfo.put("storeName", deal.getStore() != null ? deal.getStore().getStoreName() : null);
                    dealInfo.put("storeLogo", deal.getStore() != null ? deal.getStore().getLogoUrl() : null);
                    break;
                }
            }
        }

        Store store = product.getStore();
        Category category = product.getCategory();

        model.addAttribute("product", product);
        model.addAttribute("dealInfo", dealInfo);
        model.addAttribute("store", store);
        model.addAttribute("category", category);
        if (product.getExpiryDate() != null) {
            model.addAttribute("expiryDate", product.getExpiryDate().toString());
        }

        // Related products from same store (exclude current product, up to 6)
        if (store != null) {
            List<Product> relatedProducts = productRepository
                .findByStoreStoreIdAndDeletedFalse(store.getStoreId()).stream()
                .filter(p -> p.isAvailable() && !p.getProductId().equals(productId))
                .limit(6)
                .toList();
            model.addAttribute("relatedProducts", relatedProducts);
        }

        return "buyer/product-detail";
    }

    // ============ DEAL DETAIL ============

    @GetMapping("/deals/{dealId}")
    public String dealDetail(@PathVariable Long dealId, Model model) {
        Deal deal = dealRepository.findById(dealId).orElse(null);
        if (deal == null) return "redirect:/";

        LocalDateTime now = LocalDateTime.now();
        List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
        if (dealProducts == null) dealProducts = List.of();

        // Build product list with prices
        List<Map<String, Object>> products = new ArrayList<>();
        for (DealProduct dp : dealProducts) {
            if (dp.getProduct() == null || !dp.getProduct().getActive()) continue;
            Map<String, Object> pm = new HashMap<>();
            pm.put("productId", dp.getProduct().getProductId());
            pm.put("name", dp.getProduct().getName());
            pm.put("description", dp.getProduct().getDescription());
            pm.put("imageUrl", dp.getProduct().getImageUrl());
            pm.put("originalPrice", Math.round(dp.getOriginalPrice()));
            pm.put("salePrice", Math.round(dp.getSalePrice()));
            pm.put("maxQuantity", dp.getMaxQuantity());
            double discountPercent = 0;
            if ("PERCENT".equals(deal.getDiscountType())) {
                discountPercent = deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
            } else if (dp.getOriginalPrice() > 0) {
                discountPercent = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
            }
            pm.put("discountPercent", Math.round(discountPercent));
            products.add(pm);
        }

        // Time remaining
        long daysLeft = 0;
        if (deal.getEndTime() != null) {
            daysLeft = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(now, deal.getEndTime()));
        }

        // Store
        Store store = deal.getStore();

        model.addAttribute("deal", deal);
        model.addAttribute("products", products);
        model.addAttribute("daysLeft", daysLeft);
        model.addAttribute("store", store);
        model.addAttribute("dealTypeLabel", getTypeLabel(deal.getDealType()));
        return "buyer/deal-detail";
    }

    private String getTypeLabel(String type) {
        if (type == null) return "Deal";
        switch (type) {
            case "FLASH_SALE": return "⚡ Flash Sale";
            case "COMBO": return "📦 Combo";
            case "VOUCHER": return "🎫 Voucher";
            case "SEASONAL": return "🌟 Seasonal";
            default: return type;
        }
    }

    // ============ PICKUP CONFIRMATION (QR scan) ============

    @GetMapping("/pickup/{qrCode}")
    public String pickupConfirm(@PathVariable String qrCode, Model model) {
        Order order = orderRepository.findByPickupQrCode(qrCode).orElse(null);
        if (order == null) {
            model.addAttribute("error", "Mã QR không hợp lệ hoặc đơn hàng không tồn tại");
            return "buyer/pickup-confirm";
        }
        if ("COMPLETED".equals(order.getStatus())) {
            model.addAttribute("alreadyCompleted", true);
            model.addAttribute("order", order);
            return "buyer/pickup-confirm";
        }
        if (!"READY_FOR_PICKUP".equals(order.getStatus())) {
            model.addAttribute("error", "Đơn hàng chưa sẵn sàng để nhận. Trạng thái hiện tại: " + order.getStatus());
            model.addAttribute("order", order);
            return "buyer/pickup-confirm";
        }
        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "buyer/pickup-confirm";
    }

    @PostMapping("/pickup/{qrCode}/confirm")
    @ResponseBody
    public java.util.Map<String, Object> confirmPickup(@PathVariable String qrCode) {
        Order order = orderRepository.findByPickupQrCode(qrCode).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Mã QR không hợp lệ");

        ReentrantLock lock = lockManager.acquireLock("ORDER", order.getOrderId());
        try {
            // Re-read to get latest state
            order = orderRepository.findById(order.getOrderId()).orElse(null);
            if (order == null) return java.util.Map.of("success", false, "message", "Đơn hàng không tồn tại");
            if ("COMPLETED".equals(order.getStatus()))
                return java.util.Map.of("success", false, "message", "Đơn hàng đã được nhận trước đó");
            if (!"READY_FOR_PICKUP".equals(order.getStatus()))
                return java.util.Map.of("success", false, "message", "Đơn hàng chưa sẵn sàng để nhận");
            order.setStatus("COMPLETED");
            order.setActualPickupTime(java.time.LocalDateTime.now());
            orderRepository.save(order);
            return java.util.Map.of("success", true, "message", "Xác nhận nhận hàng thành công! Cảm ơn bạn đã mua sắm tại DealXanh.");
        } finally {
            lock.unlock();
        }
    }

    // ============ BUYER PROFILE ============

    @GetMapping("/buyer/profile")
    public String buyerProfile(java.security.Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";
        model.addAttribute("activeNav", "account");
        model.addAttribute("user", user);

        // Order stats
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        long totalOrders = orders != null ? orders.size() : 0;
        long completedOrders = orders != null ? orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count() : 0;
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("pendingOrders", totalOrders - completedOrders);
        model.addAttribute("recentOrders", orders != null ? orders.stream().limit(5).toList() : List.of());

        return "buyer/profile";
    }

    // ============ CHECKOUT FLOW ============

    @GetMapping("/buyer/checkout")
    public String checkout(java.security.Principal principal, Model model,
            jakarta.servlet.http.HttpSession session,
            jakarta.servlet.http.HttpServletRequest request) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        // Get cart from DB (migrate session cart first if needed)
        cartService.migrateSessionCart(session, user);
        List<Map<String, Object>> cart = cartService.getCartItemsForCheckout(user);
        if (cart == null || cart.isEmpty()) return "redirect:/buyer/cart";

        // Filter by selected product IDs (from cart page checkboxes)
        @SuppressWarnings("unchecked")
        java.util.List<Long> selectedIds = (java.util.List<Long>) session.getAttribute("checkoutItemIds");
        if (selectedIds != null && !selectedIds.isEmpty()) {
            cart = cart.stream()
                .filter(item -> {
                    Object pid = item.get("productId");
                    return pid != null && selectedIds.contains(((Number) pid).longValue());
                })
                .toList();
            session.removeAttribute("checkoutItemIds");
        }
        if (cart.isEmpty()) return "redirect:/buyer/cart";

        // Group cart items by store
        Map<Long, List<Map<String, Object>>> storeGroups = new java.util.LinkedHashMap<>();
        Map<Long, Map<String, Object>> storeInfo = new java.util.LinkedHashMap<>();
        long subtotal = 0;
        long totalDiscount = 0;
        int itemCount = 0;

        for (Map<String, Object> item : cart) {
            Long storeId = item.get("storeId") != null ? ((Number) item.get("storeId")).longValue() : null;
            if (storeId == null) continue;
            storeGroups.computeIfAbsent(storeId, k -> new ArrayList<>()).add(item);
            itemCount++;
        }

        for (Long storeId : storeGroups.keySet()) {
            Store s = storeRepository.findById(storeId).orElse(null);
            if (s != null) {
                Map<String, Object> si = new HashMap<>();
                si.put("storeId", s.getStoreId());
                si.put("storeName", s.getStoreName());
                si.put("logoUrl", s.getLogoUrl());
                si.put("address", s.getAddress());
                // Calculate distance from user to store
                double[] userLoc = locationService.getUserLocation(request, user);
                double dist = userLoc != null ? locationService.distanceToStore(userLoc, s) : -1;
                si.put("distanceKm", dist >= 0 ? Math.round(dist * 10.0) / 10.0 : -1);
                storeInfo.put(storeId, si);
            }
        }

        // Calculate totals (FIXED deals: once per unique deal; PERCENT deals: per-item)
        java.util.Set<Long> fixedDealIds = new java.util.HashSet<>();
        long fixedDealDiscount = 0;
        long originalTotal = 0; // true original price sum (before any discount)
        long percentDiscount = 0; // savings from PERCENT deals only
        for (Map<String, Object> item : cart) {
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            long salePrice = ((Number) item.getOrDefault("salePrice", item.getOrDefault("price", 0))).longValue();
            long originalPrice = ((Number) item.getOrDefault("originalPrice", salePrice)).longValue();
            String discountType = (String) item.getOrDefault("discountType", "");
            Long dealId = item.get("dealId") != null ? ((Number) item.get("dealId")).longValue() : 0;
            subtotal += salePrice * qty;
            originalTotal += originalPrice * qty;
            if ("FIXED".equals(discountType) && dealId > 0 && !fixedDealIds.contains(dealId)) {
                fixedDealDiscount += ((Number) item.getOrDefault("discountValue", 0)).longValue();
                fixedDealIds.add(dealId);
            } else if (!"FIXED".equals(discountType) && originalPrice > salePrice) {
                percentDiscount += (originalPrice - salePrice) * qty;
            }
        }
        totalDiscount = percentDiscount + fixedDealDiscount;
        // Final amount: subtotal already has PERCENT discount applied,
        // but FIXED items are at originalPrice, so subtract FIXED discount
        long total = subtotal - fixedDealDiscount;

        // Get applied voucher from session
        @SuppressWarnings("unchecked")
        Map<String, Object> appliedVoucher = (Map<String, Object>) session.getAttribute("appliedVoucher");
        long voucherDiscount = 0;
        if (appliedVoucher != null) {
            voucherDiscount = ((Number) appliedVoucher.getOrDefault("voucherDiscount", 0)).longValue();
            total -= voucherDiscount;
            if (total < 0) total = 0;
        }

        model.addAttribute("storeGroups", storeGroups);
        model.addAttribute("storeInfo", storeInfo);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("appliedVoucher", appliedVoucher);
        model.addAttribute("total", total);
        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("cart", cart);

        return "buyer/order-summary";
    }

    @Transactional
    @PostMapping("/buyer/checkout/confirm")
    public String confirmCheckout(java.security.Principal principal,
            @RequestParam(required = false, defaultValue = "BANK_TRANSFER") String paymentMethod,
            @RequestParam(required = false) String scheduledPickupTime,
            jakarta.servlet.http.HttpSession session) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        // FIFO lock: prevent double-checkout from same user
        ReentrantLock lock = lockManager.acquireLock("CART", user.getUserId());
        try {
            // Get cart from DB (only available items)
            List<Map<String, Object>> cart = cartService.getCartItemsForCheckout(user);
            if (cart == null || cart.isEmpty()) return "redirect:/buyer/cart";

            // Group cart items by store
            Map<Long, List<Map<String, Object>>> storeGroups = new java.util.LinkedHashMap<>();
            for (Map<String, Object> item : cart) {
                Long storeId = item.get("storeId") != null ? ((Number) item.get("storeId")).longValue() : null;
                if (storeId == null) continue;
                storeGroups.computeIfAbsent(storeId, k -> new ArrayList<>()).add(item);
            }

            // Get applied voucher from session
            @SuppressWarnings("unchecked")
            Map<String, Object> appliedVoucher = (Map<String, Object>) session.getAttribute("appliedVoucher");
            String voucherCode = null;
            double voucherDiscountTotal = 0;
            if (appliedVoucher != null) {
                voucherCode = (String) appliedVoucher.get("code");
                voucherDiscountTotal = ((Number) appliedVoucher.getOrDefault("voucherDiscount", 0)).doubleValue();
            }

            List<Long> orderIds = new ArrayList<>();

            for (Map.Entry<Long, List<Map<String, Object>>> entry : storeGroups.entrySet()) {
                Long storeId = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                Store store = storeRepository.findById(storeId).orElse(null);
                if (store == null) continue;

                Order order = new Order();
                order.setUser(user);
                order.setStore(store);
                order.setStatus("PENDING");
                order.setPaymentStatus("UNPAID");
                order.setPaymentMethod(paymentMethod);
                order.setCreatedAt(LocalDateTime.now());
                if (scheduledPickupTime != null && !scheduledPickupTime.isEmpty()) {
                    try {
                        order.setScheduledPickupTime(LocalDateTime.parse(scheduledPickupTime));
                    } catch (Exception e) {
                        // Ignore invalid format, leave null
                    }
                }

                double totalAmount = 0;
                double discountAmount = 0;
                List<OrderItem> orderItems = new ArrayList<>();

                // Collect unique FIXED deals in this store group to apply once
                java.util.Set<Long> appliedFixedDealIds = new java.util.HashSet<>();
                double fixedDealDiscount = 0;

                for (Map<String, Object> item : items) {
                    int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
                    double salePrice = ((Number) item.getOrDefault("salePrice", 0)).doubleValue();
                    double originalPrice = ((Number) item.getOrDefault("originalPrice", salePrice)).doubleValue();
                    Long productId = item.get("productId") != null ? ((Number) item.get("productId")).longValue() : null;

                    totalAmount += originalPrice * qty;
                    // PERCENT deals already applied per-item; FIXED deals applied once below
                    String discountType = (String) item.getOrDefault("discountType", "");
                    Long dealId = item.get("dealId") != null ? ((Number) item.get("dealId")).longValue() : 0;
                    if ("FIXED".equals(discountType) && dealId > 0 && !appliedFixedDealIds.contains(dealId)) {
                        double dv = ((Number) item.getOrDefault("discountValue", 0)).doubleValue();
                        fixedDealDiscount += dv;
                        appliedFixedDealIds.add(dealId);
                    } else if (!"FIXED".equals(discountType) && originalPrice > salePrice) {
                        discountAmount += (originalPrice - salePrice) * qty;
                    }

                    if (productId != null) {
                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            OrderItem oi = new OrderItem();
                            oi.setOrder(order);
                            oi.setProduct(product);
                            oi.setQuantity(qty);
                            // For FIXED deals: unitPrice = originalPrice (discount applied at order level)
                            if ("FIXED".equals(discountType) && dealId > 0) {
                                oi.setUnitPrice(originalPrice);
                            } else {
                                oi.setUnitPrice(salePrice);
                            }
                            orderItems.add(oi);
                        }
                    }
                }

                discountAmount += fixedDealDiscount;
                double finalAmount = totalAmount - discountAmount;
                if (voucherCode != null && voucherDiscountTotal > 0 && orderIds.isEmpty()) {
                    double vd = Math.min(voucherDiscountTotal, finalAmount);
                    discountAmount += vd;
                    finalAmount -= vd;
                    order.setCouponCode(voucherCode);
                }

                order.setTotalAmount(totalAmount);
                order.setDiscountAmount(discountAmount);
                order.setFinalAmount(finalAmount);
                order.setOrderItems(orderItems);
                order.setPickupQrCode("DX-TMP-" + generateRandomCode(6));

                order = orderRepository.save(order);
                order.setPickupQrCode("DX-" + order.getOrderId() + "-" + generateRandomCode(6));
                orderRepository.save(order);

                // Deduct stock immediately using @Modifying query (avoids version conflict)
                for (OrderItem oi : orderItems) {
                    if (oi.getProduct() != null && oi.getQuantity() != null && oi.getQuantity() > 0) {
                        productRepository.deductStock(oi.getProduct().getProductId(), oi.getQuantity());
                    }
                }

                orderIds.add(order.getOrderId());
            }

            cartService.clearCart(user);
            session.removeAttribute("appliedVoucher");
            session.setAttribute("lastOrderIds", orderIds);

            return "redirect:/buyer/order-complete";
        } finally {
            lock.unlock();
        }
    }

    private String generateRandomCode(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    // ============ BUYER CANCEL ORDER ============

    @PostMapping("/api/orders/{orderId}/cancel")
    @ResponseBody
    public java.util.Map<String, Object> buyerCancelOrder(@PathVariable Long orderId,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập");

        Order order = orderRepository.findByIdWithItems(orderId).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Đơn hàng không tồn tại");
        if (!order.getUser().getUserId().equals(user.getUserId()))
            return java.util.Map.of("success", false, "message", "Bạn không sở hữu đơn hàng này");
        if (!"PENDING".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Chỉ được hủy đơn đang chờ xác nhận");

        // Restore stock for each item
        if (order.getOrderItems() != null) {
            for (var item : order.getOrderItems()) {
                if (item.getProduct() != null && item.getQuantity() != null) {
                    productRepository.restoreStock(item.getProduct().getProductId(), item.getQuantity());
                }
            }
        }

        orderRepository.cancelOrder(orderId, "Người mua tự hủy");
        return java.util.Map.of("success", true, "message", "Đã hủy đơn hàng, hàng đã được trả về kho");
    }

    // ============ PAYMENT ============

    @GetMapping("/buyer/payment")
    public String paymentPage(java.security.Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "buyer/payment";
    }

    // ============ ORDER COMPLETE ============

    @GetMapping("/buyer/order-complete")
    public String orderComplete(java.security.Principal principal, Model model,
            jakarta.servlet.http.HttpSession session) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<Long> orderIds = (List<Long>) session.getAttribute("lastOrderIds");
        session.removeAttribute("lastOrderIds");

        List<Order> completedOrders = new ArrayList<>();
        if (orderIds != null) {
            for (Long id : orderIds) {
                orderRepository.findById(id).ifPresent(completedOrders::add);
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("completedOrders", completedOrders);
        return "buyer/order-complete";
    }

    // ============ ORDER TRACKING ============

    @GetMapping("/buyer/orders")
    public String orderTracking(java.security.Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?require_login=true";
        model.addAttribute("activeNav", "orders");

        try {
            List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
            model.addAttribute("user", user);
            model.addAttribute("orders", orders != null ? orders : List.of());
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("orders", List.of());
        }
        return "buyer/order-tracking";
    }

    // ============ DISPUTE (BUYER) ============

    @PostMapping("/api/dispute/create")
    @ResponseBody
    public java.util.Map<String, Object> createDispute(
            @RequestParam Long orderId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Đơn hàng không tồn tại");
        if (!order.getUser().getUserId().equals(user.getUserId()))
            return java.util.Map.of("success", false, "message", "Bạn không sở hữu đơn hàng này");
        if (!"COMPLETED".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Chỉ có thể khiếu nại đơn hàng đã hoàn thành");

        // Check if already disputed
        List<com.dealxanh.app.entity.Dispute> existing = disputeRepository.findAll();
        boolean alreadyDisputed = existing.stream()
            .anyMatch(d -> d.getOrder() != null && d.getOrder().getOrderId().equals(orderId)
                && !"RESOLVED_REJECTED".equals(d.getStatus()));
        if (alreadyDisputed)
            return java.util.Map.of("success", false, "message", "Đơn hàng này đã được khiếu nại");

        com.dealxanh.app.entity.Dispute dispute = new com.dealxanh.app.entity.Dispute();
        dispute.setOrder(order);
        dispute.setComplainant(user);
        dispute.setReason(reason);
        dispute.setDescription(description != null ? description : "");
        dispute.setStatus("PENDING");
        dispute.setCreatedAt(LocalDateTime.now());
        dispute.setUpdatedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        return java.util.Map.of("success", true, "message", "Đã gửi khiếu nại. Chúng tôi sẽ xử lý trong 36-42 giờ.");
    }

    // ============ REVIEW (BUYER) ============

    @PostMapping("/api/review/create")
    @ResponseBody
    public java.util.Map<String, Object> createReview(
            @RequestParam Long storeId,
            @RequestParam(required = false) Long orderId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return java.util.Map.of("success", false, "message", "Vui lòng đăng nhập");

        if (rating < 1 || rating > 5)
            return java.util.Map.of("success", false, "message", "Đánh giá từ 1-5 sao");

        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) return java.util.Map.of("success", false, "message", "Cửa hàng không tồn tại");

        // Check if already reviewed for this order
        if (orderId != null && reviewRepository.existsByUserUserIdAndOrderOrderId(user.getUserId(), orderId))
            return java.util.Map.of("success", false, "message", "Bạn đã đánh giá đơn hàng này rồi");

        com.dealxanh.app.entity.Review review = new com.dealxanh.app.entity.Review();
        review.setUser(user);
        review.setStore(store);
        review.setRating(rating);
        review.setComment(comment != null ? comment : "");
        review.setVerified(orderId != null); // Verified if from an order
        review.setCreatedAt(LocalDateTime.now());
        if (orderId != null) {
            orderRepository.findById(orderId).ifPresent(review::setOrder);
        }
        reviewRepository.save(review);

        return java.util.Map.of("success", true, "message", "Cảm ơn bạn đã đánh giá!");
    }

    @GetMapping("/api/reviews/store/{storeId}")
    @ResponseBody
    public java.util.Map<String, Object> getStoreReviews(@PathVariable Long storeId) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        var page = reviewRepository.findByStoreStoreId(storeId, pageable);
        Double avg = reviewRepository.findAverageRatingByStore(storeId);
        long total = reviewRepository.countByStoreStoreId(storeId);

        var list = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (var r : page.getContent()) {
            var m = new java.util.HashMap<String, Object>();
            m.put("reviewId", r.getReviewId());
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("verified", r.getVerified());
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            m.put("userName", r.getUser() != null ? (r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getUsername()) : "Người dùng");
            list.add(m);
        }
        return java.util.Map.of("success", true, "reviews", list, "avgRating", avg != null ? avg : 0, "totalReviews", total);
    }

    @GetMapping("/buyer/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId, java.security.Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
            return "redirect:/buyer/orders";
        }

        model.addAttribute("user", user);
        model.addAttribute("order", order);
        return "buyer/order-detail";
    }

    // ============ SEARCH ============

    @GetMapping("/buyer/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String dealType,
                         @RequestParam(required = false) String priceRange,
                         @RequestParam(required = false) String sort, Model model) {
        model.addAttribute("searchQuery", q);
        model.addAttribute("selectedDealType", dealType);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedSort", sort);
        if (q != null && !q.trim().isEmpty()) {
            String s = q.toLowerCase().trim();

            // Search products by name
            List<Product> results = productRepository.findAll().stream()
                .filter(p -> p.getActive() && !p.getDeleted()
                    && "APPROVED".equals(p.getApprovalStatus())
                    && (p.getName().toLowerCase().contains(s)))
                .limit(80)
                .toList();

            // Search stores by name
            List<Store> matchingStores = storeRepository.findAll().stream()
                .filter(st -> "ACTIVE".equals(st.getStatus()))
                .filter(st -> st.getStoreName() != null && st.getStoreName().toLowerCase().contains(s))
                .limit(10)
                .toList();

            // Get deal info for results
            LocalDateTime now = LocalDateTime.now();
            List<Deal> activeDeals = dealRepository.findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc("ACTIVE", now, now);
            Map<Long, Map<String, Object>> dealProductMap = new HashMap<>();
            for (Deal deal : activeDeals) {
                for (DealProduct dp : dealProductRepository.findByDeal(deal)) {
                    if (dp.getProduct() != null) {
                        Long pid = dp.getProduct().getProductId();
                        double dpct = "PERCENT".equals(deal.getDiscountType()) ? deal.getDiscountValue() : 0;
                        if (dpct == 0 && dp.getOriginalPrice() > 0)
                            dpct = ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100;
                        Map<String, Object> di = new HashMap<>();
                        di.put("salePrice", Math.round(dp.getSalePrice()));
                        di.put("originalPrice", Math.round(dp.getOriginalPrice()));
                        di.put("discountPercent", Math.round(dpct));
                        di.put("dealType", deal.getDealType());
                        di.put("dealId", deal.getDealId());
                        di.put("storeName", deal.getStore() != null ? deal.getStore().getStoreName() : null);
                        dealProductMap.put(pid, di);
                    }
                }
            }

            // Filter by deal type
            if (dealType != null && !dealType.isEmpty() && !"all".equals(dealType)) {
                results = results.stream().filter(p -> {
                    Map<String, Object> di = dealProductMap.get(p.getProductId());
                    return di != null && dealType.equals(di.get("dealType"));
                }).toList();
            }

            // Filter by price range
            if (priceRange != null && !priceRange.isEmpty() && !"all".equals(priceRange)) {
                results = results.stream().filter(p -> {
                    Map<String, Object> di = dealProductMap.get(p.getProductId());
                    double price = di != null ? ((Number) di.get("salePrice")).doubleValue() : p.getOriginalPrice();
                    switch (priceRange) {
                        case "under50k": return price < 50000;
                        case "50k-100k": return price >= 50000 && price < 100000;
                        case "100k-200k": return price >= 100000 && price < 200000;
                        case "over200k": return price >= 200000;
                        default: return true;
                    }
                }).toList();
            }

            // Sort
            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "price-asc":
                        results = results.stream().sorted((a, b) -> {
                            double pa = getEffectivePrice(a, dealProductMap);
                            double pb = getEffectivePrice(b, dealProductMap);
                            return Double.compare(pa, pb);
                        }).toList();
                        break;
                    case "price-desc":
                        results = results.stream().sorted((a, b) -> {
                            double pa = getEffectivePrice(a, dealProductMap);
                            double pb = getEffectivePrice(b, dealProductMap);
                            return Double.compare(pb, pa);
                        }).toList();
                        break;
                    case "discount":
                        results = results.stream().sorted((a, b) -> {
                            double da = getDiscountPercent(a, dealProductMap);
                            double db = getDiscountPercent(b, dealProductMap);
                            return Double.compare(db, da);
                        }).toList();
                        break;
                }
            }

            model.addAttribute("results", results);
            model.addAttribute("matchingStores", matchingStores);
            model.addAttribute("resultCount", results.size());
            model.addAttribute("dealProductMap", dealProductMap);
        }
        return "buyer/search";
    }

    private double getDiscountPercent(Product p, Map<Long, Map<String, Object>> dealProductMap) {
        Map<String, Object> di = dealProductMap.get(p.getProductId());
        return di != null ? ((Number) di.get("discountPercent")).doubleValue() : 0;
    }

    // ============ STORE PROFILE ============

    @GetMapping("/buyer/store/{storeId}/profile")
    public String storeProfile(@PathVariable Long storeId, Model model) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) return "redirect:/";
        List<Product> products = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> p.getActive() && "APPROVED".equals(p.getApprovalStatus()))
            .toList();
        model.addAttribute("store", store);
        model.addAttribute("products", products);
        model.addAttribute("productCount", products.size());
        return "buyer/store-profile";
    }

    // ============ DEAL MAP ============

    @GetMapping("/buyer/deal-map")
    public String dealMap(Model model) {
        model.addAttribute("activeNav", "map");
        LocalDateTime now = LocalDateTime.now();
        // Get all ACTIVE deals, filter by time in Java (handles NULL gracefully)
        List<Deal> allActive = dealRepository.findByStatus("ACTIVE");
        List<Deal> activeDeals = new ArrayList<>();
        for (Deal d : allActive) {
            boolean startOk = d.getStartTime() == null || !d.getStartTime().isAfter(now);
            boolean endOk = d.getEndTime() == null || !d.getEndTime().isBefore(now);
            if (startOk && endOk) activeDeals.add(d);
        }

        // Group by store: one pin per store with all its deals
        java.util.Map<Long, Map<String, Object>> storeGroups = new java.util.LinkedHashMap<>();
        for (Deal deal : activeDeals) {
            Store store = deal.getStore();
            if (store == null || store.getLatitude() == null || store.getLongitude() == null) continue;

            List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
            if (dealProducts == null || dealProducts.isEmpty()) continue;

            Long sid = store.getStoreId();
            Map<String, Object> sg = storeGroups.get(sid);
            if (sg == null) {
                sg = new HashMap<>();
                sg.put("storeId", sid);
                sg.put("storeName", store.getStoreName());
                sg.put("storeLogo", store.getLogoUrl());
                sg.put("lat", store.getLatitude());
                sg.put("lng", store.getLongitude());
                sg.put("maxDiscount", 0.0);
                sg.put("products", new ArrayList<Map<String, Object>>());
                storeGroups.put(sid, sg);
            }

            double sgMaxDiscount = ((Number) sg.get("maxDiscount")).doubleValue();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sgProducts = (List<Map<String, Object>>) sg.get("products");

            for (DealProduct dp : dealProducts) {
                Product p = dp.getProduct();
                if (p == null || !p.isAvailable()) continue;

                double discount = "PERCENT".equals(deal.getDiscountType())
                    ? deal.getDiscountValue()
                    : dp.getOriginalPrice() > 0 ? ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100 : 0;
                if (discount > sgMaxDiscount) sgMaxDiscount = discount;

                Map<String, Object> item = new HashMap<>();
                item.put("productId", p.getProductId());
                item.put("name", p.getName());
                item.put("price", Math.round(dp.getSalePrice()));
                item.put("original", Math.round(dp.getOriginalPrice()));
                item.put("discount", Math.round(discount));
                item.put("img", p.getImageUrl() != null ? p.getImageUrl() : "/img/default-banner.svg");
                item.put("slots", p.getStockQuantity());
                item.put("dealType", deal.getDealType());
                item.put("dealName", deal.getDealName());
                long hoursLeft = java.time.Duration.between(now, deal.getEndTime()).toHours();
                item.put("urgency", hoursLeft < 1 ? "danger" : hoursLeft < 3 ? "warning" : "safe");
                item.put("timeLeft", hoursLeft < 1 ? "< 1h" : hoursLeft < 3 ? hoursLeft + "h" : "> 3h");
                sgProducts.add(item);
            }
            sg.put("maxDiscount", sgMaxDiscount);
        }

        List<Map<String, Object>> dealMarkers = new ArrayList<>(storeGroups.values());
        System.out.println("=== DEAL MAP: stores=" + dealMarkers.size());

        // Serialize to JSON for the template
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            model.addAttribute("dealsJson", om.writeValueAsString(dealMarkers));
        } catch (Exception e) {
            model.addAttribute("dealsJson", "[]");
        }
        model.addAttribute("dealMarkers", dealMarkers);
        model.addAttribute("dealCount", dealMarkers.size());
        model.addAttribute("activeDealsCount", activeDeals.size());

        return "buyer/deal-map";
    }

    // ============ SEARCH SUGGESTIONS ============

    @GetMapping("/api/search/suggest")
    @ResponseBody
    public Map<String, Object> searchSuggest(@RequestParam String q) {
        Map<String, Object> result = new HashMap<>();
        if (q == null || q.trim().length() < 2) {
            result.put("success", true);
            result.put("products", List.of());
            result.put("stores", List.of());
            return result;
        }
        String keyword = q.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        // Find products matching keyword (limit 4)
        List<Product> products = productRepository.searchAvailable(keyword, now,
            org.springframework.data.domain.PageRequest.of(0, 4)).getContent();

        List<Map<String, Object>> productList = new ArrayList<>();
        for (Product p : products) {
            if (!p.isAvailable()) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getProductId());
            item.put("name", p.getName());
            item.put("image", p.getImageUrl() != null ? p.getImageUrl() : "/img/default-banner.svg");
            item.put("storeName", p.getStore() != null ? p.getStore().getStoreName() : null);
            productList.add(item);
        }

        // Find stores matching keyword (limit 3)
        List<Store> stores = storeRepository.findByStoreNameContainingIgnoreCaseAndStatus(
            keyword, "ACTIVE", org.springframework.data.domain.PageRequest.of(0, 3)).getContent();

        List<Map<String, Object>> storeList = new ArrayList<>();
        for (Store st : stores) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", st.getStoreId());
            item.put("name", st.getStoreName());
            item.put("image", st.getLogoUrl() != null ? st.getLogoUrl() : "/img/default-banner.svg");
            item.put("address", (st.getCity() != null ? st.getCity() : "") +
                (st.getDistrict() != null ? ", " + st.getDistrict() : ""));
            item.put("tier", st.getPartnerTierLabel());
            storeList.add(item);
        }

        result.put("success", true);
        result.put("products", productList);
        result.put("stores", storeList);
        return result;
    }

    // ============ BUYER PROFILE APIs ============

    @PostMapping("/api/buyer/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        user.setFullName(fullName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (address != null) user.setAddress(address.trim());
        userRepository.save(user);
        return Map.of("success", true, "message", "Đã cập nhật thông tin");
    }

    @PostMapping("/api/buyer/profile/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            java.security.Principal principal,
            jakarta.servlet.http.HttpServletRequest request) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return Map.of("success", false, "message", "Mật khẩu hiện tại không đúng");
        }
        if (newPassword.length() < 8) {
            return Map.of("success", false, "message", "Mật khẩu mới phải có ít nhất 8 ký tự");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setWeakPassword(false);
        userRepository.save(user);
        // Force logout after password change
        try { request.getSession().invalidate(); } catch (Exception ignored) {}
        return Map.of("success", true, "message", "Đã đổi mật khẩu. Vui lòng đăng nhập lại.", "logout", true);
    }

        // ============ ADDRESS BOOK APIs ============

    @GetMapping("/api/buyer/addresses")
    @ResponseBody
    public Map<String, Object> getAddresses(java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        List<UserAddress> addresses = userAddressRepository.findByUserUserIdOrderByIsDefaultDesc(user.getUserId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserAddress a : addresses) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("name", a.getName());
            item.put("phone", a.getPhone());
            item.put("address", a.getAddress());
            item.put("city", a.getCity());
            item.put("district", a.getDistrict());
            item.put("ward", a.getWard());
            item.put("isDefault", a.getIsDefault());
            item.put("fullAddress", a.getFullAddress());
            list.add(item);
        }
        return Map.of("success", true, "addresses", list);
    }

    @PostMapping("/api/buyer/addresses/add")
    @ResponseBody
    public Map<String, Object> addAddress(@RequestParam String name, @RequestParam String phone,
            @RequestParam String address, @RequestParam(required = false) String city,
            @RequestParam(required = false) String district, @RequestParam(required = false) String ward,
            @RequestParam(defaultValue = "false") boolean isDefault, java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        UserAddress ua = new UserAddress();
        ua.setUser(user);
        ua.setName(name.trim());
        ua.setPhone(phone.trim());
        ua.setAddress(address.trim());
        ua.setCity(city != null ? city.trim() : null);
        ua.setDistrict(district != null ? district.trim() : null);
        ua.setWard(ward != null ? ward.trim() : null);
        if (isDefault) {
            userAddressRepository.clearDefault(user.getUserId());
        }
        ua.setIsDefault(isDefault);
        ua = userAddressRepository.save(ua);
        Map<String, Object> item = new HashMap<>();
        item.put("id", ua.getId());
        item.put("fullAddress", ua.getFullAddress());
        return Map.of("success", true, "message", "Đã thêm địa chỉ", "address", item);
    }

    @PostMapping("/api/buyer/addresses/update")
    @ResponseBody
    public Map<String, Object> updateAddress(@RequestParam Long id, @RequestParam String name,
            @RequestParam String phone, @RequestParam String address,
            @RequestParam(required = false) String city, @RequestParam(required = false) String district,
            @RequestParam(required = false) String ward,
            @RequestParam(defaultValue = "false") boolean isDefault, java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        UserAddress ua = userAddressRepository.findById(id).orElse(null);
        if (ua == null || !ua.getUser().getUserId().equals(user.getUserId())) {
            return Map.of("success", false, "message", "Địa chỉ không tồn tại");
        }
        ua.setName(name.trim());
        ua.setPhone(phone.trim());
        ua.setAddress(address.trim());
        if (city != null) ua.setCity(city.trim());
        if (district != null) ua.setDistrict(district.trim());
        if (ward != null) ua.setWard(ward.trim());
        if (isDefault) userAddressRepository.clearDefault(user.getUserId());
        ua.setIsDefault(isDefault);
        userAddressRepository.save(ua);
        return Map.of("success", true, "message", "Đã cập nhật địa chỉ");
    }

    @PostMapping("/api/buyer/addresses/delete")
    @ResponseBody
    public Map<String, Object> deleteAddress(@RequestParam Long id, java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        UserAddress ua = userAddressRepository.findById(id).orElse(null);
        if (ua == null || !ua.getUser().getUserId().equals(user.getUserId())) {
            return Map.of("success", false, "message", "Địa chỉ không tồn tại");
        }
        userAddressRepository.delete(ua);
        return Map.of("success", true, "message", "Đã xoá địa chỉ");
    }

    @PostMapping("/api/buyer/profile/avatar")
    @ResponseBody
    public Map<String, Object> updateAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        // Validate
        if (file.isEmpty()) return Map.of("success", false, "message", "Vui lòng chọn file");
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/webp") && !ct.equals("image/gif"))) {
            return Map.of("success", false, "message", "Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }
        if (file.getSize() > 3 * 1024 * 1024) {
            return Map.of("success", false, "message", "Ảnh quá lớn (tối đa 3MB)");
        }
        try {
            String url = saveUploadedFile(file);
            user.setAvatarUrl(url);
            userRepository.save(user);
            return Map.of("success", true, "avatarUrl", url, "message", "Đã cập nhật ảnh đại diện");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Lỗi upload: " + e.getMessage());
        }
    }

    // ============ NOTIFICATION APIs ============

    @GetMapping("/api/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications(java.security.Principal principal) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser(principal);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập");
            return result;
        }
        List<Notification> notifications = notificationService.getRecentNotifications(user, 20);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", n.getNotificationId());
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("type", n.getType());
            item.put("linkUrl", n.getLinkUrl());
            item.put("isRead", n.getIsRead());
            item.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            list.add(item);
        }
        result.put("success", true);
        result.put("notifications", list);
        result.put("unreadCount", notificationService.getUnreadCount(user));
        return result;
    }

    @PostMapping("/api/notifications/read")
    @ResponseBody
    public Map<String, Object> markNotificationRead(@RequestParam Long id, java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        notificationService.markAsRead(id);
        return Map.of("success", true, "unreadCount", notificationService.getUnreadCount(user));
    }

    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public Map<String, Object> markAllNotificationsRead(java.security.Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return Map.of("success", false, "message", "Vui lòng đăng nhập");
        notificationService.markAllAsRead(user);
        return Map.of("success", true, "unreadCount", 0L);
    }

    @GetMapping("/api/notifications/count")
    @ResponseBody
    public Map<String, Object> getNotificationCount(java.security.Principal principal) {
        User user = getCurrentUser(principal);
        long count = user != null ? notificationService.getUnreadCount(user) : 0;
        return Map.of("success", true, "count", count);
    }

    // ============ HELPER: GET CURRENT USER ============

    private User getCurrentUser(java.security.Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        String name = principal.getName();
        // Try username first, then email (principal name could be either)
        User user = userRepository.findByUsername(name).orElse(null);
        if (user == null) user = userRepository.findByEmail(name).orElse(null);
        return user;
    }

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    private String saveUploadedFile(org.springframework.web.multipart.MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;
        // MD5 hash for dedup
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        String hash = sb.toString();
        String originalFilename = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = hash + ext;
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        java.nio.file.Path targetPath = uploadPath.resolve(filename);
        if (!java.nio.file.Files.exists(targetPath)) {
            java.nio.file.Files.copy(file.getInputStream(), targetPath);
        }
        return "/uploads/" + filename;
    }
}
