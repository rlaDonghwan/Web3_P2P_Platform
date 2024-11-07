package com.inhatc.SafeCommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    private String itemName; // 상품 이름
    private String itemDescription; // 상품 설명
    private int price; // 가격
    private int quantity; // 수량

    // images 필드를 MultipartFile로 변경
    @Transient // 데이터베이스에 저장되지 않도록 설정
    private List<MultipartFile> images = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImage> itemImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // User와의 외래키 설정
    private User user; // 상품을 등록한 사용자
}