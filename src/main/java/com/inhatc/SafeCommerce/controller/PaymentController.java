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
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/checkQuantity")
    public ResponseEntity<Map<String, Object>> checkQuantity(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long itemId = Long.valueOf(requestData.get("itemId").toString());
            int requestedQuantity = Integer.parseInt(requestData.get("quantity").toString());

            String result = paymentService.checkAndReserveQuantity(itemId, requestedQuantity);

            response.put("message", result);
            if (result.equals("상품 수량이 부족합니다.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

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
            Long userId = Long.valueOf(requestData.get("userId").toString());
            Long itemId = Long.valueOf(requestData.get("itemId").toString());
            int quantity = Integer.parseInt(requestData.get("quantity").toString());
            String buyerName = requestData.get("buyerName").toString();
            String buyerAddress = requestData.get("buyerAddress").toString();
            String buyerContact = requestData.get("buyerContact").toString();

            String result = paymentService.processOrder(userId, itemId, quantity, buyerName, buyerAddress, buyerContact);

            if (result.equals("상품 수량이 부족합니다.")) {
                response.put("message", result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response.put("message", result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "주문 처리 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private Long parseLongValue(Object value) { // Long 타입 변환 메서드
        if (value instanceof Number) { // 입력값이 숫자인 경우
            return ((Number) value).longValue(); // Long 값으로 변환
        } else if (value instanceof String) { // 입력값이 문자열인 경우
            return Long.valueOf((String) value); // 문자열을 Long 값으로 변환
        } else { // 그 외의 타입인 경우
            throw new IllegalArgumentException("잘못된 데이터 타입입니다."); // 예외 발생
        }
    }

    private int parseIntValue(Object value) { // int 타입 변환 메서드
        if (value instanceof Number) { // 입력값이 숫자인 경우
            return ((Number) value).intValue(); // int 값으로 변환
        } else if (value instanceof String) { // 입력값이 문자열인 경우
            return Integer.parseInt((String) value); // 문자열을 int 값으로 변환
        } else { // 그 외의 타입인 경우
            throw new IllegalArgumentException("잘못된 데이터 타입입니다."); // 예외 발생
        }
    }
}