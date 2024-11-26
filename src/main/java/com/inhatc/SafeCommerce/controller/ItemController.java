package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.service.ItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

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
            return "redirect:/login";
        }

        Optional<User> userOptional = itemService.findUserById(userId);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            return "redirect:/login";
        }
        item.setUser(userOptional.get());

        try {
            itemService.saveItem(item, imageData);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다.");
            return "redirect:/addItem";
        }

        redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 등록되었습니다.");
        return "redirect:/home";
    }

    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        Optional<Item> itemOptional = itemService.findItemById(itemId);
        if (itemOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 찾을 수 없습니다.");
            return "redirect:/home";
        }

        Item item = itemOptional.get();
        if (!item.getUser().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 삭제할 권한이 없습니다.");
            return "redirect:/home";
        }

        itemService.deleteItemById(itemId);
        redirectAttributes.addFlashAttribute("successMessage", "상품이 삭제되었습니다.");
        return "redirect:/home";
    }

    @GetMapping("/editItem/{itemId}")
    public String editItemForm(@PathVariable Long itemId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Optional<Item> itemOptional = itemService.findItemById(itemId);
        if (itemOptional.isPresent() && itemOptional.get().getUser().getId().equals(userId)) {
            model.addAttribute("item", itemOptional.get());
            return "item_edit";
        }
        return "redirect:/home";
    }

    @PostMapping("/editItem/{itemId}")
    public String editItem(@PathVariable Long itemId,
                           @ModelAttribute Item updatedItem,
                           @RequestParam("imageData") MultipartFile[] imageData,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        Optional<Item> itemOptional = itemService.findItemById(itemId);
        if (itemOptional.isPresent() && itemOptional.get().getUser().getId().equals(userId)) {
            try {
                itemService.updateItem(itemId, updatedItem, imageData);
                redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 수정되었습니다.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다.");
            }
        }
        return "redirect:/home";
    }
}