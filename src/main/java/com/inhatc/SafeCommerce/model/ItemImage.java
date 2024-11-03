package com.inhatc.SafeCommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Lob
    private byte[] imageData;

    @Transient
    private String base64Image; // Base64로 인코딩된 이미지, DB에는 저장되지 않음
}