package com.inhatc.SafeCommerce.service;

import com.inhatc.SafeCommerce.dto.PaymentRequest;
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

    /**
     * 장바구니 데이터를 처리하고 결제에 필요한 정보를 준비합니다.
     * 사용 컨트롤러: CartPaymentController
     */
    @Transactional
    public Map<String, Object> prepareCartPaymentData(Long cartId) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));

        return processCartItems(cart);
    }

    /**
     * 장바구니 결제 처리
     * 사용 컨트롤러: ApiCartPaymentController
     */
    @Transactional
    public String processCartPayment(Long cartId, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));

        // 주문 생성
        Order order = createOrderFromCart(cart, buyerName, buyerAddress, buyerContact, transactionHash);

        // 재고 업데이트
        updateItemQuantities(cart);

        // 주문 저장
        orderRepository.save(order);

        return "장바구니 결제가 성공적으로 처리되었습니다.";
    }

    /**
     * 장바구니 상세 정보를 반환합니다.
     * 사용 컨트롤러: ApiCartPaymentController
     */
    public Map<String, Object> getCartDetails(Long cartId) {
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 장바구니 ID입니다."));

        return processCartItems(cart);
    }

    /**
     * 결제 후 주문 업데이트
     * 사용 컨트롤러: CartPaymentController
     */
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

    // ===== 내부 헬퍼 메서드 =====

    /**
     * 장바구니의 항목을 처리하여 결제 데이터를 준비합니다.
     * 사용 메서드: prepareCartPaymentData, getCartDetails
     */
    private Map<String, Object> processCartItems(Cart cart) {
        Map<String, List<Map<String, Object>>> sellerItemsMap = new HashMap<>();
        List<String> sellerAddresses = new ArrayList<>();
        List<Integer> sellerAmounts = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();
            String sellerAccountId = seller.getAccountId();

            // 이미지 데이터 처리
            List<String> base64Images = new ArrayList<>();
            cartItem.getItem().getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                base64Images.add(base64Image);
            });

            // 판매자별 항목 추가
            sellerItemsMap.computeIfAbsent(sellerAccountId, k -> new ArrayList<>())
                    .add(Map.of(
                            "itemId", cartItem.getItem().getItemId(),
                            "itemName", cartItem.getItem().getItemName(),
                            "price", cartItem.getPrice(),
                            "quantity", cartItem.getQuantity(),
                            "base64Images", base64Images // 이미지 데이터 추가
                    ));

            int itemTotal = cartItem.getPrice() * cartItem.getQuantity();
            totalPrice += itemTotal;
        }

        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("sellerItemsMap", sellerItemsMap);
        responseData.put("sellerAddresses", sellerAddresses);
        responseData.put("sellerAmounts", sellerAmounts);
        responseData.put("totalPrice", totalPrice);

        return responseData;
    }

    /**
     * 장바구니를 기반으로 주문을 생성합니다.
     * 사용 메서드: processCartPayment
     */
    private Order createOrderFromCart(Cart cart, String buyerName, String buyerAddress, String buyerContact, String transactionHash) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDate.now());
        order.setBuyerName(buyerName);
        order.setBuyerAddress(buyerAddress);
        order.setBuyerContact(buyerContact);
        order.setTransactionId(transactionHash);
        order.setStatus(OrderStatus.ORDER);

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem.getItem());
            orderItem.setOrder(order);
            orderItem.setOrderPrice(cartItem.getPrice() * cartItem.getQuantity());
            orderItem.setCount(cartItem.getQuantity());

            order.addOrderItem(orderItem);
        }

        return order;
    }

    /**
     * 장바구니 항목에 해당하는 아이템의 재고를 업데이트합니다.
     * 사용 메서드: processCartPayment
     */
    private void updateItemQuantities(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            Item item = cartItem.getItem();
            if (item.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("상품 수량 부족: " + item.getItemName());
            }

            item.setQuantity(item.getQuantity() - cartItem.getQuantity());
            itemRepository.save(item);
        }
    }
}