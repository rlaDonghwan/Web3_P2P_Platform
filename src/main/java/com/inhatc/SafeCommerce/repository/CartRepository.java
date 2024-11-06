package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long user_id);
}
