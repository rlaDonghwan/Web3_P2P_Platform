package com.inhatc.SafeCommerce.dto;

public class CheckoutResponse {
    private boolean success;
    private String message;
    private int totalPrice;

    public CheckoutResponse(boolean success, String message, int totalPrice) {
        this.success = success;
        this.message = message;
        this.totalPrice = totalPrice;
    }

    // Getter/Setter 생략
}
