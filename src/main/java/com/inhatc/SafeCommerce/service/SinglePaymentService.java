package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.model.*;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.OrderRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import com.inhatc.SafeCommerce.util.DataParserUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SinglePaymentService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Optional<Item> getItemById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    // 상품 수량 확인 및 예약
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
    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    //단일 상품 결제 처리
    public String processOrder(Long userId, List<Map<String, Object>> items, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
        // 사용자 확인
        User user = userRepository.findById(userId)
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
            Long itemId = DataParserUtil.parseLongValue(itemData.get("itemId"));
            int quantity = DataParserUtil.parseIntValue(itemData.get("quantity"));

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상품 ID: " + itemId));
            if (item.getQuantity() < quantity) {
                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setOrderPrice(item.getPrice() * quantity);
            orderItem.setCount(quantity);

            item.setQuantity(item.getQuantity() - quantity);
            itemRepository.save(item);

            order.addOrderItem(orderItem);
        }

        // 주문 저장
        orderRepository.save(order);
        return "결제가 성공적으로 처리되었습니다.";
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
}