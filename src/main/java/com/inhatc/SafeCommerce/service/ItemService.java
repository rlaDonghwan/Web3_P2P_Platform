package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.ItemImage;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemImageRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemImageRepository itemImageRepository;

    // 사용자 ID로 User 조회
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
    //------------------------------------------------------------------------------------------------------------------

    // 특정 ID로 상품 조회
    public Optional<Item> findItemById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    // 상품과 이미지 데이터를 저장
    public void saveItem(Item item, MultipartFile[] imageData) throws IOException {
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
    //------------------------------------------------------------------------------------------------------------------

    // 상품 삭제 메서드
    public void deleteItemById(Long itemId) {
        // 1. 관련된 CartItem 삭제
        cartService.deleteCartItemsByItemId(itemId);

        // 2. 상품에 연결된 이미지를 먼저 삭제
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            List<ItemImage> images = item.getImages();
            if (images != null) {
                itemImageRepository.deleteAll(images);
            }
        }

        // 3. 상품 삭제
        itemRepository.deleteById(itemId);
    }

}

