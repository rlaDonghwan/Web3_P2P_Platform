package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.model.Order;
import com.inhatc.SafeCommerce.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 특정 사용자와 관련된 모든 주문 검색
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findOrdersByUserId(@Param("userId") Long userId);

    // 특정 상태의 주문 검색
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findOrdersByStatus(@Param("status") OrderStatus status);
}
