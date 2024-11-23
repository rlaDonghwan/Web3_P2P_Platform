package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 상품 수량 확인 및 예약 API
     */
    @PostMapping("/checkQuantity")
    public ResponseEntity<Map<String, Object>> checkQuantity(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long itemId = parseLongValue(requestData.get("itemId"));
            int requestedQuantity = parseIntValue(requestData.get("quantity"));

            String result = paymentService.checkAndReserveQuantity(itemId, requestedQuantity);

            response.put("message", result);
            if ("상품 수량이 부족합니다.".equals(result)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "수량 확인 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 결제 처리 API
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 요청 데이터 유효성 검사
            if (!requestData.containsKey("userId") || !requestData.containsKey("items") ||
                    !requestData.containsKey("buyerName") || !requestData.containsKey("buyerAddress") ||
                    !requestData.containsKey("buyerContact") || !requestData.containsKey("transactionHash")) {
                throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
            }

            Long userId = parseLongValue(requestData.get("userId"));
            List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
            String buyerName = (String) requestData.get("buyerName");
            String buyerAddress = (String) requestData.get("buyerAddress");
            String buyerContact = (String) requestData.get("buyerContact");
            String transactionHash = (String) requestData.get("transactionHash");

            // PaymentService의 processOrder 호출
            String result = paymentService.processOrder(userId, items, buyerName, buyerAddress, buyerContact, transactionHash);

            response.put("message", result);
            return ResponseEntity.ok(response);

        } catch (ClassCastException | NumberFormatException e) {
            response.put("error", "데이터 형식 오류");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (IllegalArgumentException e) {
            response.put("error", "유효성 검사 실패");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("error", "결제 처리 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Long 변환 메서드
     */
    private Long parseLongValue(Object value) {
        return value instanceof Number ? ((Number) value).longValue()
                : value instanceof String ? Long.valueOf((String) value)
                : throwIllegalArgument("Long");
    }

    /**
     * int 변환 메서드
     */
    private int parseIntValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue()
                : value instanceof String ? Integer.parseInt((String) value)
                : throwIllegalArgument("int");
    }

    private <T> T throwIllegalArgument(String expectedType) {
        throw new IllegalArgumentException("잘못된 데이터 타입입니다. " + expectedType + "가 필요합니다.");
    }
}