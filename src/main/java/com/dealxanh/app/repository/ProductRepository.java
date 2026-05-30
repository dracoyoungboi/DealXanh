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

    // Combo / Blind Box filter (supports both legacy BLIND_BOX and new COMBO type)
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND (p.productType = 'BLIND_BOX' OR p.productType = 'COMBO') " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableBlindBoxes(@Param("now") LocalDateTime now, Pageable pageable);

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

    // Pessimistic write lock for stock deduction concurrency (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // Direct update for product edit (avoid JPA cascade issues)
    @Modifying
    @Query("UPDATE Product p SET p.name = :name, p.description = :description, p.originalPrice = :price, p.stockQuantity = :stock, p.imageUrl = :imageUrl, p.expiryDate = :expiryDate, p.category.categoryId = :categoryId, p.approvalStatus = 'PENDING', p.updatedAt = CURRENT_TIMESTAMP WHERE p.productId = :id")
    void updateProductFields(@Param("id") Long id, @Param("name") String name, @Param("description") String description, @Param("price") Double price, @Param("stock") Integer stock, @Param("imageUrl") String imageUrl, @Param("expiryDate") java.time.LocalDateTime expiryDate, @Param("categoryId") Long categoryId);
}
