package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByStoreStoreIdOrderByCreatedAtDesc(Long storeId);

    List<Transaction> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.store.storeId = :storeId AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByStoreAndDateRange(@Param("storeId") Long storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // GMV = sum of all completed order amounts
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'SALE' AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :start AND :end")
    Double sumGMV(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Commission = sum of platform fees
    @Query("SELECT COALESCE(SUM(t.platformFee), 0) FROM Transaction t WHERE t.status = 'COMPLETED' AND t.createdAt BETWEEN :start AND :end")
    Double sumCommission(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Paid payouts = sum of completed payouts
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'PAYOUT' AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :start AND :end")
    Double sumPaidPayouts(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Pending payouts count
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = 'PAYOUT' AND t.status = 'PENDING'")
    Long countPendingPayouts();

    // Pending payouts amount
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'PAYOUT' AND t.status = 'PENDING'")
    Double sumPendingPayouts();

    // Transactions by type
    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByTypeAndDateRange(@Param("type") String type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Daily GMV for chart (last N days)
    @Query(value = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.type = 'SALE' AND t.status = 'COMPLETED' AND DATE(t.created_at) = DATE(:date)", nativeQuery = true)
    Double sumGMVByDate(@Param("date") java.time.LocalDate date);

    // Daily commission for chart
    @Query(value = "SELECT COALESCE(SUM(t.platform_fee), 0) FROM transactions t WHERE t.status = 'COMPLETED' AND DATE(t.created_at) = DATE(:date)", nativeQuery = true)
    Double sumCommissionByDate(@Param("date") java.time.LocalDate date);

    // Pending reconciliation (transactions with issues)
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'PENDING' AND t.type != 'PAYOUT'")
    Long countPendingReconciliation();

    // Store-level pending payouts
    @Query("SELECT t FROM Transaction t JOIN FETCH t.store WHERE t.type = 'PAYOUT' AND t.status = 'PENDING' ORDER BY t.createdAt DESC")
    List<Transaction> findPendingPayoutsWithStore();

    // Count stores with pending payouts
    @Query("SELECT COUNT(DISTINCT t.store.storeId) FROM Transaction t WHERE t.type = 'PAYOUT' AND t.status = 'PENDING'")
    Long countStoresWithPendingPayouts();

    // ===== Store-Specific Wallet Queries =====

    // Total earned (SALE COMPLETED) for a store
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.store.storeId = :storeId AND t.type = 'SALE' AND t.status = 'COMPLETED'")
    Double sumEarnedByStore(@Param("storeId") Long storeId);

    // Total commission paid by a store
    @Query("SELECT COALESCE(SUM(t.platformFee), 0) FROM Transaction t WHERE t.store.storeId = :storeId AND t.status = 'COMPLETED'")
    Double sumCommissionByStore(@Param("storeId") Long storeId);

    // Total paid out (PAYOUT COMPLETED) for a store
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.store.storeId = :storeId AND t.type = 'PAYOUT' AND t.status = 'COMPLETED'")
    Double sumPaidOutByStore(@Param("storeId") Long storeId);

    // Pending payout for a store
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.store.storeId = :storeId AND t.type = 'PAYOUT' AND t.status = 'PENDING'")
    Double sumPendingPayoutByStore(@Param("storeId") Long storeId);

    // Store transactions with optional type filter, paginated
    @Query("SELECT t FROM Transaction t WHERE t.store.storeId = :storeId AND (:type IS NULL OR t.type = :type) ORDER BY t.createdAt DESC")
    List<Transaction> findByStoreAndType(@Param("storeId") Long storeId, @Param("type") String type, org.springframework.data.domain.Pageable pageable);

    // Store transactions in date range with optional type filter
    @Query("SELECT t FROM Transaction t WHERE t.store.storeId = :storeId AND t.createdAt BETWEEN :start AND :end AND (:type IS NULL OR t.type = :type) ORDER BY t.createdAt DESC")
    List<Transaction> findByStoreAndDateRangeAndType(@Param("storeId") Long storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type, org.springframework.data.domain.Pageable pageable);
}
