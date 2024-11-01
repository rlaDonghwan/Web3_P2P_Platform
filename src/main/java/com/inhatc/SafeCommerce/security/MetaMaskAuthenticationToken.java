package com.inhatc.SafeCommerce.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

// MetaMask 인증을 위한 커스텀 인증 토큰 클래스
public class MetaMaskAuthenticationToken extends UsernamePasswordAuthenticationToken {

    // 생성자: principal(사용자 식별 정보)와 credentials(서명)로 토큰 생성
    public MetaMaskAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}