package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // For user order history
    Page<Order> findByUserUserId(Long userId, Pageable pageable);

    Page<Order> findByUserUserIdAndStatus(Long userId, String status, Pageable pageable);

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    // For store owner
    Page<Order> findByStoreStoreId(Long storeId, Pageable pageable);

    Page<Order> findByStoreStoreIdAndStatus(Long storeId, String status, Pageable pageable);

    List<Order> findByStoreStoreIdAndStatus(Long storeId, String status);

    // Load with items for detail view
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.orderId = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.store LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.orderId = :id")
    Optional<Order> findByIdWithAllDetails(@Param("id") Long id);

    // For admin dashboard
    long countByStatus(String status);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt >= :from AND o.createdAt <= :to")
    Double sumRevenueByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt <= :to")
    Long countOrdersByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Find by QR code (for store scan pickup)
    Optional<Order> findByPickupQrCode(String qrCode);

    // Pessimistic write lock for payment concurrency (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    // ============== ADMIN ORDERS PAGE STATISTICS ==============

    // Count orders by store
    long countByStoreStoreId(Long storeId);
    long countByStoreStoreIdAndStatus(Long storeId, String status);

    // Sum revenue by store (completed orders)
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o WHERE o.store.storeId = :storeId AND o.status = 'COMPLETED'")
    Double sumCompletedRevenueByStore(@Param("storeId") Long storeId);

    // Count pending orders by store
    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.storeId = :storeId AND o.status IN ('PENDING', 'CONFIRMED', 'READY_FOR_PICKUP')")
    long countPendingOrdersByStore(@Param("storeId") Long storeId);

    // Get all orders by store with pagination
    Page<Order> findByStoreStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    // Get orders by store with status filter
    Page<Order> findByStoreStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, String status, Pageable pageable);

    // Get all distinct stores that have orders
    @Query("SELECT DISTINCT o.store FROM Order o WHERE o.store IS NOT NULL ORDER BY o.store.storeName")
    java.util.List<Store> findAllStoresWithOrders();

    // === Analytics Queries ===

    // Daily order count for chart (last N days)
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = DATE(:date)")
    Long countOrdersByDate(@Param("date") java.time.LocalDate date);

    // Daily completed revenue for chart
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND DATE(o.createdAt) = DATE(:date)")
    Double sumRevenueByDate(@Param("date") java.time.LocalDate date);

    // Average order value
    @Query("SELECT COALESCE(AVG(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    Double averageOrderValue();

    // Average order value for period
    @Query("SELECT COALESCE(AVG(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    Double averageOrderValueByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Top stores by revenue
    @Query("SELECT o.store, COALESCE(SUM(o.finalAmount), 0) as revenue FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to GROUP BY o.store ORDER BY revenue DESC")
    java.util.List<Object[]> topStoresByRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Order status distribution
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to GROUP BY o.status")
    java.util.List<Object[]> orderStatusDistribution(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Pickup completion rate
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    Long countCompletedPickups(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Cancellation rate
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' AND o.createdAt BETWEEN :from AND :to")
    Long countCancelledOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Total orders between dates
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    Long totalOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
