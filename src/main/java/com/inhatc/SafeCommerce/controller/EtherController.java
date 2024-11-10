package com.inhatc.SafeCommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class EtherController {

    // sendEther.html 페이지 렌더링
    @GetMapping("/send")
    public String sendEtherPage() {
        return "send"; // .html 확장자는 생략
    }
    //------------------------------------------------------------------------------------------------------------------
}