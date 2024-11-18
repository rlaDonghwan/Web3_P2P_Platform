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

    //------------------------------------------------------------------------------------------------------------------

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

    //------------------------------------------------------------------------------------------------------------------

    @PostMapping("/updateQuantity")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody Map<String, String> data) {
        String cartItemIdStr = data.get("cartItemId");
        String quantityStr = data.get("quantity");

        if (cartItemIdStr == null || cartItemIdStr.isEmpty() || quantityStr == null || quantityStr.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Long cartItemId = Long.parseLong(cartItemIdStr);
            int quantity = Integer.parseInt(quantityStr);
            cartService.updateCartItemQuantity(cartItemId, quantity);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    //------------------------------------------------------------------------------------------------------------------

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Void> deleteCartItem(@RequestParam Long cartItemId) {
        cartService.deleteCartItemById(cartItemId);
        return ResponseEntity.ok().build();
    }

    //------------------------------------------------------------------------------------------------------------------

    @PostMapping("/checkout")
    @ResponseBody
    public ResponseEntity<String> checkout(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        session.setAttribute("checkoutItems", items); // 선택한 상품 정보를 세션에 저장

        return ResponseEntity.ok("결제 준비 완료");
    }
}