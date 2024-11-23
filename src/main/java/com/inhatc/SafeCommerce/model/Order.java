package com.inhatc.SafeCommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate orderDate; // 주문 일자

    private String buyerName; // 구매자 이름

    private String buyerAddress; // 구매자 주소

    private String buyerContact; // 구매자 연락처

    private String transactionId; // 트랜잭션 ID 저장

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}