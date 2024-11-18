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

    public String checkAndReserveQuantity(Long itemId, int requestedQuantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

        if (item.getQuantity() < requestedQuantity) {
            return "상품 수량이 부족합니다.";
        }

        // 수량이 충분하면 예약 처리 (예약은 단순 확인으로 가정)
        return "수량이 충분합니다.";
    }

    public String processOrder(Long userId, Long itemId, int quantity, String buyerName, String buyerAddress, String buyerContact) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 item ID입니다."));

        if (item.getQuantity() < quantity) {
            return "상품 수량이 부족합니다.";
        }

        // 주문 생성
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
        orderItem.setOrderPrice(item.getPrice() * quantity); // 총 가격 계산
        orderItem.setCount(quantity);

        // 관계 설정
        order.addOrderItem(orderItem);

        // 상품 수량 감소
        item.setQuantity(item.getQuantity() - quantity);
        itemRepository.save(item);

        // 주문 저장
        orderRepository.save(order);

        return "구매가 성공적으로 처리되었습니다.";
    }
}