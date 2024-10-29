//package com.inhatc.SafeCommerce.controller;
//
//import com.inhatc.SafeCommerce.dto.AuthenticationRequest;
//import com.inhatc.SafeCommerce.dto.UserDTO;
//import com.inhatc.SafeCommerce.service.MetaMaskAuthService;
//import com.inhatc.SafeCommerce.service.UserService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/auth")
//public class MetaMaskAuthController {
//
//    @Autowired
//    private MetaMaskAuthService metaMaskAuthService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private HttpSession httpSession;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest) {
//        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
//
//        // 인증 성공 시 사용자 정보를 세션에 저장
//        if (response.getStatusCode().is2xxSuccessful()) {
//            UserDTO user = userService.findOrCreateUser(authRequest.getAddress());
//            httpSession.setAttribute("user", user);
//        }
//
//        return response;
//    }
//}