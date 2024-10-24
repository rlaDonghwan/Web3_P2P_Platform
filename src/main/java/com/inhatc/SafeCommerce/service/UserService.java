package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.domain.User;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserDTO findOrCreateUser(String accountId) {
        Optional<User> userOptional = userRepository.findByAccountId(accountId);
        if (userOptional.isPresent()) {
            return convertToDTO(userOptional.get());  // User를 UserDTO로 변환 후 반환
        } else {
            User newUser = new User();
            newUser.setAccountId(accountId);
            userRepository.save(newUser);
            return convertToDTO(newUser);  // 새로 생성된 User를 UserDTO로 변환 후 반환
        }
    }

    // User 엔티티를 UserDTO로 변환
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setAccountId(user.getAccountId());
        return userDTO;
    }
}
