package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductProductId(Long productId, Pageable pageable);

    Page<Review> findByStoreStoreId(Long storeId, Pageable pageable);

    boolean existsByUserUserIdAndOrderOrderId(Long userId, Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.storeId = :storeId")
    Double findAverageRatingByStore(@Param("storeId") Long storeId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId")
    Double findAverageRatingByProduct(@Param("productId") Long productId);

    long countByStoreStoreId(Long storeId);
}
