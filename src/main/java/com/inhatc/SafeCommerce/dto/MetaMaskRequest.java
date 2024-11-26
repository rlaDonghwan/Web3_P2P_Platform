package com.inhatc.SafeCommerce.dto;


import lombok.Data;

@Data
public class MetaMaskRequest {
    private String account;
    private String signature;


}