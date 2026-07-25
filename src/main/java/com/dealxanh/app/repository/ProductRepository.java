package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Available = active, not deleted, stock > 0, within deal time window
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableProducts(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND p.category.categoryId = :categoryId " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableByCategory(@Param("categoryId") Long categoryId,
                                          @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> searchAvailable(@Param("keyword") String keyword,
                                  @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND p.store.storeId = :storeId " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    List<Product> findAvailableByStore(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

    // For store owner management
    Page<Product> findByStoreStoreIdAndDeletedFalse(Long storeId, Pageable pageable);

    // Find by store and deleted false (returns List)
    List<Product> findByStoreStoreIdAndDeletedFalse(Long storeId);

    // Find by category and deleted false
    List<Product> findByCategoryAndDeletedFalse(com.dealxanh.app.entity.Category category);

    // For admin
    Page<Product> findByDeletedFalse(Pageable pageable);

    // Approval workflow
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.approvalStatus = :status")
    Page<Product> findByApprovalStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.approvalStatus = 'PENDING'")
    List<Product> findPendingApproval();

    long countByDeletedFalseAndActive(Boolean active);

    long countByDeletedFalseAndApprovalStatus(String status);

    long countByStoreStoreIdAndDeletedFalse(Long storeId);

    // Count expired or out of stock products
    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND (p.expiryDate < CURRENT_TIMESTAMP OR p.stockQuantity <= 0)")
    long countExpiredOrOutOfStock();

    // Count all non-deleted products
    long countByDeletedFalse();

    // For expiry countdown: get all active non-deleted products
    List<Product> findByDeletedFalseAndActiveTrue();

    // For combo: get active products with HSD warning
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.active = true AND p.store.storeId = :storeId AND p.productType = 'SPECIFIC_DEAL'")
    List<Product> findComboAvailableByStore(@Param("storeId") Long storeId);

    // Standalone COMBO products (productType = "COMBO") for buyer display
    List<Product> findByProductTypeAndActiveTrueAndDeletedFalseAndApprovalStatus(
            String productType, String approvalStatus);

    // Pessimistic write lock for stock deduction concurrency (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // Direct update for product edit (avoid JPA cascade issues)
    @Modifying
    @Query("UPDATE Product p SET p.name = :name, p.description = :description, p.originalPrice = :price, p.stockQuantity = :stock, p.imageUrl = :imageUrl, p.expiryDate = :expiryDate, p.category.categoryId = :categoryId, p.approvalStatus = 'APPROVED', p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void updateProductFields(@Param("id") Long id, @Param("name") String name, @Param("description") String description, @Param("price") Double price, @Param("stock") Integer stock, @Param("imageUrl") String imageUrl, @Param("expiryDate") java.time.LocalDateTime expiryDate, @Param("categoryId") Long categoryId);

    /** Hoàn stock khi hủy đơn (auto-cancel) — tránh load/save entity gây version=null */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty, p.active = true, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void restoreStock(@Param("id") Long id, @Param("qty") int qty);

    /** Trừ stock khi đặt hàng — @Modifying tránh version conflict */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty, p.active = CASE WHEN (p.stockQuantity - :qty) <= 0 THEN false ELSE p.active END, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void deductStock(@Param("id") Long id, @Param("qty") int qty);

    /** Approve product — tránh entity save gây version conflict */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.approvalStatus = 'APPROVED', p.active = true, p.rejectionReason = null, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void approveProduct(@Param("id") Long id);

    /** Reject product — tránh entity save gây version conflict */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.approvalStatus = 'REJECTED', p.active = false, p.rejectionReason = :reason, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void rejectProduct(@Param("id") Long id, @Param("reason") String reason);

    /** Toggle active — tránh entity save gây version conflict */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.active = :active, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void setActive(@Param("id") Long id, @Param("active") Boolean active);

    /** Soft delete product — tránh JPA cascade/version conflict khi gọi save() */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.deleted = true, p.active = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void softDelete(@Param("id") Long id);

    /** Sản phẩm không thuộc bất kỳ deal ACTIVE nào — dùng cho "Gợi ý hôm nay" */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.approvalStatus = 'APPROVED' " +
           "AND p.productId NOT IN (SELECT dp.product.productId FROM DealProduct dp WHERE dp.deal.status = 'ACTIVE')")
    Page<Product> findProductsNotInActiveDeal(Pageable pageable);

    /** Home-page standalone COMBO products with Store eagerly fetched.
     *  Replaces the derived query that triggered lazy-load for p.store in the loop. */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.store s " +
           "WHERE p.productType = 'COMBO' " +
           "AND p.active = true AND p.deleted = false " +
           "AND p.stockQuantity > 0 AND p.approvalStatus = 'APPROVED' " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now) " +
           "ORDER BY p.productId ASC")
    List<Product> findHomeStandaloneCombos(@Param("now") LocalDateTime now);

    /** Home-page suggested products (not in any ACTIVE deal) with Store eagerly fetched.
     *  Also checks deal time window. Max 50 results via Pageable.
     *  Replaces findProductsNotInActiveDeal + lazy-load for p.store in the loop. */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.store s " +
           "WHERE p.active = true AND p.deleted = false " +
           "AND p.stockQuantity > 0 AND p.approvalStatus = 'APPROVED' " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now) " +
           "AND p.productId NOT IN (" +
           "  SELECT dp.product.productId FROM DealProduct dp " +
           "  WHERE dp.deal.status = 'ACTIVE' " +
           "  AND dp.deal.startTime <= :now AND dp.deal.endTime >= :now" +
           ") " +
           "ORDER BY p.productId DESC")
    List<Product> findHomeSuggestedProducts(@Param("now") LocalDateTime now, Pageable pageable);
}
