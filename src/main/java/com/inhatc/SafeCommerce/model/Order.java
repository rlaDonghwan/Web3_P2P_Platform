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

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 구매 상태

    // Order와 OrderItem 관계 설정
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // orderItems에 OrderItem 추가하는 메서드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}