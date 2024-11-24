//package com.inhatc.SafeCommerce.controller;
//
//import com.inhatc.SafeCommerce.dto.PaymentRequest;
//import com.inhatc.SafeCommerce.model.Item;
//import com.inhatc.SafeCommerce.service.PayService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.util.Base64Utils;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.Optional;
//
//@Controller
//public class PayController {
//
//    @Autowired
//    private PayService payService;
//
//    @Value("${CONTRACT_ADDRESS}")
//    private String contractAddress;
//
//    //단일 상품 결제
//    @GetMapping("/pay/{itemId}")
//    public String payPage(@PathVariable Long itemId, Model model) {
//        Optional<Item> itemOptional = payService.getItemById(itemId);
//        if (itemOptional.isPresent()) {
//            Item item = itemOptional.get();
//            item.getImages().forEach(image -> {
//                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
//                image.setBase64Image(base64Image);
//            });
//            model.addAttribute("item", item);
//        } else {
//            return "error";
//        }
//        return "payment_checkout";
//    }
//    //payment_send/으로 넘어감
//    @GetMapping("/sendEther")
//    public String sendEtherPage(
//            @RequestParam String ethPrice,
//            @RequestParam String buyerName,
//            @RequestParam String buyerAddress,
//            @RequestParam String buyerContact,
//            @RequestParam int quantity,
//            @RequestParam Long itemId,
//            Model model) {
//
//        Optional<Item> itemOptional = payService.getItemById(itemId);
//        if (itemOptional.isPresent()) {
//            Item item = itemOptional.get();
//
//            // 주문 생성
//            payService.createOrder(buyerName, buyerAddress, buyerContact, quantity, item);
//
//            // 모델에 필요한 데이터 추가
//            model.addAttribute("itemName", item.getItemName());
//            model.addAttribute("itemId", itemId);
//            model.addAttribute("itemPrice", item.getPrice());
//            model.addAttribute("quantity", quantity);
//            model.addAttribute("totalPrice", item.getPrice() * quantity);
//        } else {
//            return "error";
//        }
//
//        model.addAttribute("ethPrice", ethPrice);
//        model.addAttribute("buyerName", buyerName);
//        model.addAttribute("buyerAddress", buyerAddress);
//        model.addAttribute("buyerContact", buyerContact);
//
//        return "payment_send";
//    }
//
//
//
//    @GetMapping("/cart/pay")
//    public String cartPayPage(HttpSession session, Model model) {
//        Long cartId = (Long) session.getAttribute("cartId");
//        if (cartId == null) {
//            return "redirect:/cart";
//        }
//
//        Map<String, Object> cartData = payService.prepareCartPaymentData(cartId);
//
//        model.addAttribute("sellerItemsMap", cartData.get("sellerItemsMap"));
//        model.addAttribute("sellerTotals", cartData.get("sellerTotals"));
//        model.addAttribute("sellerAddresses", cartData.get("sellerAddresses"));
//        model.addAttribute("sellerAmounts", cartData.get("sellerAmounts"));
//        model.addAttribute("totalPrice", cartData.get("totalPrice"));
//        model.addAttribute("contractAddress", contractAddress);
//
//        return "cart_payment";
//    }
//
//    @PostMapping("/payment/submit")
//    @ResponseBody
//    public ResponseEntity<String> submitPayment(@RequestBody PaymentRequest paymentRequest) {
//        try {
//            payService.updateOrderWithPaymentDetails(paymentRequest);
//            return ResponseEntity.ok("Payment processed successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//}