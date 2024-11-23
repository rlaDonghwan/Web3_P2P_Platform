package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.PaymentRequest;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.service.PayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Optional;

@Controller
public class PayController {

    @Autowired
    private PayService payService;

    @Value("${CONTRACT_ADDRESS}")
    private String contractAddress;

    @GetMapping("/pay/{itemId}")
    public String payPage(@PathVariable Long itemId, Model model) {
        Optional<Item> itemOptional = payService.getItemById(itemId);
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

    @GetMapping("/cart/pay")
    public String cartPayPage(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId == null) {
            return "redirect:/cart";
        }

        Map<String, Object> cartData = payService.prepareCartPaymentData(cartId);

        model.addAttribute("sellerItemsMap", cartData.get("sellerItemsMap"));
        model.addAttribute("sellerTotals", cartData.get("sellerTotals"));
        model.addAttribute("orderId", cartData.get("orderId"));
        model.addAttribute("contractAddress", contractAddress);

        return "cart_payment";
    }

    @PostMapping("/payment/submit")
    public ResponseEntity<String> submitPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            payService.updateOrderWithPaymentDetails(paymentRequest);
            return ResponseEntity.ok("Payment processed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}