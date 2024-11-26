package com.inhatc.SafeCommerce.dto;


import lombok.Data;

@Data
public class AuthenticationRequest {
    private String address;
    private String signature;

}