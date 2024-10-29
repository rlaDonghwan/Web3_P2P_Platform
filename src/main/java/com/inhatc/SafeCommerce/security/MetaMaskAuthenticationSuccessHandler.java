package com.inhatc.SafeCommerce.security;

import com.inhatc.SafeCommerce.repository.UserRepository;
import com.inhatc.SafeCommerce.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

public class MetaMaskAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public MetaMaskAuthenticationSuccessHandler(UserRepository userRepository) {
        super("/home"); // 인증 성공 시 리다이렉트할 URL
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 인증 성공 후 사용자 정보 처리
        String accountId = authentication.getName(); // 사용자의 지갑 주소
        Optional<User> userOptional = userRepository.findByAccountId(accountId);
        userOptional.ifPresent(user -> {
            user.changeNonce(); // nonce 갱신
            userRepository.save(user); // 갱신된 사용자 정보 저장
        });

        super.onAuthenticationSuccess(request, response, authentication); // 기본 성공 처리
    }
}