package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT d FROM Dispute d JOIN FETCH d.complainant JOIN FETCH d.order WHERE d.disputeId = :id")
    Dispute findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT d FROM Dispute d JOIN FETCH d.complainant ORDER BY d.createdAt DESC")
    List<Dispute> findAllWithComplainant();

    @Query("SELECT d FROM Dispute d JOIN FETCH d.complainant WHERE d.status = :status ORDER BY d.createdAt DESC")
    List<Dispute> findByStatusWithComplainant(@Param("status") String status);

    long countByStatus(String status);

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'PENDING'")
    Long countPendingDisputes();

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'REVIEWING'")
    Long countReviewingDisputes();

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'RESOLVED_REFUND'")
    Long countResolvedRefundDisputes();

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'RESOLVED_REJECTED'")
    Long countResolvedRejectedDisputes();
}
