package com.dealxanh.app.controller.admin;

import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class AdminDashboardApiController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date) {

        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate;

        // Determine date range based on parameter
        if (date != null && !date.isEmpty()) {
            // Handle specific date request
            try {
                String[] parts = date.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                startDate = LocalDateTime.of(year, month, day, 0, 0);
                endDate = startDate.plusDays(1).minusSeconds(1);

                Double specificDateGMV = orderRepository.sumRevenueByPeriod(startDate, endDate);
                Long specificDateOrders = orderRepository.countOrdersByPeriod(startDate, endDate);
                Double commission = (specificDateGMV != null ? specificDateGMV : 0.0) * 0.10;

                result.put("todayGMV", specificDateGMV != null ? specificDateGMV : 0.0);
                result.put("todayOrders", specificDateOrders != null ? specificDateOrders : 0L);
                result.put("todayCommission", commission);

                // Get deltas compared to previous day
                LocalDateTime prevDayStart = startDate.minusDays(1);
                LocalDateTime prevDayEnd = startDate.minusSeconds(1);
                addDeltasToResult(result, startDate, endDate, prevDayStart, prevDayEnd);

                // Get 7-day GMV data for chart
                result.put("weeklyGMV", getWeeklyGMV(now));
                result.put("weeklyOrders", getWeeklyOrders(now));

                return result;
            } catch (Exception e) {
                // If date parsing fails, fall back to default behavior
                period = "today";
            }
        }

        // Handle period requests
        if (period == null || period.isEmpty()) {
            period = "today";
        }

        switch (period) {
            case "yesterday":
                startDate = now.minusDays(1).toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(1).minusSeconds(1);
                break;
            case "week":
                endDate = now.toLocalDate().atStartOfDay().minusSeconds(1);
                startDate = endDate.minusDays(6).toLocalDate().atStartOfDay();
                break;
            case "7d":
            case "30d":
            case "3m":
                // These are for chart data, still return today's stats
                startDate = now.toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(1).minusSeconds(1);
                break;
            default: // today
                startDate = now.toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(1).minusSeconds(1);
                break;
        }

        Double periodGMV = orderRepository.sumRevenueByPeriod(startDate, endDate);
        Long periodOrders = orderRepository.countOrdersByPeriod(startDate, endDate);
        Double commission = (periodGMV != null ? periodGMV : 0.0) * 0.10;

        result.put("todayGMV", periodGMV != null ? periodGMV : 0.0);
        result.put("todayOrders", periodOrders != null ? periodOrders : 0L);
        result.put("todayCommission", commission);

        // Always calculate deltas (compare with previous day by default)
        LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1).minusSeconds(1);

        // For "yesterday" period, compare with day before yesterday
        if ("yesterday".equals(period)) {
            LocalDateTime dayBeforeYesterday = yesterdayStart.minusDays(1);
            addDeltasToResult(result, startDate, endDate, dayBeforeYesterday, yesterdayEnd);
        } else if ("week".equals(period)) {
            // For 7-day period, compare with previous 7 days
            LocalDateTime weekAgoStart = yesterdayStart.minusDays(6);
            LocalDateTime weekAgoEnd = yesterdayStart.minusSeconds(1);
            addDeltasToResult(result, startDate, endDate, weekAgoStart, weekAgoEnd);
        } else {
            // Default: compare today with yesterday
            addDeltasToResult(result, startDate, endDate, yesterdayStart, yesterdayEnd);
        }

        // Get 7-day GMV data for chart
        result.put("weeklyGMV", getWeeklyGMV(now));
        result.put("weeklyOrders", getWeeklyOrders(now));

        // Overall stats
        result.put("totalOrders", orderRepository.count());
        result.put("activeStores", storeRepository.countByStatus("ACTIVE"));
        result.put("pendingStores", storeRepository.countByStatus("PENDING"));
        result.put("totalBuyers", userRepository.countByRoleName("ROLE_USER"));

        long totalSellers = 0L;
        totalSellers += userRepository.countByRoleName("ROLE_STORE_OWNER");
        totalSellers += userRepository.countByRoleName("ROLE_STORE_STAFF");
        result.put("totalSellers", totalSellers);

        return result;
    }

    private void addDeltasToResult(Map<String, Object> result, LocalDateTime currentStart, LocalDateTime currentEnd,
                                       LocalDateTime prevStart, LocalDateTime prevEnd) {
        // Get current period stats
        Double currentGMV = orderRepository.sumRevenueByPeriod(currentStart, currentEnd);
        Long currentOrders = orderRepository.countOrdersByPeriod(currentStart, currentEnd);

        // Get previous period stats
        Double prevGMV = orderRepository.sumRevenueByPeriod(prevStart, prevEnd);
        Long prevOrders = orderRepository.countOrdersByPeriod(prevStart, prevEnd);

        System.out.println("=== KPI DELTA DEBUG ===");
        System.out.println("Current period: " + currentStart + " to " + currentEnd);
        System.out.println("Previous period: " + prevStart + " to " + prevEnd);
        System.out.println("Current GMV: " + currentGMV);
        System.out.println("Previous GMV: " + prevGMV);
        System.out.println("Current Orders: " + currentOrders);
        System.out.println("Previous Orders: " + prevOrders);

        // Calculate GMV delta
        if (prevGMV != null && prevGMV > 0) {
            double gmvDelta = ((currentGMV != null ? currentGMV : 0.0) - prevGMV) / prevGMV * 100;
            result.put("gmvDelta", gmvDelta);
            result.put("gmvDeltaDirection", gmvDelta >= 0 ? "up" : "down");
            System.out.println("GMV Delta: " + gmvDelta + "% (" + (gmvDelta >= 0 ? "up" : "down") + ")");
        } else {
            // If previous period had no GMV, calculate delta based on current value
            double gmvDelta = (currentGMV != null && currentGMV > 0) ? 100.0 : 0.0;
            result.put("gmvDelta", gmvDelta);
            result.put("gmvDeltaDirection", "up");
            System.out.println("GMV Delta (no previous data): " + gmvDelta + "% (up)");
        }

        // Calculate Orders delta
        if (prevOrders != null && prevOrders > 0) {
            double ordersDelta = ((currentOrders != null ? currentOrders : 0L) - prevOrders) / (double) prevOrders * 100;
            result.put("ordersDelta", ordersDelta);
            result.put("ordersDeltaDirection", ordersDelta >= 0 ? "up" : "down");
            System.out.println("Orders Delta: " + ordersDelta + "% (" + (ordersDelta >= 0 ? "up" : "down") + ")");
        } else {
            // If previous period had no orders, calculate delta based on current value
            double ordersDelta = (currentOrders != null && currentOrders > 0) ? 100.0 : 0.0;
            result.put("ordersDelta", ordersDelta);
            result.put("ordersDeltaDirection", "up");
            System.out.println("Orders Delta (no previous data): " + ordersDelta + "% (up)");
        }

        // Calculate Stores delta (newly approved stores in period)
        long currentApprovedStores = storeRepository.countApprovedStoresBetweenDates(currentStart, currentEnd);
        long prevApprovedStores = storeRepository.countApprovedStoresBetweenDates(prevStart, prevEnd);

        System.out.println("Current Approved Stores: " + currentApprovedStores);
        System.out.println("Previous Approved Stores: " + prevApprovedStores);

        // Calculate absolute change in number of approved stores
        long storesDelta = currentApprovedStores - prevApprovedStores;
        result.put("storesDelta", storesDelta);
        result.put("storesDeltaDirection", storesDelta >= 0 ? "up" : "down");
        System.out.println("Stores Delta: " + storesDelta + " stores (" + (storesDelta >= 0 ? "up" : "down") + ")");

        // Calculate Buyers delta (new registrations in period)
        long currentNewBuyers = userRepository.countBuyersRegisteredBetweenDates(currentStart, currentEnd);
        long prevNewBuyers = userRepository.countBuyersRegisteredBetweenDates(prevStart, prevEnd);

        System.out.println("Current New Buyers: " + currentNewBuyers);
        System.out.println("Previous New Buyers: " + prevNewBuyers);

        // Calculate absolute change in number of new buyers
        long buyersDelta = currentNewBuyers - prevNewBuyers;
        result.put("buyersDelta", buyersDelta);
        result.put("buyersDeltaDirection", buyersDelta >= 0 ? "up" : "down");
        System.out.println("Buyers Delta: " + buyersDelta + " buyers (" + (buyersDelta >= 0 ? "up" : "down") + ")");

        System.out.println("========================");
    }

    private Map<String, Double> getWeeklyGMV(LocalDateTime now) {
        Map<String, Double> weeklyGMV = new HashMap<>();
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            Double dayGMV = orderRepository.sumRevenueByPeriod(dayStart, dayEnd);
            weeklyGMV.put(days[6 - i], dayGMV != null ? dayGMV : 0.0);
        }

        return weeklyGMV;
    }

    private Map<String, Long> getWeeklyOrders(LocalDateTime now) {
        Map<String, Long> weeklyOrders = new HashMap<>();
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            Long dayOrders = orderRepository.countOrdersByPeriod(dayStart, dayEnd);
            weeklyOrders.put(days[6 - i], dayOrders != null ? dayOrders : 0L);
        }

        return weeklyOrders;
    }
}
