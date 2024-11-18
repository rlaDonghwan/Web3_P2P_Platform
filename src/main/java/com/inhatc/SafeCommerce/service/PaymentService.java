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
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // 수량 확인 및 예약
    public String checkAndReserveQuantity(Long itemId, int requestedQuantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

        if (item.getQuantity() < requestedQuantity) {
            return "상품 수량이 부족합니다.";
        }
        return "수량이 충분합니다.";
    }

    // 주문 처리
    public String processOrder(Long userId, List<Map<String, Object>> items, String buyerName, String buyerAddress, String buyerContact) {
        // 주문 생성
        Order order = new Order();
        order.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 user ID입니다.")));
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setStatus(OrderStatus.ORDER);

        for (Map<String, Object> itemData : items) {
            Long itemId = Long.valueOf(itemData.get("itemId").toString());
            int quantity = Integer.parseInt(itemData.get("quantity").toString());

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

            if (item.getQuantity() < quantity) {
                throw new IllegalArgumentException("상품 수량이 부족합니다: " + item.getItemName());
            }

            // 주문 아이템 생성
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setOrderPrice(item.getPrice() * quantity);
            orderItem.setCount(quantity);

            // 상품 수량 감소
            item.setQuantity(item.getQuantity() - quantity);
            itemRepository.save(item);

            // 주문과 주문 아이템의 관계 설정
            order.addOrderItem(orderItem);
        }

        // 주문 저장
        orderRepository.save(order);

        return "구매가 성공적으로 처리되었습니다.";
    }

    private void saveOrder(Long userId, Item item, int quantity, String buyerName, String buyerAddress, String buyerContact) {
        Order order = new Order();
        order.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 user ID입니다.")));
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setStatus(OrderStatus.ORDER);

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrder(order);
        orderItem.setOrderPrice(item.getPrice() * quantity);
        orderItem.setCount(quantity);

        order.addOrderItem(orderItem);
        orderRepository.save(order);
    }
}