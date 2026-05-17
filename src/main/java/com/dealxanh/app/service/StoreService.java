package com.dealxanh.app.service;

import com.dealxanh.app.entity.Store;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Scheduled job: runs at 2:00 AM on the 1st of every month.
     * Upgrades partner tier based on last month's revenue and order count.
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void upgradePartnerTiers() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        List<Store> allStores = storeRepository.findByStatus("ACTIVE");

        for (Store store : allStores) {
            Double revenue = orderRepository.sumRevenueByPeriod(monthStart, monthEnd);
            Long orderCount = orderRepository.countOrdersByPeriod(monthStart, monthEnd);
            double rating = store.getAverageRating() != null ? store.getAverageRating() : 0;
            long totalReviews = store.getTotalReviews() != null ? store.getTotalReviews() : 0;

            double rev = revenue != null ? revenue : 0;
            long orders = orderCount != null ? orderCount : 0;

            String newTier;
            if (rev > 500_000_000 && orders > 2000 && rating >= 4.5) {
                newTier = "DIAMOND";
            } else if (rev > 200_000_000 && orders > 500 && rating >= 4.3) {
                newTier = "GOLD";
            } else if (rev > 50_000_000 && orders > 100 && rating >= 4.0) {
                newTier = "SILVER";
            } else {
                newTier = "BRONZE";
            }

            String currentTier = store.getPartnerTier();
            if (!newTier.equals(currentTier)) {
                store.setPartnerTier(newTier);
                store.setUpdatedAt(now);
                storeRepository.save(store);
                System.out.println("Partner tier upgraded: " + store.getStoreName()
                    + " " + currentTier + " → " + newTier
                    + " (revenue: " + String.format("%,.0f", rev)
                    + ", orders: " + orders + ")");
            }
        }
    }

    /** Manual trigger for testing */
    public void upgradePartnerTiersNow() {
        upgradePartnerTiers();
    }
}
