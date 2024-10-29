package com.inhatc.SafeCommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class MetaMaskAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_ADDRESS = "address";
    public static final String SPRING_SECURITY_FORM_SIGNATURE = "signature";

    public MetaMaskAuthenticationFilter() {
        super(new AntPathRequestMatcher("/auth/login", "POST")); // "/auth/login" URL을 통해 필터를 활성화
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String address = obtainAddress(request);
        String signature = obtainSignature(request);

        if (address == null) {
            address = "";
        }
        if (signature == null) {
            signature = "";
        }

        // 인증 요청을 생성하고 AuthenticationManager에 전달
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(address, signature);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private String obtainAddress(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_ADDRESS);
    }

    private String obtainSignature(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_SIGNATURE);
    }
}