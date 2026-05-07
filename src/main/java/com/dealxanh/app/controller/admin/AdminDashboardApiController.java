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
