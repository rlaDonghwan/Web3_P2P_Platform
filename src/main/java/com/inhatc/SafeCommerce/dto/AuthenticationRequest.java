package com.inhatc.SafeCommerce.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {
    private String address;
    private String signature;

}