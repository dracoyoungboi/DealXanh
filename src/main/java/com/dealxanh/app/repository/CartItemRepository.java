package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Cart;
import com.dealxanh.app.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    List<CartItem> findByCartCartId(Long cartId);
    void deleteByCart(Cart cart);

    /** Tìm tất cả CartItem có product nằm trong danh sách, join sẵn Cart + User để gửi thông báo */
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cart c JOIN FETCH c.user WHERE ci.product.productId IN :productIds")
    List<CartItem> findByProductIdsWithCartAndUser(@Param("productIds") List<Long> productIds);

    /** Xóa tất cả CartItem tham chiếu đến một product (dùng khi xóa sản phẩm) */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
