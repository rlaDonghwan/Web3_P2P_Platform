package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.ItemImage;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    //------------------------------------------------------------------------------------------------------------------


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


    // 모든 아이템 목록과 이미지의 Base64 인코딩
    public List<Item> getAllItemsWithImages() {
        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            for (ItemImage image : item.getImages()) {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image); // ItemImage에 Base64 이미지 필드를 추가
            }
        }
        return items;
    }
    //------------------------------------------------------------------------------------------------------------------


    // 아이템 상세 정보 조회 및 이미지의 Base64 인코딩
    public Optional<Item> getItemDetail(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            Item foundItem = item.get();
            for (ItemImage image : foundItem.getImages()) {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            }
        }
        return item;
    }
    //------------------------------------------------------------------------------------------------------------------

}