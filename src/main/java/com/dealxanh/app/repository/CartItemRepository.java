package com.dealxanh.app.repository;

import com.dealxanh.app.entity.Cart;
import com.dealxanh.app.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    List<CartItem> findByCartCartId(Long cartId);
    void deleteByCart(Cart cart);
}
