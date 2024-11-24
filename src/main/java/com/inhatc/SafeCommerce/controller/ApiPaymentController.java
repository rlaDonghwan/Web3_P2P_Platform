//package com.inhatc.SafeCommerce.controller;
//
//import com.inhatc.SafeCommerce.service.ApiPaymentService;
//import com.inhatc.SafeCommerce.util.DataParserUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//public class ApiPaymentController {
//
//    @Autowired
//    private ApiPaymentService apiPaymentService;
//
//    /**
//     * 상품 수량 확인 및 예약 API
//     *
//     * @param requestData 상품 ID와 수량 정보
//     * @return 수량 확인 결과
//     */
//    @PostMapping("/checkQuantity")
//    public ResponseEntity<Map<String, Object>> checkQuantity(@RequestBody Map<String, Object> requestData) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // 요청 데이터 파싱
//            Long itemId = DataParserUtil.parseLongValue(requestData.get("itemId"));
//            int requestedQuantity = DataParserUtil.parseIntValue(requestData.get("quantity"));
//
//            // 서비스 호출
//            String result = apiPaymentService.checkAndReserveQuantity(itemId, requestedQuantity);
//
//            response.put("message", result);
//            if ("상품 수량이 부족합니다.".equals(result)) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("error", "수량 확인 중 오류 발생");
//            response.put("details", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//    /**
//     * 결제 처리 API
//     *
//     * @param requestData 결제 정보
//     * @return 결제 처리 결과
//     */
//    @PostMapping("/process")
//    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> requestData) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // 요청 데이터 유효성 검사
//            if (!requestData.containsKey("userId") || !requestData.containsKey("items") ||
//                    !requestData.containsKey("buyerName") || !requestData.containsKey("buyerAddress") ||
//                    !requestData.containsKey("buyerContact") || !requestData.containsKey("transactionHash")) {
//                throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
//            }
//
//            // 요청 데이터 파싱
//            Long userId = DataParserUtil.parseLongValue(requestData.get("userId"));
//            List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
//            String buyerName = (String) requestData.get("buyerName");
//            String buyerAddress = (String) requestData.get("buyerAddress");
//            String buyerContact = (String) requestData.get("buyerContact");
//            String transactionHash = (String) requestData.get("transactionHash");
//
//            // 서비스 호출
//            String result = apiPaymentService.processOrder(userId, items, buyerName, buyerAddress, buyerContact, transactionHash);
//
//            response.put("message", result);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("error", "결제 처리 중 오류 발생");
//            response.put("details", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     * 장바구니 상품 정보 확인 API
//     */
//    @GetMapping("/cart/details")
//    public ResponseEntity<Map<String, Object>> getCartDetails(@RequestParam Long cartId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Map<String, Object> cartData = apiPaymentService.getCartDetails(cartId);
//            response.put("cartData", cartData);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("error", "장바구니 정보 로드 중 오류 발생");
//            response.put("details", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    /**
//     * 장바구니 결제 처리 API
//     */
//    @PostMapping("/cart/process")
//    public ResponseEntity<Map<String, Object>> processCartPayment(@RequestBody Map<String, Object> requestData) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            if (!requestData.containsKey("cartId") || !requestData.containsKey("buyerName") ||
//                    !requestData.containsKey("buyerAddress") || !requestData.containsKey("buyerContact") ||
//                    !requestData.containsKey("transactionHash")) {
//                throw new IllegalArgumentException("필수 필드가 누락되었습니다.");
//            }
//
//            Long cartId = DataParserUtil.parseLongValue(requestData.get("cartId"));
//            String buyerName = (String) requestData.get("buyerName");
//            String buyerAddress = (String) requestData.get("buyerAddress");
//            String buyerContact = (String) requestData.get("buyerContact");
//            String transactionHash = (String) requestData.get("transactionHash");
//
//            String result = apiPaymentService.processCartPayment(cartId, buyerName, buyerAddress, buyerContact, transactionHash);
//
//            response.put("message", result);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("error", "장바구니 결제 처리 중 오류 발생");
//            response.put("details", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//
//}