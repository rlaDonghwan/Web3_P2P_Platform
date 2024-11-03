package com.inhatc.SafeCommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // 홈 화면
    @GetMapping("/home")
    public String home() {
        return "home";
    }



}