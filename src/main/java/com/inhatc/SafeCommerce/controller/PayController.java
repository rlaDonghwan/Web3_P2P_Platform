package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.model.Cart;
import com.inhatc.SafeCommerce.model.CartItem;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.model.User;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import jakarta.servlet.http.HttpSession;
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

    // 프로퍼티에서 값 읽기
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
                String base64Image = "data:image/png;base64," + Base64Utils. encodeToString(image.getImageData());
                image.setBase64Image(base64Image);
            });
            model.addAttribute("item", item);
        } else {
            return "error"; // 에러 페이지로 이동
        }
        return "pay_detail"; // 결제 상세 페이지로 이동
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

        // 상품 정보 가져오기
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();

            // 상품 정보 추가
            model.addAttribute("itemName", item.getItemName());
            model.addAttribute("itemId", itemId);
            model.addAttribute("itemPrice", item.getPrice());
            model.addAttribute("quantity", quantity);
            model.addAttribute("totalPrice", item.getPrice() * quantity); // 총 금액
        }

        // 구매 정보 추가
        model.addAttribute("ethPrice", ethPrice);
        model.addAttribute("buyerName", buyerName);
        model.addAttribute("buyerAddress", buyerAddress);
        model.addAttribute("buyerContact", buyerContact);

        return "sendEther"; // 결제 페이지로 이동
    }


    @GetMapping("/cart/pay")
    public String cartPayPage(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("checkoutCart");
        Integer totalPrice = (Integer) session.getAttribute("totalPrice");

        if (cart == null || totalPrice == null) {
            return "redirect:/cart"; // 세션이 비어 있으면 장바구니 페이지로 이동
        }

        Map<User, List<CartItem>> sellerItemsMap = new HashMap<>();
        Map<User, Integer> sellerTotals = new HashMap<>();

        for (CartItem cartItem : cart.getCartItems()) {
            User seller = cartItem.getItem().getUser();
            sellerItemsMap.computeIfAbsent(seller, k -> new ArrayList<>()).add(cartItem);

            int sellerTotal = sellerTotals.getOrDefault(seller, 0);
            sellerTotal += cartItem.getPrice() * cartItem.getQuantity();
            sellerTotals.put(seller, sellerTotal);
        }

        // 환경 변수 추가
        model.addAttribute("sellerItemsMap", sellerItemsMap);
        model.addAttribute("sellerTotals", sellerTotals);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("contractAddress", contractAddress); // 추가
        model.addAttribute("infuraApiKey", infuraApiKey);       // 추가

        return "cart_payment";
    }

}