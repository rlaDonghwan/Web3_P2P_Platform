package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.OrderDetailDTO;
import com.inhatc.SafeCommerce.model.Item;
import com.inhatc.SafeCommerce.repository.ItemRepository;
import com.inhatc.SafeCommerce.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ItemRepository itemRepository;

    private final OrderRepository orderRepository;


    // 홈 화면
    @GetMapping("/home")
    public String home(Model model) {
        List<Item> items = itemRepository.findAllActiveItems().stream().map(item -> {
            // 각 Item의 이미지들을 Base64로 인코딩
            item.getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image); // ItemImage에 Base64 이미지 필드를 추가
            });
            return item;
        }).collect(Collectors.toList());

        model.addAttribute("items", items);
        return "home";
    }
    //------------------------------------------------------------------------------------------------------------------

    // 상품 클릭 시 상세 정보
    @GetMapping("/items/{itemId}")
    public String getItemDetail(@PathVariable Long itemId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // 세션에서 userId 가져오기
        model.addAttribute("userId", userId); // 모델에 추가

        Optional<Item> optionalItem = itemRepository.findActiveItemById(itemId); // 삭제되지 않은 상품만 조회
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            item.getImages().forEach(image -> {
                String base64Image = "data:image/png;base64," + Base64Utils.encodeToString(image.getImageData());
                image.setBase64Image(base64Image); // ItemImage에 Base64 이미지 필드를 추가
            });
            model.addAttribute("item", item);
            return "item_detail"; // 상품 상세 페이지로 리다이렉트
        } else {
            return "redirect:/home"; // 상품이 없을 경우 홈으로 리다이렉트
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    @GetMapping("/myPage")
    public String myPage(Model model, HttpSession session) {
        // 세션에서 사용자 ID 가져오기
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        // 주문 데이터 가져오기
        List<OrderDetailDTO> orders = orderRepository.findOrderDetailsByUserId(userId);

        // 모델에 데이터 추가
        model.addAttribute("orders", orders);

        return "myPage"; // myPage.html로 이동
    }
}