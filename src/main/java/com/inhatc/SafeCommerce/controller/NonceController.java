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

    /**
     * 클라이언트가 제공한 주소(accountId)로 nonce 값을 조회하고,
     * 해당 사용자가 없으면 새로 생성한 후 nonce 값을 반환합니다.
     *
     * @param address 클라이언트가 요청한 사용자 계정 주소
     * @return 해당 사용자에 대한 nonce 값을 ResponseEntity 형식으로 반환
     */
    @GetMapping("/nonce/{address}")
    public ResponseEntity<Integer> getNonce(@PathVariable String address) {
        // accountId로 사용자 조회
        Optional<User> userOptional = userRepository.findByAccountId(address);

        User user;
        if (userOptional.isPresent()) {
            // 기존 사용자가 있을 경우 해당 사용자 정보 사용
            user = userOptional.get();
        } else {
            // 기존 사용자가 없을 경우 새 사용자 생성
            user = new User();
            user.setAccountId(address);  // accountId 설정
            user.changeNonce();          // 새로운 nonce 생성 및 설정
            userRepository.save(user);   // 새 사용자 정보를 저장
        }

        // 해당 사용자의 nonce 값을 반환
        return ResponseEntity.ok(user.getNonce());
    }
    //------------------------------------------------------------------------------------------------------------------
}