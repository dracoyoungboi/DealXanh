package com.dealxanh.app.repository;

import com.dealxanh.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByResetToken(String resetToken);

    // Load user kèm role dùng JOIN FETCH (tránh lazy loading)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    // Tìm theo email (trả về List để xử lý trường hợp email trùng)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email ORDER BY u.userId DESC")
    List<User> findAllByEmailWithRole(@Param("email") String email);

    // Lấy tất cả user kèm role cho admin panel
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role ORDER BY u.userId DESC")
    List<User> findAllUsersWithRole();

    // Phân trang có search
    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.role WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsersWithRole(@Param("search") String search, Pageable pageable);

    long countByRoleName(String roleName);
}
