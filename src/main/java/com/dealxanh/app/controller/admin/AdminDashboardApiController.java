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
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) String date) {

        Map<String, Object> result = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate;

        // Handle specific date request
        if (date != null && !date.isEmpty()) {
            try {
                String[] parts = date.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                startDate = LocalDateTime.of(year, month, day, 0, 0);
                endDate = startDate.plusDays(1).minusSeconds(1);

                Double specificDateGMV = orderRepository.sumRevenueByPeriod(startDate, endDate);
                Long specificDateOrders = orderRepository.countOrdersByPeriod(startDate, endDate);

                result.put("todayGMV", specificDateGMV != null ? specificDateGMV : 0.0);
                result.put("todayOrders", specificDateOrders != null ? specificDateOrders : 0L);

                return result;
            } catch (Exception e) {
                // If date parsing fails, fall back to default behavior
                startDate = now.toLocalDate().atStartOfDay();
            }
        }

        switch (period) {
            case "7d":
                startDate = now.minusDays(7);
                break;
            case "30d":
                startDate = now.minusDays(30);
                break;
            case "3m":
                startDate = now.minusMonths(3);
                break;
            default:
                startDate = now.toLocalDate().atStartOfDay();
                break;
        }

        // Get daily data for chart
        List<Map<String, Object>> dailyData = new ArrayList<>();
        int days = period.equals("7d") ? 7 : (period.equals("30d") ? 30 : 1);

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            Double dayGMV = orderRepository.sumRevenueByPeriod(dayStart, dayEnd);
            Long dayOrders = orderRepository.countOrdersByPeriod(dayStart, dayEnd);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.toLocalDate().toString());
            dayData.put("gmv", dayGMV != null ? dayGMV : 0.0);
            dayData.put("orders", dayOrders != null ? dayOrders : 0L);

            dailyData.add(dayData);
        }

        result.put("dailyData", dailyData);

        // Today's stats
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);

        Double todayGMV = orderRepository.sumRevenueByPeriod(todayStart, todayEnd);
        Long todayOrders = orderRepository.countOrdersByPeriod(todayStart, todayEnd);

        result.put("todayGMV", todayGMV != null ? todayGMV : 0.0);
        result.put("todayOrders", todayOrders != null ? todayOrders : 0L);

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
}
