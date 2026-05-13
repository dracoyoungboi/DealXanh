package com.dealxanh.app.repository;

import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT dp FROM DealProduct dp WHERE dp.deal = :deal ORDER BY dp.priority ASC")
    List<DealProduct> findByDeal(@Param("deal") Deal deal);

    @Query("SELECT dp FROM DealProduct dp WHERE dp.product = :product")
    List<DealProduct> findByProduct(@Param("product") Product product);

    List<DealProduct> findByDeal_DealId(Long dealId);

    void deleteByDeal_DealId(Long dealId);
}
