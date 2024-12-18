package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    // 계정 ID로 사용자를 찾고, 없으면 새 사용자 생성
    public UserDTO findOrCreateUser(String accountId) {
        return userRepository.findByAccountId(accountId)
                .map(this::convertToDTO)
                .orElseGet(() -> createUser(accountId));
    }
    //------------------------------------------------------------------------------------------------------------------


    // 새 사용자 생성 및 저장
    private UserDTO createUser(String accountId) {
        User newUser = new User();
        newUser.setAccountId(accountId);
        newUser.changeNonce(); // 새 nonce 생성

        try {
            userRepository.save(newUser); // DB에 저장
        } catch (Exception e) {
            // 예외 처리
            throw new RuntimeException("사용자 저장 중 오류가 발생했습니다.", e);
        }

        return convertToDTO(newUser);
    }
    //------------------------------------------------------------------------------------------------------------------


    // User 엔티티 -> UserDTO로 변환
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getAccountId(), user.getNonce());
    }
    //------------------------------------------------------------------------------------------------------------------

    //User 아이디 찾는 메서드
    public UserDTO getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(this::convertToDTO).orElse(null);
    }
    //------------------------------------------------------------------------------------------------------------------

    //글쓴이 userID 조회
    public Long findAuthorIdByItemId(Long itemId) {
        // itemId로 글쓴이 userId 조회
        return itemRepository.findAuthorIdByItemId(itemId);
    }
    //------------------------------------------------------------------------------------------------------------------
}