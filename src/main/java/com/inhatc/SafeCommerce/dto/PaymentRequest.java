package com.inhatc.SafeCommerce.dto;

import lombok.Data;


@Data
public class PaymentRequest {
    private Long orderId;         // 주문 ID
    private String buyerName;     // 구매자 이름
    private String buyerAddress;  // 구매자 주소
    private String buyerContact;  // 구매자 연락처
    private String ethAmount;     // 결제 금액 (ETH)
    private String transactionHash; // 트랜잭션 해시

    // Getters and Setters
}