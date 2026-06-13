package com.dealxanh.app.service;

import com.dealxanh.app.entity.Notification;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Thay thế ExpiryCountdownService.
 * KHÔNG tự động giảm giá. Thay vào đó:
 * 1. Kiểm tra sản phẩm sắp hết hạn
 * 2. Tính toán mức giảm giá đề xuất dựa trên tỉ lệ thời gian còn lại
 * 3. Gửi thông báo cho seller
 * 4. Seller xác nhận → cập nhật currentPrice
 * 5. SP tự hết hạn → deactive
 */
@Service
public class PriceSuggestionService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private NotificationService notificationService;

    // ===== THRESHOLDS =====

    /** Tỉ lệ thời gian còn lại để bắt đầu gợi ý (≤ 1/3 = 33%) */
    private static final double SUGGESTION_START_RATIO = 1.0 / 3.0;

    /** Gợi ý giảm giá theo tỉ lệ thời gian còn lại */
    private static final double RATIO_CRITICAL = 0.05;  // ≤ 5%  → 80%
    private static final double RATIO_URGENT   = 0.10;  // ≤ 10% → 60%
    private static final double RATIO_HIGH     = 0.20;  // ≤ 20% → 40%
    private static final double RATIO_MEDIUM   = 0.33;  // ≤ 33% → 25%

    // ===== SCHEDULED: Chạy mỗi giờ =====

    @Scheduled(fixedRate = 3_600_000) // 1 hour
    @Transactional
    public void checkAndSuggest() {
        System.out.println("=== PRICE SUGGESTION CHECK START ===");
        List<Product> products = productRepository.findByDeletedFalseAndActiveTrue();
        LocalDateTime now = LocalDateTime.now();
        int suggested = 0, expired = 0;

        for (Product p : products) {
            if (p.getExpiryDate() == null) continue;
            if (p.getOriginalPrice() == null || p.getOriginalPrice() <= 0) continue;

            long remainingHours = p.getRemainingHours();

            // Hết hạn → deactive
            if (remainingHours <= 0) {
                p.setCurrentPrice(0.0);
                p.setActive(false);
                p.setUpdatedAt(now);
                productRepository.save(p);
                expired++;
                continue;
            }

            // Tính tỉ lệ và đề xuất
            double ratio = p.getRemainingRatio();
            if (ratio < 0 || ratio > SUGGESTION_START_RATIO) continue; // Chưa đến ngưỡng

            // Nếu đã có đề xuất và chưa quá 24h → bỏ qua (tránh spam)
            if (p.getSuggestedAt() != null
                    && ChronoUnit.HOURS.between(p.getSuggestedAt(), now) < 24) {
                continue;
            }

            // Tính mức giảm giá đề xuất
            double suggestedPercent = getSuggestedDiscountPercent(ratio);
            if (suggestedPercent <= 0) continue;

            double suggestedPrice = Math.round(p.getOriginalPrice() * (1 - suggestedPercent / 100));
            if (suggestedPrice <= 0) suggestedPrice = Math.round(p.getOriginalPrice() * 0.2);

            // Lưu đề xuất vào product
            p.setSuggestedDiscount(suggestedPercent);
            p.setSuggestedPrice(suggestedPrice);
            p.setSuggestedAt(now);
            p.setUpdatedAt(now);
            productRepository.save(p);

            // Gửi thông báo cho chủ store
            Store store = p.getStore();
            if (store != null) {
                // Lấy owner từ store (có thể cần fetch thêm nếu lazy)
                User owner = null;
                try {
                    Store fullStore = storeRepository.findByIdWithOwner(store.getStoreId()).orElse(null);
                    if (fullStore != null) owner = fullStore.getOwner();
                } catch (Exception ignored) {}
                if (owner == null) owner = store.getOwner();
                if (owner != null) {
                    String title = "Đề xuất giảm giá " + (int) suggestedPercent + "% — \"" + p.getName() + "\"";
                    String msg = "Sản phẩm \"" + p.getName() + "\" còn " + remainingHours + " giờ trước HSD ("
                            + String.format("%.0f", ratio * 100) + "% thời gian). "
                            + "Hệ thống đề xuất giảm " + (int) suggestedPercent + "% còn "
                            + String.format("%,.0f", suggestedPrice) + "đ.";
                    notificationService.createNotification(owner, title, msg, "PRICE_SUGGESTION",
                            "/seller/products?suggested=1");
                    suggested++;
                }
            }
        }

        System.out.println("=== PRICE SUGGESTION END: " + suggested + " suggested, " + expired + " expired ===");
    }

    /** Tính % giảm giá đề xuất dựa trên tỉ lệ thời gian còn lại */
    public double getSuggestedDiscountPercent(double remainingRatio) {
        if (remainingRatio <= RATIO_CRITICAL) return 80;  // ≤ 5%
        if (remainingRatio <= RATIO_URGENT)   return 60;  // ≤ 10%
        if (remainingRatio <= RATIO_HIGH)     return 40;  // ≤ 20%
        if (remainingRatio <= RATIO_MEDIUM)   return 25;  // ≤ 33%
        return 15; // Default mild suggestion
    }

    // ===== SELLER CONFIRM =====

    @Transactional
    public String confirmSuggestedPrice(Long productId, User seller) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p == null) return "Sản phẩm không tồn tại";
        if (p.getSuggestedPrice() == null) return "Không có đề xuất giảm giá nào";

        // Kiểm tra quyền
        if (p.getStore() == null || seller.getWorkStore() == null
                || !p.getStore().getStoreId().equals(seller.getWorkStore().getStoreId())) {
            return "Bạn không có quyền";
        }

        double newPrice = p.getSuggestedPrice();
        p.setCurrentPrice(newPrice);
        p.setUpdatedAt(LocalDateTime.now());
        // Xóa đề xuất sau khi đã áp dụng
        p.setSuggestedDiscount(null);
        p.setSuggestedPrice(null);
        p.setSuggestedAt(null);
        productRepository.save(p);

        System.out.println("Seller confirmed suggested price for product #" + productId
                + ": " + newPrice + "đ");
        return null; // OK
    }

    @Transactional
    public String dismissSuggestion(Long productId, User seller) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p == null) return "Sản phẩm không tồn tại";
        if (p.getStore() == null || seller.getWorkStore() == null
                || !p.getStore().getStoreId().equals(seller.getWorkStore().getStoreId())) {
            return "Bạn không có quyền";
        }
        p.setSuggestedDiscount(null);
        p.setSuggestedPrice(null);
        p.setSuggestedAt(null);
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
        return null;
    }
}
