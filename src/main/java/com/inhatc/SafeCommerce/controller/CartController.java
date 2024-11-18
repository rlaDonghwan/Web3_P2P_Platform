package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public String cartForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Cart> cartOptional = cartService.getCartByUserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            model.addAttribute("cartItems", cart.getCartItems());
            model.addAttribute("totalPrice", cart.getCartItems().stream().mapToInt(
                    cartItem -> cartItem.getPrice() * cartItem.getQuantity()).sum()
            );
        } else {
            model.addAttribute("cartItems", new ArrayList<CartItem>());
            model.addAttribute("totalPrice", 0);
        }

        return "cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(@RequestParam Long itemId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
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

        if (cartItemIdStr == null || cartItemIdStr.isEmpty() || quantityStr == null || quantityStr.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 요청입니다.");
        }

        try {
            Long cartItemId = Long.parseLong(cartItemIdStr);
            int quantity = Integer.parseInt(quantityStr);
            String message = cartService.updateCartItemQuantity(cartItemId, quantity);

            if (message.equals("재고 수량을 초과하는 요청입니다.")) {
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

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        boolean allItemsAvailable = true;

        for (Map<String, Object> item : items) {
            Long cartItemId = Long.valueOf(item.get("cartItemId").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());

            // 수량 확인
            Optional<CartItem> cartItemOptional = cartService.getCartItemById(cartItemId);
            if (cartItemOptional.isPresent()) {
                CartItem cartItem = cartItemOptional.get();
                if (cartItem.getItem().getQuantity() < quantity) {
                    allItemsAvailable = false;
                    return ResponseEntity.ok(Map.of("success", false, "message", "수량이 부족한 상품이 있습니다: " + cartItem.getItem().getItemName()));
                }
            } else {
                allItemsAvailable = false;
                return ResponseEntity.ok(Map.of("success", false, "message", "유효하지 않은 장바구니 상품입니다."));
            }
        }

        // 모든 상품의 수량이 충분하면 결제를 진행
        if (allItemsAvailable) {
            session.setAttribute("checkoutItems", items); // 세션에 선택된 상품 저장
            return ResponseEntity.ok(Map.of("success", true, "message", "결제 준비 완료"));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "결제 실패: 수량 부족"));
        }
    }
}