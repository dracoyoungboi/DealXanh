package com.dealxanh.app.service;

import com.dealxanh.app.entity.Product;
import com.dealxanh.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
public class ExpiryCountdownService {

    @Autowired
    private ProductRepository productRepository;

    private static final int COUNTDOWN_START_DAYS = 7;

    /**
     * Chạy mỗi ngày lúc 1:00 AM.
     * Giảm currentPrice dần khi sản phẩm tiến gần đến ngày hết hạn.
     * currentPrice = originalPrice * daysRemaining / COUNTDOWN_START_DAYS
     * Khi đến ngày hết hạn: currentPrice = 0, active = false.
     *
     * Countdown KHÔNG ảnh hưởng đến originalPrice hay dealPrice,
     * do đó không mâu thuẫn với quản lý deal của admin/seller.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processExpiryCountdown() {
        System.out.println("=== EXPIRY COUNTDOWN START ===");
        int updatedCount = 0;
        int expiredCount = 0;

        List<Product> activeProducts = productRepository.findByDeletedFalseAndActiveTrue();
        LocalDateTime now = LocalDateTime.now();

        for (Product product : activeProducts) {
            if (product.getExpiryDate() == null) continue;
            if (product.getOriginalPrice() == null || product.getOriginalPrice() <= 0) continue;

            long daysUntilExpiry = ChronoUnit.DAYS.between(now.toLocalDate(), product.getExpiryDate().toLocalDate());

            if (daysUntilExpiry <= 0) {
                // Đã hết hạn: set giá về 0, vô hiệu hóa sản phẩm
                product.setCurrentPrice(0.0);
                product.setActive(false);
                product.setUpdatedAt(now);
                productRepository.save(product);
                expiredCount++;
                System.out.println("EXPIRED: " + product.getName() + " (ID=" + product.getProductId() + ") - set price=0, inactive");
            } else if (daysUntilExpiry <= COUNTDOWN_START_DAYS) {
                // Đang trong vùng countdown 7 ngày
                double ratio = (double) daysUntilExpiry / COUNTDOWN_START_DAYS;
                long newPrice = Math.round(product.getOriginalPrice() * ratio);
                Double oldCurrentPrice = product.getCurrentPrice();
                product.setCurrentPrice((double) newPrice);
                product.setUpdatedAt(now);
                productRepository.save(product);
                updatedCount++;
                System.out.println("COUNTDOWN: " + product.getName() + " (ID=" + product.getProductId()
                    + ") - " + daysUntilExpiry + " days left, price: "
                    + (oldCurrentPrice != null ? Math.round(oldCurrentPrice) : product.getOriginalPrice())
                    + " -> " + newPrice);
            } else {
                // Còn hơn 7 ngày: reset currentPrice = originalPrice (nếu đã bị thay đổi trước đó)
                if (product.getCurrentPrice() != null && !product.getCurrentPrice().equals(product.getOriginalPrice())) {
                    product.setCurrentPrice(product.getOriginalPrice());
                    product.setUpdatedAt(now);
                    productRepository.save(product);
                }
            }
        }

        System.out.println("=== EXPIRY COUNTDOWN END: " + updatedCount + " updated, " + expiredCount + " expired ===");
    }
}
