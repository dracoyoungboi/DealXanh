package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    // Find active deals
    List<Deal> findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc(
        String status, LocalDateTime startTime, LocalDateTime endTime
    );

    // Find by deal type
    Page<Deal> findByDealType(String dealType, Pageable pageable);

    // Find by status
    Page<Deal> findByStatus(String status, Pageable pageable);
    List<Deal> findByStatus(String status);

    // Find by store
    Page<Deal> findByStoreStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    // Find by store and status
    List<Deal> findByStoreStoreIdAndStatus(Long storeId, String status);

    // Find all deals by store
    List<Deal> findByStoreStoreId(Long storeId);

    // Find active deals for a store
    @Query("SELECT d FROM Deal d WHERE d.status = 'ACTIVE' AND d.startTime <= :now AND d.endTime >= :now AND (d.store.storeId = :storeId OR d.scope = 'ALL_STORES')")
    List<Deal> findActiveDealsForStore(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

    // Search deals
    @Query("SELECT d FROM Deal d WHERE d.dealName LIKE %:keyword% OR d.dealCode LIKE %:keyword%")
    Page<Deal> searchDeals(@Param("keyword") String keyword, Pageable pageable);

    // Count by status
    long countByStatus(String status);

    // Count by deal type
    long countByDealType(String dealType);

    // Find deals starting soon (within 24 hours)
    @Query("SELECT d FROM Deal d WHERE d.status = 'SCHEDULED' AND d.startTime BETWEEN :now AND :end")
    List<Deal> findStartingSoon(@Param("now") LocalDateTime now, @Param("end") LocalDateTime end);

    // Find expiring soon (within 24 hours)
    @Query("SELECT d FROM Deal d WHERE d.status = 'ACTIVE' AND d.endTime BETWEEN :now AND :end")
    List<Deal> findExpiringSoon(@Param("now") LocalDateTime now, @Param("end") LocalDateTime end);

    // Find all deals with stores info
    @Query("SELECT DISTINCT d FROM Deal d LEFT JOIN FETCH d.store LEFT JOIN FETCH d.createdBy ORDER BY d.createdAt DESC")
    List<Deal> findAllWithDetails();

    // Get deal statistics
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.status = 'ACTIVE'")
    Long countActiveDeals();

    @Query("SELECT COUNT(d) FROM Deal d WHERE d.status = 'SCHEDULED'")
    Long countScheduledDeals();

    // Check if deal code exists (excluding current deal)
    @Query("SELECT COUNT(d) FROM Deal d WHERE d.dealCode = :code AND d.status <> 'CANCELLED' AND (:dealId IS NULL OR d.dealId <> :dealId)")
    Long countByDealCodeAndStatusNotCancelled(@Param("code") String code, @Param("dealId") Long dealId);

    // Find active deal by code
    Optional<Deal> findByDealCodeAndStatus(String dealCode, String status);

    // Find overlapping deals for same store
    @Query("SELECT d FROM Deal d WHERE d.store.storeId = :storeId AND d.status IN ('SCHEDULED', 'ACTIVE') " +
           "AND d.startTime < :endTime AND d.endTime > :startTime " +
           "AND (:dealId IS NULL OR d.dealId <> :dealId)")
    List<Deal> findOverlappingDealsForStore(@Param("storeId") Long storeId, @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime, @Param("dealId") Long dealId);

    // Get average product price for store
    @Query("SELECT AVG(p.originalPrice) FROM Product p WHERE p.store.storeId = :storeId AND p.active = true AND p.deleted = false")
    Double getAverageProductPriceByStore(@Param("storeId") Long storeId);

    // Count approved products for store
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.storeId = :storeId AND p.approvalStatus = 'APPROVED' AND p.deleted = false")
    Long countApprovedProductsByStore(@Param("storeId") Long storeId);

    /** Chuyển ACTIVE deals đã quá endTime sang ENDED */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Deal d SET d.status = 'ENDED', d.updatedAt = CURRENT_TIMESTAMP WHERE d.status = 'ACTIVE' AND d.endTime IS NOT NULL AND d.endTime < CURRENT_TIMESTAMP")
    int endExpiredActiveDeals();

    /** Soft-delete deal: set status = CANCELLED (tránh JPA cascade/version conflict) */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Deal d SET d.status = 'CANCELLED', d.updatedAt = CURRENT_TIMESTAMP WHERE d.dealId = :id")
    void cancelDeal(@Param("id") Long id);
}
