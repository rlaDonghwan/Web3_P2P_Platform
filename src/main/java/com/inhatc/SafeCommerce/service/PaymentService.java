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
@Service
public class PaymentService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public String checkAndReserveQuantity(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

        // 상품 수량이 충분한지 확인
        if (item.getQuantity() < 1) {
            return "Insufficient quantity";
        }

        // 수량이 충분하면 결제 진행 가능
        return "Sufficient quantity";
    }

    public String processOrder(Long userId, Long itemId, int quantity, String buyerName, String buyerAddress, String buyerContact) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

        // 상품 수량이 충분한지 확인
        if (item.getQuantity() < quantity) {
            return "상품 수량이 부족합니다.";
        }

        // Order 생성
        Order order = new Order();
        order.setUser(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 user ID입니다.")));
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setStatus(OrderStatus.ORDER);

        // OrderItem 생성 및 설정
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(item.getPrice());
        orderItem.setCount(quantity);

        // Order에 OrderItem 추가
        order.addOrderItem(orderItem);

        // 상품 수량 감소
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        // Order와 OrderItem 저장
        orderRepository.save(order);

        return "Order saved successfully";
    }
}