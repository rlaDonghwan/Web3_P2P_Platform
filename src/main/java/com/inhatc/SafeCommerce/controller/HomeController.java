package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ItemService itemService; // ItemService를 주입받음

    // 홈 화면
    @GetMapping("/home")
    public String home(Model model) {
        List<Item> items = itemService.getAllItemsWithImages(); // 서비스 메소드를 통해 아이템 리스트 가져오기
        model.addAttribute("items", items);
        return "home";
    }
    //------------------------------------------------------------------------------------------------------------------

    // 상품 클릭 시 상세 정보
    @GetMapping("/items/{itemId}")
    public String getItemDetail(@PathVariable Long itemId, Model model) {
        Optional<Item> item = itemService.getItemDetail(itemId); // 서비스 메소드를 통해 아이템 상세 정보 가져오기
        if (item.isPresent()) {
            model.addAttribute("item", item.get());
            return "item_detail";
        } else {
            return "redirect:/home";
        }
    }
    //------------------------------------------------------------------------------------------------------------------
}