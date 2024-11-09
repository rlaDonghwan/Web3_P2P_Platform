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

    public Optional<Cart> getCartByUserId(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        cartOptional.ifPresent(cart -> cart.getCartItems().forEach(cartItem -> {
            if (cartItem.getItem().getImages() != null && !cartItem.getItem().getImages().isEmpty()) {
                cartItem.getItem().getImages().forEach(image -> {
                    String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                    image.setBase64Image(base64Image);
                });
            }
        }));
        return cartOptional;
    }
    //------------------------------------------------------------------------------------------------------------------

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

            Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getItem().getItemId().equals(itemId))
                    .findFirst();

            if (existingCartItem.isPresent()) {
                CartItem cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartRepository.save(cart);
                return "장바구니에 같은 상품이 있어 수량이 증가했습니다.";
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setItem(item);
                cartItem.setQuantity(1);
                cartItem.setPrice(item.getPrice());
                cart.addCartItem(cartItem);
                cartRepository.save(cart);
                return "장바구니에 상품이 추가되었습니다.";
            }
        } else {
            return "상품이나 사용자가 존재하지 않습니다.";
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        cartItemRepository.findById(cartItemId).ifPresent(cartItem -> {
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem.getCart());
        });
    }
    //------------------------------------------------------------------------------------------------------------------

    // 특정 Item ID로 CartItem을 삭제하는 메서드 추가
    public void deleteCartItemsByItemId(Long itemId) {
        cartItemRepository.deleteByItem_ItemId(itemId);
    }

    public void deleteCartItemById(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }


}