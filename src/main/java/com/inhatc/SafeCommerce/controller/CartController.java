package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.CartRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    //장바구니 화면 로드 메서드
    @GetMapping
    public String cartForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // 로그인이 필요할 경우 로그인 페이지로 리다이렉트
        }

        // 사용자 ID로 장바구니 정보를 가져오기
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
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

        return "cart"; // cart.html 템플릿을 렌더링
    }
    //------------------------------------------------------------------------------------------------------------------

    // 장바구니 추가 메서드
    @PostMapping("/add")
    public String addToCart(@RequestParam Long itemId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "로그인이 필요합니다.";
        }

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (userOptional.isPresent() && itemOptional.isPresent()) {
            User user = userOptional.get();
            Item item = itemOptional.get();

            // 사용자의 장바구니 가져오기 또는 생성
            Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });

            // CartItem 생성 및 장바구니에 추가
            CartItem cartItem = new CartItem();
            cartItem.setItem(item);
            cartItem.setQuantity(1);
            cartItem.setPrice(item.getPrice());
            cart.addCartItem(cartItem);

            cartRepository.save(cart);

            return "장바구니에 추가되었습니다.";
        } else {
            return "상품이나 사용자가 존재하지 않습니다.";
        }
    }
}