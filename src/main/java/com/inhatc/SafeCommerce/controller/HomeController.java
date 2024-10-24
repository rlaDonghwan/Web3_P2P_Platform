package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.MetaMaskRequest;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.service.MetaMaskService;
import com.inhatc.SafeCommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller

public class HomeController {

    @Autowired
    private MetaMaskService metaMaskService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession httpSession;

    @GetMapping("/home")
    public String home() {
        return "home";  // home.html 반환
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody MetaMaskRequest request) {
        System.out.println("Received account: " + request.getAccount());
        System.out.println("Received signature: " + request.getSignature());

        String accountId = request.getAccount();

        UserDTO user = userService.findOrCreateUser(accountId);

        boolean isVerified = metaMaskService.verifySignature(request.getAccount(), request.getSignature());

        if (isVerified) {
            httpSession.setAttribute("user", user);
            return ResponseEntity.ok("로그인 성공!");
        } else {
            return ResponseEntity.badRequest().body("서명 검증 실패");
        }
    }
}