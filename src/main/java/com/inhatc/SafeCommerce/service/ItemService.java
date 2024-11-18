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

    public void updateItem(Long itemId, Item updatedItem, MultipartFile[] imageData) throws IOException {
        Optional<Item> existingItemOpt = itemRepository.findById(itemId);
        if (existingItemOpt.isPresent()) {
            Item existingItem = existingItemOpt.get();

            // 기본 정보 업데이트
            existingItem.setItemName(updatedItem.getItemName());
            existingItem.setItemDescription(updatedItem.getItemDescription());
            existingItem.setPrice(updatedItem.getPrice());
            existingItem.setQuantity(updatedItem.getQuantity());

            // 이미지가 새로 업로드된 경우에만 추가
            if (imageData.length > 0 && !imageData[0].isEmpty()) {
                // 기존 이미지를 유지하기 위해 초기화하지 않음
                for (MultipartFile file : imageData) {
                    if (!file.isEmpty()) {
                        ItemImage newImage = new ItemImage();
                        newImage.setImageData(file.getBytes());
                        newImage.setItem(existingItem);
                        existingItem.getImages().add(newImage); // 기존 이미지 리스트에 추가
                    }
                }
            }

            itemRepository.save(existingItem);
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public void deleteItemById(Long itemId) {
        // 1. 장바구니에서 해당 상품과 연관된 항목 삭제
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
    //------------------------------------------------------------------------------------------------------------------
}

