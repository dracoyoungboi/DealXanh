
package com.dealxanh.app.repository;

import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DealProductRepository extends JpaRepository<DealProduct, Long> {

    // Find products by deal
    List<DealProduct> findByDealDealIdOrderByPriorityAsc(Long dealId);

    // Find deals by product
    List<DealProduct> findByProductProductId(Long productId);

    // Count products in deal
    long countByDealDealId(Long dealId);

    // Find available deal products
    @Query("SELECT dp FROM DealProduct dp WHERE dp.deal.dealId = :dealId AND (dp.maxQuantity IS NULL OR dp.soldQuantity < dp.maxQuantity)")
    List<DealProduct> findAvailableProductsByDeal(@Param("dealId") Long dealId);

    @Query("SELECT dp FROM DealProduct dp WHERE dp.deal = :deal AND dp.product = :product")
    DealProduct findByDealAndProduct(@Param("deal") Deal deal, @Param("product") Product product);

    /** Check if any DealProduct exists for a deal+product pair (ignores duplicates) */
    @Query("SELECT COUNT(dp) > 0 FROM DealProduct dp WHERE dp.deal.dealId = :dealId AND dp.product.productId = :productId")
    boolean existsByDealIdAndProductId(@Param("dealId") Long dealId, @Param("productId") Long productId);

    /** Xóa TẤT CẢ bản ghi DealProduct theo deal+product — xử lý cả trường hợp có duplicate rows */
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM deal_products WHERE deal_id = :dealId AND product_id = :productId", nativeQuery = true)
    int deleteByDealIdAndProductId(@Param("dealId") Long dealId, @Param("productId") Long productId);

    @Query("SELECT dp FROM DealProduct dp WHERE dp.deal = :deal ORDER BY dp.priority ASC")
    List<DealProduct> findByDeal(@Param("deal") Deal deal);

    @Query("SELECT dp FROM DealProduct dp WHERE dp.product = :product")
    List<DealProduct> findByProduct(@Param("product") Product product);

    List<DealProduct> findByDeal_DealId(Long dealId);

    void deleteByDeal_DealId(Long dealId);

    /** Xóa tất cả DealProduct của một deal bằng native query (tránh load entity + version check) */
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM deal_products WHERE deal_id = :dealId", nativeQuery = true)
    void deleteByDealIdDirect(@Param("dealId") Long dealId);

    /** Sản phẩm đang sale: DealProduct có deal ACTIVE, trong time window, sản phẩm available */
    @Query(value = "SELECT dp FROM DealProduct dp JOIN FETCH dp.product p JOIN FETCH dp.deal d " +
           "WHERE d.status = 'ACTIVE' AND d.startTime <= :now AND d.endTime >= :now " +
           "AND p.active = true AND p.deleted = false AND p.stockQuantity > 0 AND p.approvalStatus = 'APPROVED' " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(dp) FROM DealProduct dp JOIN dp.product p JOIN dp.deal d " +
           "WHERE d.status = 'ACTIVE' AND d.startTime <= :now AND d.endTime >= :now " +
           "AND p.active = true AND p.deleted = false AND p.stockQuantity > 0 AND p.approvalStatus = 'APPROVED' " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DealProduct> findActiveDealProducts(@Param("now") LocalDateTime now,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    /** Home-page batch: all active DealProducts with Deal + Product + both Stores eagerly fetched.
     *  Replaces the N+1 pattern of dealRepository → for-each deal → dealProductRepository.findByDeal(deal).
     *  Sorted by deal priority DESC, then dealProduct priority ASC, then id ASC for stable output. */
    @Query("SELECT dp FROM DealProduct dp " +
           "JOIN FETCH dp.deal d " +
           "JOIN FETCH dp.product p " +
           "LEFT JOIN FETCH d.store ds " +
           "LEFT JOIN FETCH p.store ps " +
           "WHERE d.status = 'ACTIVE' " +
           "AND d.startTime <= :now AND d.endTime >= :now " +
           "AND p.active = true AND p.deleted = false " +
           "AND p.stockQuantity > 0 AND p.approvalStatus = 'APPROVED' " +
           "AND (p.dealStartTime IS NULL OR p.dealStartTime <= :now) " +
           "AND (p.dealEndTime IS NULL OR p.dealEndTime >= :now) " +
           "AND (dp.maxQuantity IS NULL OR COALESCE(dp.soldQuantity, 0) < dp.maxQuantity) " +
           "ORDER BY " +
           "  CASE WHEN d.priority IS NULL THEN 0 ELSE d.priority END DESC, " +
           "  CASE WHEN dp.priority IS NULL THEN 0 ELSE dp.priority END ASC, " +
           "  dp.dealProductId ASC")
    List<DealProduct> findHomeActiveDealProducts(@Param("now") LocalDateTime now);
}
