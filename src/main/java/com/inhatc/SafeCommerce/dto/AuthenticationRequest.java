package com.inhatc.SafeCommerce.dto;

public class AuthenticationRequest {
    private String address;
    private String signature;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}