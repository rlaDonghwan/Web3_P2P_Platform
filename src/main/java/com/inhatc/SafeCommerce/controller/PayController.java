package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.DTOConverter;
import com.inhatc.SafeCommerce.dto.PaymentRequest;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.model.*;
import com.inhatc.SafeCommerce.repository.CartRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.OrderRepository;
import com.inhatc.SafeCommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
public class PayController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${CONTRACT_ADDRESS}")
    private String contractAddress;

    @Value("${INFURA_API_KEY}")
    private String infuraApiKey;

    @GetMapping("/pay/{itemId}")
    public String payPage(@PathVariable Long itemId, Model model) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            });
            model.addAttribute("item", item);
        } else {
            return "error";
        }
        return "pay_detail";
    }

    @GetMapping("/sendEther")
    @Transactional
    public String sendEtherPage(
            @RequestParam String ethPrice,
            @RequestParam String buyerName,
            @RequestParam String buyerAddress,
            @RequestParam String buyerContact,
            @RequestParam int quantity,
            @RequestParam Long itemId,
            Model model) {

        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            // 주문 저장
            Order order = new Order();
            order.setBuyerName(buyerName);
            order.setBuyerAddress(buyerAddress);
            order.setBuyerContact(buyerContact);
            order.setOrderDate(LocalDate.now());
            order.setStatus(OrderStatus.ORDER);

            // 현재 로그인한 사용자 정보 설정 (세션에서 가져오기)
            User user = userRepository.findById(item.getUser().getId()).orElseThrow();
            order.setUser(user);

            // 주문 항목 추가
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(quantity);
            orderItem.setOrderPrice(item.getPrice() * quantity);
            order.addOrderItem(orderItem);

            // 주문 저장
            orderRepository.save(order);

            // 모델에 데이터 추가
            model.addAttribute("itemName", item.getItemName());
            model.addAttribute("itemId", itemId);
            model.addAttribute("itemPrice", item.getPrice());
            model.addAttribute("quantity", quantity);
            model.addAttribute("totalPrice", item.getPrice() * quantity);
        }

        model.addAttribute("ethPrice", ethPrice);
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("buyerAddress", buyerAddress);
        model.addAttribute("buyerContact", buyerContact);

        return "sendEther";
    }

    @GetMapping("/cart/pay")
    @Transactional
    public String cartPayPage(HttpSession session, Model model) {
        Long cartId = (Long) session.getAttribute("cartId");
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");

        if (cartId == null || totalPrice == null) {
            System.out.println("Session cartId or totalPrice is missing.");
            return "redirect:/cart";
        }

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

        // 주문 생성 및 저장
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.ORDER);

        // 현재 로그인한 사용자 정보 설정 (세션에서 가져오기)
        User user = userRepository.findById(cart.getUser().getId()).orElseThrow();
        order.setUser(user);

        // 주문 항목 추가
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cartItem.getItem());
            orderItem.setCount(cartItem.getQuantity());
            orderItem.setOrderPrice(cartItem.getPrice() * cartItem.getQuantity());
            order.addOrderItem(orderItem);
        }

        // 주문 저장
        orderRepository.save(order);

        model.addAttribute("sellerItemsMap", sellerItemsMap);
        model.addAttribute("sellerTotals", sellerTotals);
        model.addAttribute("totalPrice", totalPrice);

        model.addAttribute("sellerItemsJson", sellerItemsMap);
        model.addAttribute("sellerAmountsJson", sellerTotals.values());
        model.addAttribute("contractAddress", contractAddress);
        model.addAttribute("infuraApiKey", infuraApiKey);
        model.addAttribute("orderId", order.getId());
        model.addAttribute("contractAddress", contractAddress);


        return "cart_payment";
    }

    @PostMapping("/payment/submit")
    @Transactional
    public ResponseEntity<String> submitPayment(@RequestBody PaymentRequest paymentRequest) {
        Optional<Order> orderOptional = orderRepository.findById(paymentRequest.getOrderId());
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setTransactionId(paymentRequest.getTransactionHash()); // 트랜잭션 해시 저장
            order.setStatus(OrderStatus.ORDER); // 상태 업데이트
            orderRepository.save(order);
            return ResponseEntity.ok("Payment processed successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
    }
}
