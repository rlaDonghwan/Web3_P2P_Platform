package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.AuthenticationRequest;
import com.inhatc.SafeCommerce.dto.MetaMaskRequest;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.service.MetaMaskAuthService;
import com.inhatc.SafeCommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);


    @Autowired
    private MetaMaskAuthService metaMaskAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession httpSession;

    // 로그인 요청 처리
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest) {
        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            UserDTO user = userService.findOrCreateUser(authRequest.getAddress());
            httpSession.setAttribute("user", user);
            logger.info("User session set with Account ID: {}", user.getAccountId());
        }
        return response;
    }

    // 서명 기반 로그인 요청 처리
    @PostMapping("/auth/login-with-signature")
    public ResponseEntity<?> loginWithSignature(@RequestBody MetaMaskRequest request) {
        logger.info("Received JSON: account={}, signature={}", request.getAccount(), request.getSignature());

        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setAddress(request.getAccount());
        authRequest.setSignature(request.getSignature());

        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            UserDTO user = userService.findOrCreateUser(request.getAccount());
            httpSession.setAttribute("user", user);
            logger.info("User session set with Account ID: {}", user.getAccountId());
            return ResponseEntity.ok("로그인 성공!");
        } else {
            logger.warn("Signature verification failed for Account: {}", request.getAccount());
            return ResponseEntity.badRequest().body("서명 검증 실패");
        }
    }
}
