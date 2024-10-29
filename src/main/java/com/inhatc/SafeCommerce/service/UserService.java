package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // 계정 ID로 사용자를 찾고, 없으면 새 사용자 생성
    public UserDTO findOrCreateUser(String accountId) {
        Optional<User> userOptional = userRepository.findByAccountId(accountId);
        if (userOptional.isPresent()) {
            return convertToDTO(userOptional.get());
        } else {
            User newUser = new User();
            newUser.setAccountId(accountId);
            newUser.changeNonce(); // 새 nonce 생성
            userRepository.save(newUser); // DB에 저장
            return convertToDTO(newUser);
        }
    }

    // User 엔티티 -> UserDTO로 변환
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getAccountId(), user.getNonce());
    }
}