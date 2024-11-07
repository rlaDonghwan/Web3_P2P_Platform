package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.service.ItemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/addItem")
    public String addItemForm() {
        return "add_item";
    }

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

        try {
            itemService.saveItem(item, imageData, userId); // Item과 이미지를 저장
            redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 등록되었습니다.");
            return "redirect:/home"; // 상품 등록 후 홈 페이지로 리다이렉트
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다.");
            return "redirect:/addItem"; // 오류가 발생하면 다시 폼 페이지로 이동
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login"; // 사용자 찾기 실패 시 로그인 페이지로 리다이렉트
        }
    }
}