package com.inhatc.SafeCommerce.repository;

import com.inhatc.SafeCommerce.dto.OrderDetailDTO;
import com.inhatc.SafeCommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT new com.inhatc.SafeCommerce.dto.OrderDetailDTO(" +
            "o.id, i.itemName, o.orderDate, oi.count, " +
            "(oi.orderPrice * oi.count), o.buyerName, o.buyerAddress, o.buyerContact, o.transactionId, o.status) " +
            "FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.Item i " +
            "WHERE o.user.id = :userId")
    List<OrderDetailDTO> findOrderDetailsByUserId(@Param("userId") Long userId);
}