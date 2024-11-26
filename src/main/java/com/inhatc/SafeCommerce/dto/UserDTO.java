package com.inhatc.SafeCommerce.dto;


import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String accountId;
    private Integer nonce;

    public UserDTO(Long id, String accountId, Integer nonce) {
        this.id = id;
        this.accountId = accountId;
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        if (accountId != null && accountId.length() > 10) {
            return accountId.substring(0, 10); // Ethereum 주소 앞 10자리만 반환
        }
        return accountId; // null이거나 10자 이하일 경우 그대로 반환
    }

}