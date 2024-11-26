package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private Long getLoggedInUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @GetMapping
    public String cartForm(HttpSession session, Model model) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Cart> cartOptional = cartService.getCartByUserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            model.addAttribute("cartItems", cart.getCartItems());
            model.addAttribute("totalPrice", cart.getCartItems().stream()
                    .mapToInt(cartItem -> cartItem.getPrice() * cartItem.getQuantity()).sum());
        } else {
            model.addAttribute("cartItems", new ArrayList<CartItem>());
            model.addAttribute("totalPrice", 0);
        }

        return "cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(@RequestParam Long itemId, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String message = cartService.addItemToCart(userId, itemId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/updateQuantity")
    @ResponseBody
    public ResponseEntity<String> updateQuantity(@RequestBody Map<String, String> data) {
        String cartItemIdStr = data.get("cartItemId");
        String quantityStr = data.get("quantity");

        if (cartItemIdStr == null || quantityStr == null || cartItemIdStr.isEmpty() || quantityStr.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 요청입니다.");
        }

        try {
            Long cartItemId = Long.parseLong(cartItemIdStr);
            int quantity = Integer.parseInt(quantityStr);
            String message = cartService.updateCartItemQuantity(cartItemId, quantity);

            if ("재고 수량을 초과하는 요청입니다.".equals(message)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }

            return ResponseEntity.ok("장바구니 상품 수량이 업데이트되었습니다.");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("형식 오류가 발생했습니다.");
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Void> deleteCartItem(@RequestParam Long cartItemId) {
        cartService.deleteCartItemById(cartItemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment")
    public String cartPaymentPage(HttpSession session, Model model) {
        Long userId = getLoggedInUserId(session);

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Cart> cartOptional = cartService.getCartByUserId(userId);

        if (cartOptional.isEmpty() || cartOptional.get().getCartItems().isEmpty()) {
            model.addAttribute("sellerItemsJson", "[]");
            model.addAttribute("totalPrice", 0);
            return "cart_payment";
        }

        Cart cart = cartOptional.get();

        // CartService에서 반환된 데이터를 JSON으로 변환하여 모델에 전달
        String sellerItemsJson = cartService.getSellerItemsAsJson(cart.getCartItems());

        int totalPrice = cart.getCartItems().stream()
                .mapToInt(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("sellerItemsJson", sellerItemsJson);
        model.addAttribute("totalPrice", totalPrice);

        return "cart_payment";
    }

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            System.out.println("유저 ID가 세션에 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        System.out.println("세션에서 가져온 userId: " + userId);

        Optional<Cart> cartOptional = cartService.getCartByUserId(userId);
        if (cartOptional.isEmpty() || cartOptional.get().getCartItems().isEmpty()) {
            System.out.println("장바구니에 상품이 없습니다.");
            return ResponseEntity.ok(Map.of("success", false, "message", "장바구니가 비어 있습니다."));
        }

        Cart cart = cartOptional.get();

        // 세션에 cartId 저장
        session.setAttribute("cartId", cart.getId());

        int totalPrice = cart.getCartItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity()).sum();

        System.out.println("결제 준비 완료. Total Price: " + totalPrice);
        session.setAttribute("totalPrice", totalPrice);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "결제 준비 완료",
                "cartId", cart.getId(),
                "totalPrice", totalPrice
        ));
    }
}