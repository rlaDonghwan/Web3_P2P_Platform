package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class NonceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/nonce/{address}")
    public ResponseEntity<Integer> getNonce(@PathVariable String address) {
        Optional<User> userOptional = userRepository.findByAccountId(address);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // 사용자 생성
            user = new User();
            user.setAccountId(address);
            user.changeNonce(); // 새로운 nonce 설정
            userRepository.save(user);
        }

        return ResponseEntity.ok(user.getNonce());
    }
}