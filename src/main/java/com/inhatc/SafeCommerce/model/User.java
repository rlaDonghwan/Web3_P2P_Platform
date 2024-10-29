package com.inhatc.SafeCommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountId; // Ethereum 주소를 저장

    private int nonce; // MetaMask 인증에 사용될 nonce 값

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    // nonce 초기화 메서드 (초기 생성 시)
    @PrePersist
    public void initializeNonce() {
        this.nonce = generateRandomNonce();
    }

    // nonce 갱신 메서드 (로그인 시)
    public void changeNonce() {
        this.nonce = generateRandomNonce();
    }

    // 새로운 nonce 생성 메서드
    private Integer generateRandomNonce() {
        return (int) (Math.random() * 1_000_000); // 0 ~ 999,999 범위의 랜덤 숫자
    }
}