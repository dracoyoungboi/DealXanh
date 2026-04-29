package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByStoreName(String storeName);

    Optional<Store> findByOwnerUserId(Long ownerId);

    Optional<Store> findByOwner(com.dealxanh.app.entity.User owner);

    List<Store> findByStatus(String status);

    Page<Store> findByStatus(String status, Pageable pageable);

    Page<Store> findByStoreNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    @Query("SELECT s FROM Store s WHERE s.status = 'ACTIVE' ORDER BY s.averageRating DESC")
    List<Store> findTopRatedStores(Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT s FROM Store s JOIN FETCH s.owner WHERE s.storeId = :id")
    Optional<Store> findByIdWithOwner(@Param("id") Long id);
}
