package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    // 상품 추가 폼으로 이동
    @GetMapping("/addItem")
    public String addItemForm() {
        return "item_add"; // 상품 추가 폼을 반환
    }

    // 상품 추가 메서드
    @PostMapping("/addItem")
    public String addItem(@ModelAttribute Item item,
                          @RequestParam("images") List<MultipartFile> images,
                          @RequestParam("userId") Long userId,
                          Model model) {
        try {
            itemService.saveItem(item, images, userId); // Item과 이미지를 저장
            return "redirect:/home"; // 성공 페이지로 리다이렉트
        } catch (IOException e) {
            logger.error("상품 등록에 실패했습니다.", e);
            model.addAttribute("errorMessage", "상품 등록에 실패했습니다.");
            return "item_add"; // 오류가 발생하면 다시 폼 페이지로 이동
        }
    }
}