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
import org.springframework.context.annotation.Lazy;
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

    /** Self-injection để gọi @Transactional qua AOP proxy, tránh self-invocation bug */
    @Autowired
    @Lazy
    private OrderAutoCancelService self;

    /**
     * Runs every 30 minutes to auto-cancel stale orders.
     */
    @Scheduled(fixedRate = 1_800_000) // 30 minutes
    @Transactional
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
        LocalDateTime deadline = now.minusMinutes(30);
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (!"PENDING".equals(order.getStatus())) continue;
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(deadline)) {
                self.cancelOrder(order, "Tự động hủy: quá 30 phút không được xác nhận hoặc thanh toán");
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
                self.cancelOrder(order, reason);
            }
        }
    }

    @Transactional
    void cancelOrder(Order order, String reason) {
        ReentrantLock lock = lockManager.acquireLock("ORDER", order.getOrderId());
        try {
            // Load with items eagerly to snapshot before @Modifying query
            Order fresh = orderRepository.findByIdWithItems(order.getOrderId()).orElse(null);
            if (fresh == null) return;
            if ("CANCELLED".equals(fresh.getStatus()) || "COMPLETED".equals(fresh.getStatus())) return;

            // Snapshot item info BEFORE @Modifying query runs (clearAutomatically detaches everything)
            List<long[]> itemSnapshots = new java.util.ArrayList<>();
            if (fresh.getOrderItems() != null) {
                for (OrderItem item : fresh.getOrderItems()) {
                    Product p = item.getProduct();
                    if (p != null && item.getQuantity() != null && item.getQuantity() > 0) {
                        itemSnapshots.add(new long[]{p.getProductId(), item.getQuantity()});
                    }
                }
            }

            // @Modifying(clearAutomatically=true) — UPDATE trực tiếp, clear EntityManager
            orderRepository.cancelOrder(fresh.getOrderId(), reason);

            // Restore stock dùng @Modifying query — không load/save entity, tránh version=null
            for (long[] snap : itemSnapshots) {
                productRepository.restoreStock(snap[0], (int) snap[1]);
            }

            log.info("Order #{} auto-cancelled: {}", fresh.getOrderId(), reason);
        } finally {
            lock.unlock();
        }
    }
}
