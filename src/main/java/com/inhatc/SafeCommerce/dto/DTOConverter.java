package com.inhatc.SafeCommerce.dto;

import com.inhatc.SafeCommerce.model.User;

public class DTOConverter {
    public static UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getAccountId(), user.getNonce());
    }
}