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
            return "error";
        }
        return "pay_detail";
    }

    @GetMapping("/sendEther")
    public String sendEtherPage(
            @RequestParam String ethPrice,
            @RequestParam String buyerName,
            @RequestParam String buyerAddress,
            @RequestParam String buyerContact,
            Model model) {

        // 전달받은 정보를 모델에 추가하여 sendEther 템플릿으로 전달
        model.addAttribute("ethPrice", ethPrice);
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("buyerAddress", buyerAddress);
        model.addAttribute("buyerContact", buyerContact);

        return "sendEther";
    }
}