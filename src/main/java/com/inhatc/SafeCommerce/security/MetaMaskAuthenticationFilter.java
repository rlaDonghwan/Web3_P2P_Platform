package com.inhatc.SafeCommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class MetaMaskAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    // 클라이언트 요청에서 MetaMask의 주소와 서명 값을 가져올 때 사용하는 파라미터 이름
    public static final String SPRING_SECURITY_FORM_ADDRESS = "address";
    public static final String SPRING_SECURITY_FORM_SIGNATURE = "signature";

    // "/auth/login" URL을 통해 이 필터가 활성화되도록 설정
    public MetaMaskAuthenticationFilter() {
        super(new AntPathRequestMatcher("/auth/login", "POST"));
    }

    // 인증을 시도하는 메서드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 요청에서 address와 signature 파라미터를 가져옴
        String address = obtainAddress(request);
        String signature = obtainSignature(request);

        // address나 signature가 null인 경우 빈 문자열로 처리
        if (address == null) {
            address = "";
        }
        if (signature == null) {
            signature = "";
        }

        // 인증 요청 객체를 생성하여 AuthenticationManager에 전달
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(address, signature);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // 요청에서 address 파라미터를 가져오는 메서드
    private String obtainAddress(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_ADDRESS);
    }

    // 요청에서 signature 파라미터를 가져오는 메서드
    private String obtainSignature(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_SIGNATURE);
    }
}