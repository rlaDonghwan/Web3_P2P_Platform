package com.inhatc.SafeCommerce.dto;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String message;
    private String address;

    public AuthenticationResponse(String message, String address) {
        this.message = message;
        this.address = address;
    }

}