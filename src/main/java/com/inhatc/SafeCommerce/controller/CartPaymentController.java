package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.PaymentRequest;
import com.inhatc.SafeCommerce.service.CartPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartPaymentController {

    @Autowired
    private CartPaymentService cartPaymentService;

    @Value("${CONTRACT_ADDRESS}")
    private String contractAddress;

    /**
     * 장바구니 결제 페이지
     */
    @GetMapping("/pay")
    public String cartPayPage(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");
        if (cartId == null) {
            return "redirect:/cart";
        }

        Map<String, Object> cartData = cartPaymentService.prepareCartPaymentData(cartId);

        model.addAttribute("sellerItemsMap", cartData.get("sellerItemsMap"));
        model.addAttribute("sellerTotals", cartData.get("sellerTotals"));
        model.addAttribute("sellerAddresses", cartData.get("sellerAddresses"));
        model.addAttribute("sellerAmounts", cartData.get("sellerAmounts"));
        model.addAttribute("totalPrice", cartData.get("totalPrice"));
        model.addAttribute("contractAddress", contractAddress);

        return "cart_payment";
    }

    /**
     * 결제 정보 업데이트
     */
    @PostMapping("/payment/submit")
    @ResponseBody
    public ResponseEntity<String> submitCartPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            cartPaymentService.updateOrderWithPaymentDetails(paymentRequest);
            return ResponseEntity.ok("Cart payment processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
