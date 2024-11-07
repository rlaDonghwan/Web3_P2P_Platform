package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.ItemImage;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveItem(Item item, MultipartFile[] imageData, Long userId) throws IOException {
        // User를 찾아서 Item에 설정합니다.
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        item.setUser(user);

        // 이미지 데이터를 저장합니다.
        List<ItemImage> images = new ArrayList<>();
        for (MultipartFile file : imageData) {
            if (!file.isEmpty()) {
                ItemImage image = new ItemImage();
                image.setImageData(file.getBytes());
                image.setItem(item); // 이미지와 아이템 연결
                images.add(image);
            }
        }

        item.setImages(images); // Item에 이미지 리스트 설정
        itemRepository.save(item); // Item 엔티티를 저장
    }
}