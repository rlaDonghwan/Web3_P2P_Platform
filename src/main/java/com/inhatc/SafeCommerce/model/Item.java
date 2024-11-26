package com.inhatc.SafeCommerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    private String itemName; // 상품 이름
    private String itemDescription; // 상품 설명
    private int price; // 가격
    private int quantity; // 수량

    @Column(nullable = false)
    private boolean isDeleted = false; // 삭제 여부

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // User와의 외래키 설정
    private User user; // 상품을 등록한 사용자
}

