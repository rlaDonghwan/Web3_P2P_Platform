package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PayController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping("/pay/{itemId}")
    public String payPage(@PathVariable Long itemId, Model model) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            });
            model.addAttribute("item", item);
        } else {
            return "error"; // 에러 페이지로 이동
        }
        return "pay_detail"; // 결제 페이지로 이동
    }

    @GetMapping("/sendEther")
    public String sendEtherPage(
            @RequestParam String ethPrice,        // 이더리움 가격
            @RequestParam String buyerName,       // 구매자 이름
            @RequestParam String buyerAddress,    // 구매자 주소
            @RequestParam String buyerContact,    // 구매자 연락처
            @RequestParam int quantity,           // 구매 수량
            @RequestParam Long itemId,            // 상품 ID
            Model model) {

        // 상품 정보를 데이터베이스에서 가져옴
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            // 전달받은 정보를 모델에 추가
            model.addAttribute("itemName", item.getItemName());
            model.addAttribute("totalPrice", item.getPrice() * quantity); // 총 가격 계산
        }

        // 모델에 구매 정보 추가
        model.addAttribute("ethPrice", ethPrice);
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("buyerAddress", buyerAddress);
        model.addAttribute("buyerContact", buyerContact);
        model.addAttribute("quantity", quantity);

        return "sendEther"; // sendEther 템플릿으로 이동
    }
}