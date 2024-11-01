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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
        }
        return response;
    }

    // 로그인 상태 확인
    @GetMapping("/auth/check-login-status")
    public ResponseEntity<?> checkLoginStatus(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        String balance = (String) session.getAttribute("balance");

        if (isLoggedIn != null && isLoggedIn) {
            Map<String, String> response = new HashMap<>();
            response.put("isLoggedIn", "true");
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(Collections.singletonMap("isLoggedIn", "false"));
        }
    }

    // 서명 기반 로그인 요청 처리
    @PostMapping("/auth/login-with-signature")
    public ResponseEntity<?> loginWithSignature(@RequestBody MetaMaskRequest request) {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setAddress(request.getAccount());
        authRequest.setSignature(request.getSignature());

        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            UserDTO user = userService.findOrCreateUser(request.getAccount());
            httpSession.setAttribute("user", user);
            httpSession.setAttribute("isLoggedIn", true);

            logger.info("User session set with Account ID: {}", user.getAccountId());
            return ResponseEntity.ok("로그인 성공!");
        } else {
            return ResponseEntity.badRequest().body("서명 검증 실패");
        }
    }

    // 클라이언트에서 전송한 잔액을 세션에 저장하는 엔드포인트
    @PostMapping("/auth/save-balance")
    public ResponseEntity<?> saveBalance(@RequestBody Map<String, String> balanceData, HttpSession session) {
        String balance = balanceData.get("balance");
        session.setAttribute("balance", balance); // 세션에 잔액 저장
        logger.info("Save balance: {}", balance);
        return ResponseEntity.ok("Balance saved");
    }

    // 로그아웃 요청 처리
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 현재 세션 무효화
        return ResponseEntity.ok("Logged out successfully");
    }
}