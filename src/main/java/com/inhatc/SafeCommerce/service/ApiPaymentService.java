//package com.inhatc.SafeCommerce.service;
//
//import com.inhatc.SafeCommerce.model.*;
//import com.inhatc.SafeCommerce.repository.CartRepository;
//import com.inhatc.SafeCommerce.repository.ItemRepository;
//import com.inhatc.SafeCommerce.repository.OrderRepository;
//import com.inhatc.SafeCommerce.repository.UserRepository;
//import com.inhatc.SafeCommerce.util.DataParserUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@Service
//public class ApiPaymentService {
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CartRepository cartRepository;
//
//    /**
//     * 상품 수량 확인 및 예약
//     *
//     * @param itemId 상품 ID
//     * @param requestedQuantity 요청된 수량
//     * @return 수량 확인 결과
//     */
//    public String checkAndReserveQuantity(Long itemId, int requestedQuantity) {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("상품 ID가 유효하지 않습니다."));
//
//        if (item.getQuantity() < requestedQuantity) {
//            return "상품 수량이 부족합니다.";
//        }
//
//        // 수량 감소 (예약)
//        item.setQuantity(item.getQuantity() - requestedQuantity);
//        itemRepository.save(item);
//
//        return "수량이 충분합니다.";
//    }
//
//    /**
//     * 주문 처리
//     *
//     * @param userId 사용자 ID
//     * @param items 주문 항목
//     * @param buyerName 구매자 이름
//     * @param buyerAddress 구매자 주소
//     * @param buyerContact 구매자 연락처
//     * @param transactionHash 트랜잭션 해시
//     * @return 처리 결과 메시지
//     */
//    public String processOrder(Long userId, List<Map<String, Object>> items, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
//        // 사용자 확인
//        var user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
//
//        // 주문 생성
//        Order order = new Order();
//        order.setUser(user);
//        order.setOrderDate(LocalDate.now());
//        order.setBuyerName(buyerName);
//        order.setBuyerAddress(buyerAddress);
//        order.setBuyerContact(buyerContact);
//        order.setTransactionId(transactionHash);
//        order.setStatus(OrderStatus.ORDER);
//
//        // 주문 항목 추가
//        for (Map<String, Object> itemData : items) {
//            Long itemId = DataParserUtil.parseLongValue(itemData.get("itemId"));
//            int quantity = DataParserUtil.parseIntValue(itemData.get("quantity"));
//
//            Item item = itemRepository.findById(itemId)
//                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상품 ID: " + itemId));
//            if (item.getQuantity() < quantity) {
//                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
//            }
//
//            // 주문 항목 생성 및 수량 감소
//            OrderItem orderItem = new OrderItem();
//            orderItem.setItem(item);
//            orderItem.setOrder(order);
//            orderItem.setOrderPrice(item.getPrice() * quantity);
//            orderItem.setCount(quantity);
//
//            item.setQuantity(item.getQuantity() - quantity);
//            itemRepository.save(item);
//
//            order.addOrderItem(orderItem);
//        }
//
//        // 주문 저장
//        orderRepository.save(order);
//        return "결제가 성공적으로 처리되었습니다.";
//    }
//
//    public Map<String, Object> getCartDetails(Long cartId) {
//        Cart cart = cartRepository.findCartWithDetails(cartId)
//                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));
//
//        Map<User, List<CartItem>> sellerItemsMap = new HashMap<>();
//        Map<User, Integer> sellerTotals = new HashMap<>();
//        List<String> sellerAddresses = new ArrayList<>();
//        List<Integer> sellerAmounts = new ArrayList<>();
//        int totalPrice = 0;
//
//        for (CartItem cartItem : cart.getCartItems()) {
//            User seller = cartItem.getItem().getUser();
//
//            sellerItemsMap.computeIfAbsent(seller, k -> new ArrayList<>()).add(cartItem);
//
//            int sellerTotal = sellerTotals.getOrDefault(seller, 0);
//            int itemTotal = cartItem.getPrice() * cartItem.getQuantity();
//            sellerTotal += itemTotal;
//            sellerTotals.put(seller, sellerTotal);
//
//            totalPrice += itemTotal;
//        }
//
//        sellerTotals.forEach((seller, total) -> {
//            sellerAddresses.add(seller.getAccountId());
//            sellerAmounts.add(total);
//        });
//
//        Map<String, Object> cartData = new HashMap<>();
//        cartData.put("sellerItemsMap", sellerItemsMap);
//        cartData.put("sellerTotals", sellerTotals);
//        cartData.put("sellerAddresses", sellerAddresses);
//        cartData.put("sellerAmounts", sellerAmounts);
//        cartData.put("totalPrice", totalPrice);
//
//        return cartData;
//    }
//
//    /**
//     * 장바구니 결제 처리
//     *
//     * @param cartId 장바구니 ID
//     * @param buyerName 구매자 이름
//     * @param buyerAddress 구매자 주소
//     * @param buyerContact 구매자 연락처
//     * @param transactionHash 트랜잭션 해시
//     * @return 처리 결과 메시지
//     */
//    public String processCartPayment(Long cartId, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
//        Cart cart = cartRepository.findCartWithDetails(cartId)
//                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));
//
//        Order order = new Order();
//        order.setUser(cart.getUser());
//        order.setOrderDate(LocalDate.now());
//        order.setBuyerName(buyerName);
//        order.setBuyerAddress(buyerAddress);
//        order.setBuyerContact(buyerContact);
//        order.setTransactionId(transactionHash);
//        order.setStatus(OrderStatus.ORDER);
//
//        for (CartItem cartItem : cart.getCartItems()) {
//            Item item = cartItem.getItem();
//            if (item.getQuantity() < cartItem.getQuantity()) {
//                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
//            }
//
//            OrderItem orderItem = new OrderItem();
//            orderItem.setItem(item);
//            orderItem.setOrder(order);
//            orderItem.setOrderPrice(cartItem.getPrice() * cartItem.getQuantity());
//            orderItem.setCount(cartItem.getQuantity());
//
//            item.setQuantity(item.getQuantity() - cartItem.getQuantity());
//            itemRepository.save(item);
//
//            order.addOrderItem(orderItem);
//        }
//
//        orderRepository.save(order);
//        return "장바구니 결제가 성공적으로 처리되었습니다.";
//    }
//
//
//}