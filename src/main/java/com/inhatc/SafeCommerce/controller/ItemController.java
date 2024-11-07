package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.service.ItemService;
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
import java.util.Optional;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    // 상품 추가 폼으로 이동
    @GetMapping("/addItem")
    public String addItemForm() {
        return "item_add";
    }

    // 상품 추가 메서드
    @PostMapping("/addItem")
    public String addItem(@ModelAttribute Item item,
                          @RequestParam("imageData") MultipartFile[] imageData,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        // 세션에서 로그인된 사용자 ID를 가져옵니다.
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        // User를 찾아서 Item에 설정합니다.
        Optional<User> userOptional = itemService.findUserById(userId);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            return "redirect:/login";
        }
        item.setUser(userOptional.get());

        // 이미지 데이터를 저장합니다.
        try {
            itemService.saveItem(item, imageData); // 서비스 계층을 통해 아이템과 이미지를 저장
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다.");
            return "redirect:/addItem";
        }

        redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 등록되었습니다.");
        return "redirect:/home"; // 상품 등록 후 홈 페이지로 리다이렉트
    }
    //------------------------------------------------------------------------------------------------------------------
}
