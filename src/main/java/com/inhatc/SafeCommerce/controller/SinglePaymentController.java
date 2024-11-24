package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.service.SinglePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class SinglePaymentController {

    @Autowired
    private SinglePaymentService singlePaymentService;

    /**
     * 단일 상품 결제 페이지
     */
    @GetMapping("/pay/{itemId}")
    public String payPage(@PathVariable Long itemId, Model model) {
        Optional<Item> itemOptional = singlePaymentService.getItemById(itemId);
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
        return "payment_checkout";
    }

    /**
     * 단일 상품 결제 정보 전송 페이지
     */
    @GetMapping("/sendEther")
    public String sendEtherPage(
            @RequestParam String ethPrice,
            @RequestParam String buyerName,
            @RequestParam String buyerAddress,
            @RequestParam String buyerContact,
            @RequestParam int quantity,
            @RequestParam Long itemId,
            Model model) {

        Optional<Item> itemOptional = singlePaymentService.getItemById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            singlePaymentService.createOrder(buyerName, buyerAddress, buyerContact, quantity, item);

            model.addAttribute("itemName", item.getItemName());
            model.addAttribute("itemId", itemId);
            model.addAttribute("itemPrice", item.getPrice());
            model.addAttribute("quantity", quantity);
            model.addAttribute("totalPrice", item.getPrice() * quantity);
        } else {
            return "error";
        }

        model.addAttribute("ethPrice", ethPrice);
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("buyerAddress", buyerAddress);
        model.addAttribute("buyerContact", buyerContact);

        return "payment_send";
    }
}