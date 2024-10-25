package com.inhatc.SafeCommerce.controller;

import com.inhatc.SafeCommerce.dto.MetaMaskRequest;
import com.inhatc.SafeCommerce.dto.UserDTO;
import com.inhatc.SafeCommerce.service.MetaMaskService;
import com.inhatc.SafeCommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private MetaMaskService metaMaskService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession httpSession;

    @GetMapping("/home")
    public String home() {
        logger.info("Navigating to home page");
        return "home";  // home.html 반환
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody MetaMaskRequest request) {
        logger.info("Received JSON: account={}, signature={}", request.getAccount(), request.getSignature());

        String accountId = request.getAccount();
        UserDTO user = userService.findOrCreateUser(accountId);

        logger.info("User found or created with Account ID: {}", user.getAccountId());

        boolean isVerified = metaMaskService.verifySignature(request.getAccount(), request.getSignature());
        logger.info("Signature verification result: {}", isVerified);

        if (isVerified) {
            httpSession.setAttribute("user", user);
            logger.info("User session set with Account ID: {}", user.getAccountId());
            return ResponseEntity.ok("로그인 성공!");
        } else {
            logger.warn("Signature verification failed for Account: {}", request.getAccount());
            return ResponseEntity.badRequest().body("서명 검증 실패");
        }
    }
}
