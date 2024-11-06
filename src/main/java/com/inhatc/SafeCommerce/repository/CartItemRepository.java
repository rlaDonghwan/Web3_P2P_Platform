package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}