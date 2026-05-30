package com.dealxanh.app.repository;

import com.dealxanh.app.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserUserIdOrderByIsDefaultDesc(Long userId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.userId = :userId")
    void clearDefault(@Param("userId") Long userId);
}
