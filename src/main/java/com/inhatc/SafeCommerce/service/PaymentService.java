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

    /**
     * 상품 수량 확인 및 예약
     */
    public String checkAndReserveQuantity(Long itemId, int requestedQuantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품 ID가 유효하지 않습니다."));

        if (item.getQuantity() < requestedQuantity) {
            return "상품 수량이 부족합니다.";
        }

        // 수량 감소 (예약)
        item.setQuantity(item.getQuantity() - requestedQuantity);
        itemRepository.save(item);

        return "수량이 충분합니다.";
    }

    /**
     * 주문 처리
     */
    public String processOrder(Long userId, List<Map<String, Object>> items, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
        // 사용자 확인
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        // 주문 생성
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setTransactionId(transactionHash);
        order.setStatus(OrderStatus.ORDER);

        // 주문 항목 추가
        for (Map<String, Object> itemData : items) {
            Long itemId = parseLongValue(itemData.get("itemId"));
            int quantity = parseIntValue(itemData.get("quantity"));

            // 상품 확인 및 수량 검증
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상품 ID: " + itemId));
            if (item.getQuantity() < quantity) {
                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
            }

            // 주문 항목 생성
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setOrderPrice(item.getPrice() * quantity);
            orderItem.setCount(quantity);

            // 상품 수량 감소
            item.setQuantity(item.getQuantity() - quantity);
            itemRepository.save(item);

            // 주문에 추가
            order.addOrderItem(orderItem);
        }

        // 주문 저장
        orderRepository.save(order);

        return "결제가 성공적으로 처리되었습니다.";
    }

    /**
     * Long 값 변환 메서드
     */
    private Long parseLongValue(Object value) {
        return value instanceof Number ? ((Number) value).longValue()
                : value instanceof String ? Long.valueOf((String) value)
                : throwIllegalArgument("Long");
    }

    /**
     * int 값 변환 메서드
     */
    private int parseIntValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue()
                : value instanceof String ? Integer.parseInt((String) value)
                : throwIllegalArgument("int");
    }

    private <T> T throwIllegalArgument(String expectedType) {
        throw new IllegalArgumentException("잘못된 데이터 타입입니다. " + expectedType + "가 필요합니다.");
    }
}