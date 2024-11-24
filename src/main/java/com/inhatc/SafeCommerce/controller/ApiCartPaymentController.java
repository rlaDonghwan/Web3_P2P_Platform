package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.service.CartPaymentService;
import com.inhatc.SafeCommerce.util.DataParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class ApiCartPaymentController {

    @Autowired
    private CartPaymentService cartPaymentService;

    /**
     * 장바구니 상품 정보 확인 API
     *
     * @param cartId 장바구니 ID
     * @return 장바구니 세부 정보
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getCartDetails(@RequestParam Long cartId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> cartData = cartPaymentService.getCartDetails(cartId);
            response.put("cartData", cartData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "장바구니 정보 로드 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 결제 처리 API
     *
     * @param requestData 결제 정보
     * @return 결제 처리 결과
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processCartPayment(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!requestData.containsKey("cartId") || !requestData.containsKey("buyerName") ||
                    !requestData.containsKey("buyerAddress") || !requestData.containsKey("buyerContact") ||
                    !requestData.containsKey("transactionHash")) {
                throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
            }

            Long cartId = DataParserUtil.parseLongValue(requestData.get("cartId"));
            String buyerName = (String) requestData.get("buyerName");
            String buyerAddress = (String) requestData.get("buyerAddress");
            String buyerContact = (String) requestData.get("buyerContact");
            String transactionHash = (String) requestData.get("transactionHash");

            String result = cartPaymentService.processCartPayment(cartId, buyerName, buyerAddress, buyerContact, transactionHash);

            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "장바구니 결제 처리 중 오류 발생");
            response.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}