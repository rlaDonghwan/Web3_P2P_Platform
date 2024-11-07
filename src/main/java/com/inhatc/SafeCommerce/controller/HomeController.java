package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ItemRepository itemRepository;

    // 홈 화면
    @GetMapping("/home")
    public String home(Model model) {
        List<Item> items = itemRepository.findAll().stream().map(item -> {
            // 각 Item의 이미지들을 Base64로 인코딩
            item.getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image); // ItemImage에 Base64 이미지 필드를 추가
            });
            return item;
        }).collect(Collectors.toList());

        model.addAttribute("items", items);
        return "home";
    }
}