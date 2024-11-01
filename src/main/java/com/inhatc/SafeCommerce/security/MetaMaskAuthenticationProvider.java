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

    // 인증을 처리하는 메서드
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String address = (String) authentication.getPrincipal(); // 인증 요청의 주소
        String signature = (String) authentication.getCredentials(); // 인증 요청의 서명
        Integer nonce = 12345; // 예시로 사용된 nonce, 실제 사용 시에는 데이터베이스 등에서 불러와야 함

        // 서명이 유효한지 검사
        if (isSignatureValid(signature, address, nonce)) {
            // 유효한 서명일 경우 인증된 사용자 생성
            UserDetails userDetails = User.withUsername(address).password("").authorities("ROLE_USER").build();
            return new MetaMaskAuthenticationToken(userDetails, signature); // 인증 토큰 반환
        } else {
            // 서명이 유효하지 않은 경우 예외 발생
            throw new AuthenticationException("Invalid MetaMask signature") {};
        }
    }

    // 이 Provider가 MetaMaskAuthenticationToken을 지원하는지 여부를 반환
    @Override
    public boolean supports(Class<?> authentication) {
        return MetaMaskAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // 서명이 유효한지 검사하는 메서드
    public boolean isSignatureValid(String signature, String address, Integer nonce) {
        String message = String.format("Wallet address: %s \n Nonce: %d", address, nonce);

        try {
            // 서명을 바이트 배열로 변환하여 v, r, s 부분을 분리
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            byte v = signatureBytes[64]; // 서명 값의 v 부분
            if (v < 27) {
                v += 27; // EIP-155 호환성을 위해 v 값 수정
            }
            byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32); // 서명 값의 r 부분
            byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64); // 서명 값의 s 부분

            Sign.SignatureData data = new Sign.SignatureData(v, r, s); // 서명 데이터 객체 생성
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), data); // 복구된 공개키로 메시지 확인

            // 복구된 주소 생성
            String recoveredAddress = "0x" + Keys.getAddress(publicKey);
            return address.equalsIgnoreCase(recoveredAddress); // 복구된 주소와 제공된 주소가 일치하는지 확인
        } catch (SignatureException e) {
            System.err.println("SignatureException: " + e.getMessage());
            return false; // 서명 예외 발생 시 유효하지 않음으로 반환
        } catch (Exception e) {
            System.err.println("Error during signature validation: " + e.getMessage());
            return false; // 기타 예외 발생 시 유효하지 않음으로 반환
        }
    }
}