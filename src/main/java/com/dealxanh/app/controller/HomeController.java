package com.dealxanh.app.controller;

import com.dealxanh.app.entity.Category;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Order;
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
                if (dp.getProduct() == null || !dp.getProduct().getActive()) continue;
                Product prod = dp.getProduct();

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

    @SuppressWarnings("unchecked")
    private int getCartItemCount(jakarta.servlet.http.HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) return 0;
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) return 0;
        return cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum();
    }

    @GetMapping("/category/{categoryId}")
    public String categoryPage(@PathVariable Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String priceRange,
            Model model) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return "redirect:/";

        // Get approved active products in this category
        List<Product> products = productRepository.findAll().stream()
            .filter(p -> p.getActive() && !p.getDeleted()
                && "APPROVED".equals(p.getApprovalStatus())
                && p.getCategory() != null
                && p.getCategory().getCategoryId().equals(categoryId))
            .toList();

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
                        Map<String, Object> info = new HashMap<>();
                        info.put("salePrice", Math.round(dp.getSalePrice()));
                        info.put("originalPrice", Math.round(dp.getOriginalPrice()));
                        info.put("discountPercent", Math.round(discountPercent));
                        info.put("dealType", deal.getDealType());
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
    public java.util.Map<String, Object> getCart(jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) cart = java.util.List.of();
        return java.util.Map.of("items", cart, "count", cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum());
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public java.util.Map<String, Object> addToCart(@RequestParam Long dealId, @RequestParam Long productId,
            @RequestParam String productName, @RequestParam double salePrice, @RequestParam double originalPrice,
            @RequestParam String storeName, @RequestParam(required = false) String productImage,
            @RequestParam(required = false) Long storeId,
            jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) cart = new java.util.ArrayList<>();

        // Check if already in cart
        for (java.util.Map<String, Object> item : cart) {
            if (item.get("productId").equals(productId)) {
                int qty = ((Number) item.getOrDefault("quantity", 1)).intValue() + 1;
                item.put("quantity", qty);
                session.setAttribute("cart", cart);
                return java.util.Map.of("success", true, "message", "Đã tăng số lượng", "count", cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum());
            }
        }

        java.util.Map<String, Object> item = new java.util.HashMap<>();
        item.put("dealId", dealId);
        item.put("productId", productId);
        item.put("productName", productName);
        item.put("salePrice", salePrice);
        item.put("originalPrice", originalPrice);
        item.put("storeName", storeName);
        item.put("productImage", productImage != null ? productImage : "");
        item.put("storeId", storeId);
        item.put("quantity", 1);
        cart.add(item);
        session.setAttribute("cart", cart);
        return java.util.Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "count", cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum());
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public java.util.Map<String, Object> removeFromCart(@RequestParam Long productId, jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) return java.util.Map.of("success", false, "message", "Giỏ hàng trống");
        cart.removeIf(item -> item.get("productId").equals(productId));
        session.setAttribute("cart", cart);
        return java.util.Map.of("success", true, "count", cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum());
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public java.util.Map<String, Object> updateCartItem(@RequestParam Long productId, @RequestParam int quantity,
            jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) return java.util.Map.of("success", false, "message", "Giỏ hàng trống");
        for (java.util.Map<String, Object> item : cart) {
            if (item.get("productId").equals(productId)) {
                if (quantity <= 0) cart.remove(item);
                else item.put("quantity", quantity);
                session.setAttribute("cart", cart);
                return java.util.Map.of("success", true, "count", cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum());
            }
        }
        return java.util.Map.of("success", false, "message", "Không tìm thấy sản phẩm");
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public java.util.Map<String, Object> cartCount(jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> cart = (java.util.List<java.util.Map<String, Object>>) session.getAttribute("cart");
        int count = cart == null ? 0 : cart.stream().mapToInt(i -> ((Number) i.getOrDefault("quantity", 1)).intValue()).sum();
        return java.util.Map.of("count", count);
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
        if ("COMPLETED".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Đơn hàng đã được nhận trước đó");
        if (!"READY_FOR_PICKUP".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Đơn hàng chưa sẵn sàng để nhận");
        order.setStatus("COMPLETED");
        order.setActualPickupTime(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return java.util.Map.of("success", true, "message", "✅ Xác nhận nhận hàng thành công! Cảm ơn bạn đã mua sắm tại DealXanh.");
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

        // Get cart from session
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cart = (List<Map<String, Object>>) session.getAttribute("cart");
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

        model.addAttribute("storeGroups", storeGroups);
        model.addAttribute("storeInfo", storeInfo);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("total", total);
        model.addAttribute("cart", cart);

        return "buyer/order-summary";
    }

    @PostMapping("/buyer/checkout/confirm")
    public String confirmCheckout(java.security.Principal principal,
            jakarta.servlet.http.HttpSession session) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cart = (List<Map<String, Object>>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/buyer/cart";

        // Create orders grouped by store
        Map<Long, Order> storeOrders = new java.util.LinkedHashMap<>();
        for (Map<String, Object> item : cart) {
            Long storeId = item.get("storeId") != null ? ((Number) item.get("storeId")).longValue() : null;
            if (storeId == null) continue;
            Order order = storeOrders.computeIfAbsent(storeId, k -> {
                Order o = new Order();
                o.setUser(user);
                o.setStore(storeRepository.findById(k).orElse(null));
                o.setStatus("PENDING");
                o.setCreatedAt(LocalDateTime.now());
                o.setPickupQrCode("DX-" + k + "-" + generateRandomCode(6));
                return o;
            });
        }

        // Save orders and clear cart
        for (Order o : storeOrders.values()) {
            orderRepository.save(o);
        }
        session.removeAttribute("cart");

        return "redirect:/buyer/order-complete";
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
    public String orderComplete(java.security.Principal principal, Model model) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
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
        // Redirect to orders list for now (detail page pending)
        return "redirect:/buyer/orders";
    }

    // ============ SEARCH ============

    @GetMapping("/buyer/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("searchQuery", q);
        if (q != null && !q.trim().isEmpty()) {
            String s = q.toLowerCase().trim();
            List<Product> results = productRepository.findAll().stream()
                .filter(p -> p.getActive() && !p.getDeleted()
                    && "APPROVED".equals(p.getApprovalStatus())
                    && (p.getName().toLowerCase().contains(s)))
                .limit(50)
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
        // Get active stores for map display
        List<Store> activeStores = storeRepository.findByStatus("ACTIVE");
        model.addAttribute("stores", activeStores != null ? activeStores : List.of());
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
