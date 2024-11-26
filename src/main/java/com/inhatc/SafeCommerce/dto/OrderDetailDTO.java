package com.inhatc.SafeCommerce.dto;

import com.inhatc.SafeCommerce.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderDetailDTO {
    private Long orderId; // 주문 ID
    private String itemName; // 상품 이름
    private LocalDate orderDate; // 주문 날짜
    private int count; // 주문 수량
    private int totalPrice; // 총 가격
    private String buyerName; // 구매자 이름
    private String buyerAddress; // 구매자 주소
    private String buyerContact; // 구매자 연락처
    private String transactionId; // 트랜잭션 ID
    private OrderStatus status; // 주문 상태
}