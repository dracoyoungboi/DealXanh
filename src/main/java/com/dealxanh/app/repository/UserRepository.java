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

    // Find users by role object
    List<User> findByRole(com.dealxanh.app.entity.Role role);

    // Find top 5 users by created date
    List<User> findTop5ByOrderByCreatedAtDesc();

    // Active/Inactive status methods
    List<User> findByActive(Boolean active);

    long countByActive(Boolean active);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.active = :active ORDER BY u.createdAt DESC")
    List<User> findByActiveWithRole(@Param("active") Boolean active);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.active = :active AND LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY u.createdAt DESC")
    List<User> searchActiveUsersWithRole(@Param("active") Boolean active, @Param("search") String search);

    // Count active/inactive buyers
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = 'ROLE_USER' AND u.active = :active")
    long countByRoleAndActive(@Param("active") Boolean active);

    // Count new buyer registrations between dates
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = 'ROLE_USER' AND u.createdAt BETWEEN :startDate AND :endDate")
    long countBuyersRegisteredBetweenDates(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}
