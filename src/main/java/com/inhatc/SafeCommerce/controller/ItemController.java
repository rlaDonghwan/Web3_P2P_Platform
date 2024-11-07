package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.ItemImage;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/addItem")
    public String addItemForm() {
        return "item_add";
    }

    @PostMapping("/addItem")
    public String addItem(@ModelAttribute Item item,
                          @RequestParam("imageData") MultipartFile[] imageData,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            return "redirect:/login";
        }
        item.setUser(user);

        List<ItemImage> images = new ArrayList<>();
        try {
            for (MultipartFile file : imageData) {
                if (!file.isEmpty()) {
                    ItemImage image = new ItemImage();
                    image.setImageData(file.getBytes());
                    image.setItem(item); // 이미지와 아이템 연결
                    images.add(image);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다.");
            return "redirect:/addItem";
        }

        item.setImages(images);
        itemRepository.save(item); // Item 엔티티를 저장

        redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 등록되었습니다.");
        return "redirect:/home"; // 상품 등록 후 홈 페이지로 리다이렉트
    }
}




