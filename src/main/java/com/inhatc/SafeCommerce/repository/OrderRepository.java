package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
