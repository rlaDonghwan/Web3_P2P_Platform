package com.inhatc.SafeCommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemDTO {
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private int price;
    private int quantity;
    private String base64Image; // Base64 이미지 데이터

}