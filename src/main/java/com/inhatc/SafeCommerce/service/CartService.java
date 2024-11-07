package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.CartItemRepository;
import com.inhatc.SafeCommerce.repository.CartRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    // 사용자 ID로 장바구니 조회
    public Optional<Cart> getCartByUserId(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        cartOptional.ifPresent(cart -> cart.getCartItems().forEach(cartItem -> {
            // 아이템의 이미지가 있을 경우 Base64로 변환
            if (cartItem.getItem().getItemImages() != null && !cartItem.getItem().getItemImages().isEmpty()) {
                cartItem.getItem().getItemImages().forEach(image -> {
                    String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                    image.setBase64Image(base64Image);
                });
            }
        }));
        return cartOptional;
    }

    // 장바구니에 상품 추가
    public String addItemToCart(Long userId, Long itemId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (userOptional.isPresent() && itemOptional.isPresent()) {
            User user = userOptional.get();
            Item item = itemOptional.get();
            Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });

            // 장바구니에 이미 존재하는 아이템인지 확인
            Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getItem().getItemId().equals(itemId))
                    .findFirst();

            if (existingCartItem.isPresent()) {
                // 이미 존재하는 경우 수량 증가
                CartItem cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartItem.setPrice(item.getPrice() * cartItem.getQuantity()); // 가격 업데이트
                cartItemRepository.save(cartItem); // CartItem 저장
                return "장바구니에 같은 상품이 있어 수량이 증가했습니다.";
            } else {
                // 새로운 아이템인 경우 장바구니에 추가
                CartItem cartItem = new CartItem();
                cartItem.setItem(item);
                cartItem.setQuantity(1);
                cartItem.setPrice(item.getPrice()); // 초기 가격 설정
                cart.addCartItem(cartItem);
                cartItemRepository.save(cartItem); // CartItem 저장
                cartRepository.save(cart); // Cart 저장
                return "장바구니에 상품이 추가되었습니다.";
            }
        } else {
            return "상품이나 사용자가 존재하지 않습니다.";
        }
    }

    // 장바구니 아이템 수량 업데이트
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        cartItemRepository.findById(cartItemId).ifPresent(cartItem -> {
            cartItem.setQuantity(quantity);
            cartItem.setPrice(cartItem.getItem().getPrice() * quantity); // 가격 재계산
            cartRepository.save(cartItem.getCart());
        });
    }
}