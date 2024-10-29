package com.inhatc.SafeCommerce.dto;

public class UserDTO {
    private Long id;
    private String accountId;
    private Integer nonce;

    public UserDTO(Long id, String accountId, Integer nonce) {
        this.id = id;
        this.accountId = accountId;
        this.nonce = nonce;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }
}