package com.inhatc.SafeCommerce.service;

import com.google.gson.Gson;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private final Gson gson = new Gson(); // JSON 변환을 위한 Gson 객체

    /**
     * 사용자 ID를 기반으로 장바구니를 조회합니다.
     */
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

    /**
     * 장바구니에 상품을 추가합니다.
     */
    public String addItemToCart(Long userId, Long itemId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (userOptional.isEmpty() || itemOptional.isEmpty()) {
            return "상품이나 사용자가 존재하지 않습니다.";
        }

        User user = userOptional.get();
        Item item = itemOptional.get();

        if (item.getQuantity() < 1) {
            return "상품의 재고가 부족합니다.";
        }

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
            if (cartItem.getQuantity() + 1 > item.getQuantity()) {
                return "상품의 재고가 부족합니다.";
            }
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setItem(item);
            cartItem.setQuantity(1);
            cartItem.setPrice(item.getPrice());
            cart.addCartItem(cartItem);
        }

        cartRepository.save(cart);
        return "장바구니에 상품이 추가되었습니다.";
    }

    /**
     * 장바구니 상품의 수량을 업데이트합니다.
     */
    public String updateCartItemQuantity(Long cartItemId, int quantity) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isEmpty()) {
            return "장바구니 상품을 찾을 수 없습니다.";
        }

        CartItem cartItem = cartItemOptional.get();
        Item item = cartItem.getItem();

        if (quantity > item.getQuantity()) {
            return "재고 수량을 초과하는 요청입니다.";
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem.getCart());
        return "장바구니 상품 수량이 업데이트되었습니다.";
    }

    /**
     * 장바구니에서 특정 상품을 삭제합니다.
     */
    public void deleteCartItemById(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * 판매자별로 장바구니 상품을 그룹화하여 반환합니다.
     */
    public Map<String, List<Map<String, Object>>> getSellerItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> {
                            String accountId = item.getItem().getUser().getAccountId();
                            return accountId.length() > 10 ? accountId.substring(0, 10) : accountId;
                        },
                        Collectors.mapping(
                                cartItem -> Map.of(
                                        "itemId", cartItem.getItem().getItemId(),
                                        "itemName", cartItem.getItem().getItemName(),
                                        "price", cartItem.getPrice(),
                                        "quantity", cartItem.getQuantity()
                                ),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 판매자별 장바구니 상품 데이터를 JSON 문자열로 변환하여 반환합니다.
     */
    public String getSellerItemsAsJson(List<CartItem> cartItems) {
        Map<String, List<Map<String, Object>>> sellerItems = getSellerItems(cartItems);
        return gson.toJson(sellerItems); // JSON 문자열로 변환 후 반환
    }

    /**
     * 특정 상품 ID를 기준으로 장바구니 상품을 삭제합니다.
     */
    public void deleteCartItemsByItemId(Long itemId) {
        cartItemRepository.deleteByItem_ItemId(itemId);
    }
}