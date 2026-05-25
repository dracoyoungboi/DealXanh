package com.dealxanh.app.service;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.*;
import com.dealxanh.app.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DealProductRepository dealProductRepository;
    @Autowired private ResourceLockManager lockManager;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public void migrateSessionCart(HttpSession session, User user) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sessionCart = (List<Map<String, Object>>) session.getAttribute("cart");
        if (sessionCart == null || sessionCart.isEmpty()) return;

        Cart cart = getOrCreateCart(user);
        boolean migrated = false;

        for (Map<String, Object> sessionItem : sessionCart) {
            Long productId = toLong(sessionItem.get("productId"));
            if (productId == null) continue;

            // Check if already in DB cart
            boolean exists = cart.getCartItems().stream()
                    .anyMatch(ci -> ci.getProduct() != null && productId.equals(ci.getProduct().getProductId()));
            if (exists) continue;

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.isAvailable()) continue;
            if (!"APPROVED".equals(product.getApprovalStatus())) continue;

            int qty = Math.min(
                    ((Number) sessionItem.getOrDefault("quantity", 1)).intValue(),
                    product.getStockQuantity()
            );
            if (qty <= 0) continue;

            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(product);
            ci.setQuantity(qty);
            ci.setUnitPrice(((Number) sessionItem.getOrDefault("salePrice", 0)).doubleValue());
            cart.getCartItems().add(ci);
            migrated = true;
        }

        if (migrated) {
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
        session.removeAttribute("cart");
    }

    @Transactional
    public Map<String, Object> addItem(User user, Long dealId, Long productId,
            String productName, double salePrice, double originalPrice,
            String storeName, String productImage, Long storeId,
            String dealType, String dealName) {
        ReentrantLock lock = lockManager.acquireLock("CART", user.getUserId());
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.isAvailable()) {
                return Map.of("success", false, "message", "Sản phẩm không khả dụng");
            }
            if (!"APPROVED".equals(product.getApprovalStatus())) {
                return Map.of("success", false, "message", "Sản phẩm không khả dụng");
            }

            int maxStock = product.getStockQuantity();
            if (maxStock <= 0) {
                return Map.of("success", false, "message", "Sản phẩm đã hết hàng");
            }

            Cart cart = getOrCreateCart(user);

            for (CartItem ci : cart.getCartItems()) {
                if (ci.getProduct() != null && ci.getProduct().getProductId().equals(productId)) {
                    if (ci.getQuantity() >= maxStock) {
                        return Map.of("success", false, "message",
                                "Chỉ còn " + maxStock + " sản phẩm trong kho. Bạn đã có " + ci.getQuantity() + " trong giỏ.");
                    }
                    ci.setQuantity(ci.getQuantity() + 1);
                    ci.setUnitPrice(salePrice);
                    cart.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(cart);
                    return Map.of("success", true, "message", "Đã tăng số lượng",
                            "count", getItemCount(cart));
                }
            }

            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(product);
            ci.setQuantity(1);
            ci.setUnitPrice(salePrice);
            cart.getCartItems().add(ci);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

            return Map.of("success", true, "message", "Đã thêm vào giỏ hàng",
                    "count", getItemCount(cart));
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Map<String, Object> removeItem(User user, Long productId) {
        ReentrantLock lock = lockManager.acquireLock("CART", user.getUserId());
        try {
            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart == null) return Map.of("success", false, "message", "Giỏ hàng trống");

            cart.getCartItems().removeIf(ci -> ci.getProduct() != null
                    && ci.getProduct().getProductId().equals(productId));
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

            return Map.of("success", true, "count", getItemCount(cart));
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Map<String, Object> updateItemQuantity(User user, Long productId, int quantity) {
        ReentrantLock lock = lockManager.acquireLock("CART", user.getUserId());
        try {
            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart == null) return Map.of("success", false, "message", "Giỏ hàng trống");

            Product product = productRepository.findById(productId).orElse(null);
            int maxStock = product != null ? product.getStockQuantity() : 0;

            for (CartItem ci : cart.getCartItems()) {
                if (ci.getProduct() != null && ci.getProduct().getProductId().equals(productId)) {
                    if (quantity <= 0) {
                        cart.getCartItems().remove(ci);
                    } else if (quantity > maxStock) {
                        return Map.of("success", false, "message",
                                "Chỉ còn " + maxStock + " sản phẩm trong kho. Không thể thêm " + quantity + " vào giỏ.");
                    } else {
                        ci.setQuantity(quantity);
                    }
                    cart.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(cart);
                    return Map.of("success", true, "count", getItemCount(cart));
                }
            }
            return Map.of("success", false, "message", "Không tìm thấy sản phẩm trong giỏ");
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Object> getCartData(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        List<Map<String, Object>> items = new ArrayList<>();
        int count = 0;

        if (cart != null && cart.getCartItems() != null) {
            LocalDateTime now = LocalDateTime.now();
            for (CartItem ci : cart.getCartItems()) {
                Product product = ci.getProduct();
                Map<String, Object> item = new HashMap<>();

                if (product == null) continue;

                item.put("productId", product.getProductId());
                item.put("productName", product.getName());
                item.put("productImage", product.getImageUrl() != null ? product.getImageUrl() : "");
                item.put("originalPrice", product.getOriginalPrice() != null ? product.getOriginalPrice() : 0);

                // Store info
                Store store = product.getStore();
                item.put("storeName", store != null ? store.getStoreName() : "");
                item.put("storeId", store != null ? store.getStoreId() : null);

                // Check availability
                boolean available = product.isAvailable()
                        && "APPROVED".equals(product.getApprovalStatus());
                int maxStock = product.getStockQuantity();

                if (!available) {
                    item.put("quantity", 0);
                    item.put("maxStock", 0);
                    item.put("unavailable", true);
                    item.put("unavailableReason", "Sản phẩm đã hết hàng hoặc hết hạn");
                    item.put("salePrice", ci.getUnitPrice() != null ? ci.getUnitPrice() : 0);
                    item.put("dealId", 0);
                    item.put("dealType", "");
                    item.put("dealName", "");
                } else {
                    int qty = Math.min(ci.getQuantity(), maxStock);
                    item.put("quantity", qty);
                    item.put("maxStock", maxStock);
                    item.put("unavailable", false);

                    // Find active deal for this product
                    List<DealProduct> dealProducts = dealProductRepository.findByProduct(product);
                    DealProduct activeDp = null;
                    if (dealProducts != null) {
                        for (DealProduct dp : dealProducts) {
                            Deal deal = dp.getDeal();
                            if (deal != null && "ACTIVE".equals(deal.getStatus())
                                    && deal.getStartTime() != null && deal.getStartTime().isBefore(now)
                                    && deal.getEndTime() != null && deal.getEndTime().isAfter(now)) {
                                activeDp = dp;
                                break;
                            }
                        }
                    }

                    if (activeDp != null) {
                        item.put("salePrice", activeDp.getSalePrice() != null ? activeDp.getSalePrice() : product.getOriginalPrice());
                        item.put("dealId", activeDp.getDeal().getDealId());
                        item.put("dealType", activeDp.getDeal().getDealType());
                        item.put("dealName", activeDp.getDeal().getDealName());
                    } else {
                        item.put("salePrice", product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getOriginalPrice());
                        item.put("dealId", 0);
                        item.put("dealType", "");
                        item.put("dealName", "");
                    }
                }

                count += ((Number) item.get("quantity")).intValue();
                items.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("count", count);
        return result;
    }

    public int getItemCount(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        return getItemCount(cart);
    }

    private int getItemCount(Cart cart) {
        if (cart == null || cart.getCartItems() == null) return 0;
        return cart.getCartItems().stream()
                .mapToInt(ci -> ci.getQuantity() != null ? ci.getQuantity() : 0)
                .sum();
    }

    @Transactional
    public void clearCart(User user) {
        ReentrantLock lock = lockManager.acquireLock("CART", user.getUserId());
        try {
            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart != null) {
                cart.getCartItems().clear();
                cartRepository.save(cart);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Map<String, Object>> getCartItemsForCheckout(User user) {
        Map<String, Object> data = getCartData(user);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        // Filter out unavailable items
        return items.stream()
                .filter(i -> !Boolean.TRUE.equals(i.get("unavailable")))
                .toList();
    }

    private Long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
