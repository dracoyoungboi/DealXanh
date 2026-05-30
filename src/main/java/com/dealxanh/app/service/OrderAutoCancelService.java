package com.dealxanh.app.service;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.OrderItem;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class OrderAutoCancelService {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ResourceLockManager lockManager;

    /**
     * Runs every 30 minutes to auto-cancel stale orders.
     */
    @Scheduled(fixedRate = 1_800_000) // 30 minutes
    public void autoCancelOrders() {
        log.info("OrderAutoCancelService: checking for stale orders...");
        LocalDateTime now = LocalDateTime.now();

        // 1. Cancel PENDING orders older than 60 minutes
        cancelStalePendingOrders(now);

        // 2. Cancel READY_FOR_PICKUP orders where:
        //    - Product has expired OR
        //    - 2 days have passed since READY_FOR_PICKUP
        cancelStaleReadyOrders(now);
    }

    private void cancelStalePendingOrders(LocalDateTime now) {
        LocalDateTime deadline = now.minusMinutes(60);
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (!"PENDING".equals(order.getStatus())) continue;
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(deadline)) {
                cancelOrder(order, "Tự động hủy: quá 60 phút không được xác nhận");
            }
        }
    }

    private void cancelStaleReadyOrders(LocalDateTime now) {
        LocalDateTime twoDaysAgo = now.minusDays(2);
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (!"READY_FOR_PICKUP".equals(order.getStatus())) continue;

            boolean shouldCancel = false;
            String reason = "";

            // Check if 2 days passed since status became READY_FOR_PICKUP
            if (order.getUpdatedAt() != null && order.getUpdatedAt().isBefore(twoDaysAgo)) {
                shouldCancel = true;
                reason = "Tự động hủy: quá 2 ngày khách không đến nhận";
            }

            // Check for expired products in order
            if (!shouldCancel && order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Product p = item.getProduct();
                    if (p != null && p.getExpiryDate() != null && p.getExpiryDate().isBefore(now)) {
                        shouldCancel = true;
                        reason = "Tự động hủy: sản phẩm '" + p.getName() + "' đã hết hạn (" + p.getExpiryDate().toLocalDate() + ")";
                        break;
                    }
                }
            }

            if (shouldCancel) {
                cancelOrder(order, reason);
            }
        }
    }

    @Transactional
    void cancelOrder(Order order, String reason) {
        ReentrantLock lock = lockManager.acquireLock("ORDER", order.getOrderId());
        try {
            Order fresh = orderRepository.findById(order.getOrderId()).orElse(null);
            if (fresh == null) return;
            if ("CANCELLED".equals(fresh.getStatus()) || "COMPLETED".equals(fresh.getStatus())) return;

            // Use @Modifying query to avoid JPA version/flush issues
            orderRepository.cancelOrder(fresh.getOrderId(), reason);

            // Restore stock
            if (fresh.getOrderItems() != null) {
                for (OrderItem item : fresh.getOrderItems()) {
                    Product p = item.getProduct();
                    if (p != null) {
                        p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                        if (!p.getActive() && p.getStockQuantity() > 0) p.setActive(true);
                        productRepository.save(p);
                    }
                }
            }

            log.info("Order #{} auto-cancelled: {}", fresh.getOrderId(), reason);
        } finally {
            lock.unlock();
        }
    }
}
