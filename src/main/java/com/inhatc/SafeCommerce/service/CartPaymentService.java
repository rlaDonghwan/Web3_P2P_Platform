package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.dto.DTOConverter;
import com.inhatc.SafeCommerce.dto.PaymentRequest;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.model.*;
import com.inhatc.SafeCommerce.repository.CartRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.OrderRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartPaymentService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Map<String, Object> prepareCartPaymentData(Long cartId) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Map<UserDTO, List<CartItem>> sellerItemsMap = new HashMap<>();
        Map<UserDTO, Integer> sellerTotals = new HashMap<>();
        List<String> sellerAddresses = new ArrayList<>();
        List<Integer> sellerAmounts = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();
            UserDTO sellerDTO = DTOConverter.convertToDTO(seller);

            cartItem.getItem().getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            });

            sellerItemsMap.computeIfAbsent(sellerDTO, k -> new ArrayList<>()).add(cartItem);

            int sellerTotal = sellerTotals.getOrDefault(sellerDTO, 0);
            int itemTotal = cartItem.getPrice() * cartItem.getQuantity();
            sellerTotal += itemTotal;
            sellerTotals.put(sellerDTO, sellerTotal);

            totalPrice += itemTotal;
        }

        sellerTotals.forEach((seller, total) -> {
            sellerAddresses.add(seller.getAccountId());
            sellerAmounts.add(total);
        });

        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.ORDER);

        User user = userRepository.findById(cart.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        order.setUser(user);

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem.getItem());
            orderItem.setCount(cartItem.getQuantity());
            orderItem.setOrderPrice(cartItem.getPrice() * cartItem.getQuantity());
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sellerItemsMap", sellerItemsMap);
        responseData.put("sellerTotals", sellerTotals);
        responseData.put("sellerAddresses", sellerAddresses);
        responseData.put("sellerAmounts", sellerAmounts);
        responseData.put("orderId", savedOrder.getId());
        responseData.put("totalPrice", totalPrice);

        return responseData;
    }

    @Transactional
    public void updateOrderWithPaymentDetails(PaymentRequest paymentRequest) {
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setBuyerName(paymentRequest.getBuyerName());
        order.setBuyerAddress(paymentRequest.getBuyerAddress());
        order.setBuyerContact(paymentRequest.getBuyerContact());
        order.setTransactionId(paymentRequest.getTransactionHash());
        order.setStatus(OrderStatus.ORDER);
        orderRepository.save(order);
    }

    /**
     * 장바구니 세부 정보 가져오기
     */
    public Map<String, Object> getCartDetails(Long cartId) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));

        Map<User, List<CartItem>> sellerItemsMap = new HashMap<>();
        Map<User, Integer> sellerTotals = new HashMap<>();
        List<String> sellerAddresses = new ArrayList<>();
        List<Integer> sellerAmounts = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();

            sellerItemsMap.computeIfAbsent(seller, k -> new ArrayList<>()).add(cartItem);

            int sellerTotal = sellerTotals.getOrDefault(seller, 0);
            int itemTotal = cartItem.getPrice() * cartItem.getQuantity();
            sellerTotal += itemTotal;
            sellerTotals.put(seller, sellerTotal);

            totalPrice += itemTotal;
        }

        sellerTotals.forEach((seller, total) -> {
            sellerAddresses.add(seller.getAccountId());
            sellerAmounts.add(total);
        });

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("sellerItemsMap", sellerItemsMap);
        cartData.put("sellerTotals", sellerTotals);
        cartData.put("sellerAddresses", sellerAddresses);
        cartData.put("sellerAmounts", sellerAmounts);
        cartData.put("totalPrice", totalPrice);

        return cartData;
    }

    /**
     * 장바구니 결제 처리
     */
    public String processCartPayment(Long cartId, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setTransactionId(transactionHash);
        order.setStatus(OrderStatus.ORDER);

        for (CartItem cartItem : cart.getCartItems()) {
            Item item = cartItem.getItem();
            if (item.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setOrderPrice(cartItem.getPrice() * cartItem.getQuantity());
            orderItem.setCount(cartItem.getQuantity());

            item.setQuantity(item.getQuantity() - cartItem.getQuantity());
            itemRepository.save(item);

            order.addOrderItem(orderItem);
        }

        orderRepository.save(order);
        return "장바구니 결제가 성공적으로 처리되었습니다.";
    }
}