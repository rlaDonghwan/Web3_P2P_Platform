package com.inhatc.SafeCommerce.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class MetaMaskAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public MetaMaskAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}