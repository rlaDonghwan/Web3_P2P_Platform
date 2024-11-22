package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long user_id);

    @Query("SELECT c FROM Cart c " +
            "JOIN FETCH c.cartItems ci " +
            "JOIN FETCH ci.item i " +
            "JOIN FETCH i.user u " +
            "WHERE c.id = :cartId")
    Optional<Cart> findCartWithDetails(@Param("cartId") Long cartId);
}
