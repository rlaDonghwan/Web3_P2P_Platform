package com.inhatc.SafeCommerce.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

@Component
public class MetaMaskAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String address = (String) authentication.getPrincipal();
        String signature = (String) authentication.getCredentials();
        Integer nonce = 12345; // 예시: 실제로는 데이터베이스에서 불러와야 함

        if (isSignatureValid(signature, address, nonce)) {
            // 서명이 유효한 경우 인증된 사용자 생성
            UserDetails userDetails = User.withUsername(address).password("").authorities("ROLE_USER").build();
            return new MetaMaskAuthenticationToken(userDetails, signature);
        } else {
            throw new AuthenticationException("Invalid MetaMask signature") {};
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MetaMaskAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public boolean isSignatureValid(String signature, String address, Integer nonce) {
        String message = String.format("Signing a message to login: %d", nonce);

        try {
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }
            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);

            Sign.SignatureData data = new Sign.SignatureData(v, r, s);
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), data);

            String recoveredAddress = "0x" + Keys.getAddress(publicKey);
            return address.equalsIgnoreCase(recoveredAddress);
        } catch (SignatureException e) {
            System.err.println("SignatureException: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error during signature validation: " + e.getMessage());
            return false;
        }
    }
}