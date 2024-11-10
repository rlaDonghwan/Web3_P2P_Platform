package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // 필요한 경우 추가
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkQuantity")
    public ResponseEntity<Map<String, Object>> checkQuantity(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String itemIdStr = (String) requestData.get("itemId");
            if (itemIdStr == null || itemIdStr.isEmpty()) {
                throw new IllegalArgumentException("itemId가 필요합니다.");
            }
            Long itemId = Long.valueOf(itemIdStr);
            String result = paymentService.checkAndReserveQuantity(itemId);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "수량 확인 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf((String) requestData.get("userId"));
            Long itemId = Long.valueOf((String) requestData.get("itemId"));
            int quantity = Integer.parseInt((String) requestData.get("quantity"));
            String buyerName = (String) requestData.get("buyerName");
            String buyerAddress = (String) requestData.get("buyerAddress");
            String buyerContact = (String) requestData.get("buyerContact");

            String result = paymentService.processOrder(userId, itemId, quantity, buyerName, buyerAddress, buyerContact);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "주문 처리 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}