package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkQuantity")
    public String checkQuantity(@RequestBody Map<String, Object> requestData) {
        Long itemId = Long.valueOf((String) requestData.get("itemId"));
        int quantity = Integer.parseInt((String) requestData.get("quantity"));
        return paymentService.checkAndReserveQuantity(itemId, quantity);
    }

    @PostMapping("/process")
    public String processPayment(
            @RequestParam Long userId,
            @RequestParam Long itemId,
            @RequestParam int quantity,
            @RequestParam String buyerName,
            @RequestParam String buyerAddress,
            @RequestParam String buyerContact
    ) {
        return paymentService.processOrder(userId, itemId, quantity, buyerName, buyerAddress, buyerContact);
    }
}