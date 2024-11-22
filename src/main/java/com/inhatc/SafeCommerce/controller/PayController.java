package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.DTOConverter;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.CartRepository;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class PayController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

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
        // 세션에서 cartId와 totalPrice 가져오기
        Long cartId = (Long) session.getAttribute("cartId");
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");

        // 디버깅 로그 추가
        if (cartId == null || totalPrice == null) {
            System.out.println("Session cartId or totalPrice is missing.");
            return "redirect:/cart";
        }

        // Cart 정보 가져오기
        Cart cart = cartRepository.findCartWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        Map<UserDTO, List<CartItem>> sellerItemsMap = new HashMap<>();
        Map<UserDTO, Integer> sellerTotals = new HashMap<>();

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();
            UserDTO sellerDTO = DTOConverter.convertToDTO(seller);

            sellerItemsMap.computeIfAbsent(sellerDTO, k -> new ArrayList<>()).add(cartItem);

            int sellerTotal = sellerTotals.getOrDefault(sellerDTO, 0);
            sellerTotal += cartItem.getPrice() * cartItem.getQuantity();
            sellerTotals.put(sellerDTO, sellerTotal);
        }

        // 모델에 데이터 추가
        model.addAttribute("sellerItemsMap", sellerItemsMap);
        model.addAttribute("sellerTotals", sellerTotals);
        model.addAttribute("totalPrice", totalPrice);

        // JSON 형태로 프론트엔드 전달
        model.addAttribute("sellerItemsJson", sellerItemsMap);
        model.addAttribute("sellerAmountsJson", sellerTotals.values());
        model.addAttribute("contractAddress", contractAddress);
        model.addAttribute("infuraApiKey", infuraApiKey);

        return "cart_payment";
    }
}