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

        // 디버깅 로그 추가
        System.out.println("Received cartItemId: " + cartItemIdStr);
        System.out.println("Received quantity: " + quantityStr);

        if (cartItemIdStr == null || cartItemIdStr.isEmpty() || quantityStr == null || quantityStr.isEmpty()) {
            System.out.println("유효하지 않은 요청입니다: cartItemId 또는 quantity가 비어 있습니다.");
            return ResponseEntity.badRequest().build();  // 잘못된 요청 응답 반환
        }

        try {
            Long cartItemId = Long.parseLong(cartItemIdStr);
            int quantity = Integer.parseInt(quantityStr);
            cartService.updateCartItemQuantity(cartItemId, quantity);
        } catch (NumberFormatException e) {
            System.out.println("Number format exception 발생: " + e.getMessage());
            return ResponseEntity.badRequest().build();  // 형식이 잘못된 경우 잘못된 요청 응답 반환
        }

        return ResponseEntity.ok().build();
    }
    //------------------------------------------------------------------------------------------------------------------


}

