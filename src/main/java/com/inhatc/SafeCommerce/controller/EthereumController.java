package com.inhatc.SafeCommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class EthereumController {

    // sendEther.html 페이지 렌더링
    @GetMapping("/sendEther")
    public String sendEtherPage() {
        return "sendEther"; // .html 확장자는 생략
    }

}
