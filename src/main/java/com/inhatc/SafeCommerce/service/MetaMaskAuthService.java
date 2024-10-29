package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.dto.AuthenticationRequest;
import com.inhatc.SafeCommerce.dto.AuthenticationResponse;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.UserRepository;
import com.inhatc.SafeCommerce.security.MetaMaskAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MetaMaskAuthService {

    @Autowired
    private MetaMaskAuthenticationProvider authProvider;

    @Autowired
    private UserRepository userRepository;

    // 수정된 authenticate 메서드
    public ResponseEntity<?> authenticate(AuthenticationRequest authRequest) {
        Optional<User> userOptional = userRepository.findByAccountId(authRequest.getAddress());
        User user = userOptional.orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        boolean isSignatureValid = authProvider.isSignatureValid(authRequest.getSignature(), user.getAccountId(), user.getNonce());
        if (!isSignatureValid) {
            return ResponseEntity.status(401).body("Invalid signature provided");
        }

        // 인증 성공 시 nonce 갱신
        user.changeNonce();
        userRepository.save(user);

        return ResponseEntity.ok(new AuthenticationResponse("Authenticated", user.getAccountId()));
    }
}