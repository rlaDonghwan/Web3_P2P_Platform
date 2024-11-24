package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.service.SinglePaymentService;
import com.inhatc.SafeCommerce.util.DataParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiSinglePaymentController {

    @Autowired
    private SinglePaymentService singlePaymentService;

    /**
     * 단일 상품 수량 확인 및 예약 API
     *
     * @param requestData 상품 ID와 수량 정보
     * @return 수량 확인 결과
     */
    @PostMapping("/checkQuantity")
    public ResponseEntity<Map<String, Object>> checkQuantity(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long itemId = DataParserUtil.parseLongValue(requestData.get("itemId"));
            int requestedQuantity = DataParserUtil.parseIntValue(requestData.get("quantity"));

            String result = singlePaymentService.checkAndReserveQuantity(itemId, requestedQuantity);

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
     * 단일 상품 결제 처리 API
     *
     * @param requestData 결제 정보
     * @return 결제 처리 결과
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processSinglePayment(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!requestData.containsKey("userId") || !requestData.containsKey("items") ||
                    !requestData.containsKey("buyerName") || !requestData.containsKey("buyerAddress") ||
                    !requestData.containsKey("buyerContact") || !requestData.containsKey("transactionHash")) {
                throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
            }

            Long userId = DataParserUtil.parseLongValue(requestData.get("userId"));
            List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
            String buyerName = (String) requestData.get("buyerName");
            String buyerAddress = (String) requestData.get("buyerAddress");
            String buyerContact = (String) requestData.get("buyerContact");
            String transactionHash = (String) requestData.get("transactionHash");

            String result = singlePaymentService.processOrder(userId, items, buyerName, buyerAddress, buyerContact, transactionHash);

            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "결제 처리 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}