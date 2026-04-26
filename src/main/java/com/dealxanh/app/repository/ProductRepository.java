package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Available = active, not deleted, stock > 0, within deal time window
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableProducts(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.category.categoryId = :categoryId " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableByCategory(@Param("categoryId") Long categoryId,
                                          @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> searchAvailable(@Param("keyword") String keyword,
                                  @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.store.storeId = :storeId " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    List<Product> findAvailableByStore(@Param("storeId") Long storeId, @Param("now") LocalDateTime now);

    // For store owner management
    Page<Product> findByStoreStoreIdAndDeletedFalse(Long storeId, Pageable pageable);

    // Blind Box filter
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.deleted = false AND p.stockQuantity > 0 " +
           "AND p.productType = 'BLIND_BOX'" +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now)")
    Page<Product> findAvailableBlindBoxes(@Param("now") LocalDateTime now, Pageable pageable);

    // For admin
    Page<Product> findByDeletedFalse(Pageable pageable);

    long countByDeletedFalseAndActive(Boolean active);

    long countByStoreStoreIdAndDeletedFalse(Long storeId);
}
