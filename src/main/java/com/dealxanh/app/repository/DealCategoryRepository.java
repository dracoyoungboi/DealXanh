package com.dealxanh.app.repository;

import com.dealxanh.app.entity.DealCategory;
import com.dealxanh.app.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealCategoryRepository extends JpaRepository<DealCategory, Long> {

    @Query("SELECT dc FROM DealCategory dc WHERE dc.deal = :deal AND dc.category = :category")
    DealCategory findByDealAndCategory(@Param("deal") Deal deal, @Param("category") com.dealxanh.app.entity.Category category);

    @Query("SELECT dc FROM DealCategory dc WHERE dc.deal = :deal AND dc.active = true ORDER BY dc.priority ASC")
    List<DealCategory> findByDealAndActiveTrue(@Param("deal") Deal deal);

    @Query("SELECT dc FROM DealCategory dc WHERE dc.category = :category AND dc.active = true")
    List<DealCategory> findActiveByCategory(@Param("category") com.dealxanh.app.entity.Category category);

    List<DealCategory> findByDeal_DealId(Long dealId);

    void deleteByDeal_DealId(Long dealId);

    /** Xóa tất cả DealCategory của một deal bằng native query (tránh load entity) */
    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM deal_categories WHERE deal_id = :dealId", nativeQuery = true)
    void deleteByDealIdDirect(@Param("dealId") Long dealId);
}
