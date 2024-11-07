package com.inhatc.SafeCommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String accountId;
    private Integer nonce;

    public UserDTO(Long id, String accountId, Integer nonce) {
        this.id = id;
        this.accountId = accountId;
        this.nonce = nonce;
    }

}