package com.dealxanh.app.controller;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.Category;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.OrderItem;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.CategoryRepository;
import com.dealxanh.app.repository.DealRepository;
import com.dealxanh.app.repository.DealProductRepository;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
    private CategoryRepository categoryRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ResourceLockManager lockManager;

    @GetMapping("/")
    public String home(Model model, jakarta.servlet.http.HttpServletRequest request) {
        int cartCount = getCartItemCount(request);
        model.addAttribute("activeNav", "home");
        model.addAttribute("cartItemCount", cartCount);
        model.addAttribute("notifCount", 0);

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

        model.addAttribute("store", store);
        model.addAttribute("activeDeals", activeDeals);
        model.addAttribute("products", products);
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
            jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
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
            jakarta.servlet.http.HttpSession session) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        // Get cart from DB (migrate session cart first if needed)
        cartService.migrateSessionCart(session, user);
        List<Map<String, Object>> cart = cartService.getCartItemsForCheckout(user);
        if (cart == null || cart.isEmpty()) return "redirect:/buyer/cart";

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
                storeInfo.put(storeId, si);
            }
        }

        // Calculate totals
        for (Map<String, Object> item : cart) {
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            long salePrice = ((Number) item.getOrDefault("salePrice", item.getOrDefault("price", 0))).longValue();
            long originalPrice = ((Number) item.getOrDefault("originalPrice", salePrice)).longValue();
            subtotal += salePrice * qty;
            if (originalPrice > salePrice) {
                totalDiscount += (originalPrice - salePrice) * qty;
            }
        }
        long total = subtotal;

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
        model.addAttribute("cart", cart);

        return "buyer/order-summary";
    }

    @PostMapping("/buyer/checkout/confirm")
    public String confirmCheckout(java.security.Principal principal,
            @RequestParam(required = false, defaultValue = "BANK_TRANSFER") String paymentMethod,
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

                double totalAmount = 0;
                double discountAmount = 0;
                List<OrderItem> orderItems = new ArrayList<>();

                for (Map<String, Object> item : items) {
                    int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
                    double salePrice = ((Number) item.getOrDefault("salePrice", 0)).doubleValue();
                    double originalPrice = ((Number) item.getOrDefault("originalPrice", salePrice)).doubleValue();
                    Long productId = item.get("productId") != null ? ((Number) item.get("productId")).longValue() : null;

                    totalAmount += originalPrice * qty;
                    if (originalPrice > salePrice) {
                        discountAmount += (originalPrice - salePrice) * qty;
                    }

                    if (productId != null) {
                        Product product = productRepository.findById(productId).orElse(null);
                        if (product != null) {
                            OrderItem oi = new OrderItem();
                            oi.setOrder(order);
                            oi.setProduct(product);
                            oi.setQuantity(qty);
                            oi.setUnitPrice(salePrice);
                            orderItems.add(oi);
                        }
                    }
                }

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
    public String search(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("searchQuery", q);
        if (q != null && !q.trim().isEmpty()) {
            String s = q.toLowerCase().trim();

            // Search products by name
            List<Product> results = productRepository.findAll().stream()
                .filter(p -> p.getActive() && !p.getDeleted()
                    && "APPROVED".equals(p.getApprovalStatus())
                    && (p.getName().toLowerCase().contains(s)))
                .limit(50)
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
            model.addAttribute("results", results);
            model.addAttribute("matchingStores", matchingStores);
            model.addAttribute("resultCount", results.size());
            model.addAttribute("dealProductMap", dealProductMap);
        }
        return "buyer/search";
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
        LocalDateTime now = LocalDateTime.now();
        List<Deal> activeDeals = dealRepository.findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc(
            "ACTIVE", now, now);

        List<Map<String, Object>> dealMarkers = new ArrayList<>();
        for (Deal deal : activeDeals) {
            Store store = deal.getStore();
            if (store == null || store.getLatitude() == null || store.getLongitude() == null) continue;

            List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
            if (dealProducts == null || dealProducts.isEmpty()) continue;

            for (DealProduct dp : dealProducts) {
                Product p = dp.getProduct();
                if (p == null || !p.isAvailable()) continue;

                Map<String, Object> marker = new HashMap<>();
                marker.put("id", p.getProductId());
                marker.put("name", p.getName());
                marker.put("store", store.getStoreName());
                marker.put("storeId", store.getStoreId());
                marker.put("lat", store.getLatitude());
                marker.put("lng", store.getLongitude());
                marker.put("price", Math.round(dp.getSalePrice()));
                marker.put("original", Math.round(dp.getOriginalPrice()));
                double discount = "PERCENT".equals(deal.getDiscountType())
                    ? deal.getDiscountValue()
                    : dp.getOriginalPrice() > 0 ? ((dp.getOriginalPrice() - dp.getSalePrice()) / dp.getOriginalPrice()) * 100 : 0;
                marker.put("discount", Math.round(discount));
                marker.put("img", p.getImageUrl() != null ? p.getImageUrl() : "/img/default-banner.svg");
                marker.put("slots", p.getStockQuantity());
                marker.put("dealType", deal.getDealType());
                marker.put("dealName", deal.getDealName());

                // Urgency based on time remaining
                long hoursLeft = java.time.Duration.between(now, deal.getEndTime()).toHours();
                String urgency = hoursLeft < 1 ? "danger" : hoursLeft < 3 ? "warning" : "safe";
                marker.put("urgency", urgency);
                marker.put("timeLeft", hoursLeft < 1 ? "< 1h"
                    : hoursLeft < 3 ? hoursLeft + "h" : "> 3h");

                dealMarkers.add(marker);
            }
        }

        // Serialize to JSON for the template
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            model.addAttribute("dealsJson", om.writeValueAsString(dealMarkers));
        } catch (Exception e) {
            model.addAttribute("dealsJson", "[]");
        }
        model.addAttribute("dealCount", dealMarkers.size());

        return "buyer/deal-map";
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
}
