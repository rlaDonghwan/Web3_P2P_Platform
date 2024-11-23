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
import java.util.*;

@Service
public class PayService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Optional<Item> getItemById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    @Transactional
    public Order createOrder(String buyerName, String buyerAddress, String buyerContact, int quantity, Item item) {
        Order order = new Order();
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.ORDER);

        // User 설정
        User user = userRepository.findById(item.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        order.setUser(user);

        // 주문 항목 추가
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(quantity);
        orderItem.setOrderPrice(item.getPrice() * quantity);
        order.addOrderItem(orderItem);

        return orderRepository.save(order);
    }

    @Transactional
    public Map<String, Object> prepareCartPaymentData(Long cartId) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Map<UserDTO, List<CartItem>> sellerItemsMap = new HashMap<>();
        Map<UserDTO, Integer> sellerTotals = new HashMap<>();

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();
            UserDTO sellerDTO = DTOConverter.convertToDTO(seller);

            cartItem.getItem().getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            });

            sellerItemsMap.computeIfAbsent(sellerDTO, k -> new ArrayList<>()).add(cartItem);

            int sellerTotal = sellerTotals.getOrDefault(sellerDTO, 0);
            sellerTotal += cartItem.getPrice() * cartItem.getQuantity();
            sellerTotals.put(sellerDTO, sellerTotal);
        }

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
        responseData.put("orderId", savedOrder.getId());
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
}