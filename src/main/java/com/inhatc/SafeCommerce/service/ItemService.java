package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.ItemImage;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    // 사용자 ID로 User 조회
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // 상품과 이미지 데이터를 저장
    @Transactional
    public Item saveItem(Item item, List<MultipartFile> images, Long userId) throws IOException {
        // User 정보를 Item에 설정
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(item::setUser); // 사용자가 존재하는 경우 Item에 설정

        // MultipartFile 리스트를 ItemImage 객체로 변환하여 저장
        for (MultipartFile image : images) {
            ItemImage itemImage = new ItemImage();
            itemImage.setImageData(image.getBytes()); // 이미지 데이터를 byte[]로 변환
            itemImage.setItem(item); // 현재 Item과의 관계 설정
            item.getItemImages().add(itemImage); // Item에 ItemImage 추가
        }
        return itemRepository.save(item); // Item과 이미지를 데이터베이스에 저장
    }

    // 모든 아이템 목록과 이미지의 Base64 인코딩
    public List<Item> getAllItemsWithImages() {
        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            for (ItemImage image : item.getItemImages()) {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image); // ItemImage에 Base64 이미지 필드를 추가
            }
        }
        return items;
    }

    // 아이템 상세 정보 조회 및 이미지의 Base64 인코딩
    public Optional<Item> getItemDetail(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            Item foundItem = item.get();
            for (ItemImage image : foundItem.getItemImages()) {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            }
        }
        return item;
    }
}