package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.Order;
import com.inhatc.SafeCommerce.model.OrderItem;
import com.inhatc.SafeCommerce.model.OrderStatus;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.OrderRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;

@Service
public class PaymentService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public String checkAndReserveQuantity(Long itemId, int quantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));

        // 주문할 수량이 충분한지 확인
        if (item.getQuantity() < quantity) {
            return "Insufficient quantity";
        }

        // 수량 감소
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        return "Sufficient quantity";
    }

    public String processOrder(Long userId, Long itemId, int quantity, String buyerName, String buyerAddress, String buyerContact) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));

        // Order 생성
        Order order = new Order();
        order.setUser(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID")));
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setStatus(OrderStatus.ORDER);

        // OrderItem 생성 및 설정
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrder(order);
        orderItem.setOrderPrice(item.getPrice());
        orderItem.setCount(quantity);

        // Order와 OrderItem 연결 및 저장
        order.setOrderItems(Collections.singletonList(orderItem));
        orderRepository.save(order);

        return "Order saved successfully";
    }
}