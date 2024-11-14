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
import org.springframework.web.bind.annotation.*;

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

    // 일반 로그인 요청 처리
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest) {
        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            UserDTO user = userService.findOrCreateUser(authRequest.getAddress());
            setUserSession(user);
        }
        return response;
    }
    //------------------------------------------------------------------------------------------------------------------

    // 로그인 상태 확인 요청 처리
    @GetMapping("/auth/check-login-status")
    public ResponseEntity<?> checkLoginStatus(HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");

        if (Boolean.TRUE.equals(isLoggedIn)) {
            String balance = (String) session.getAttribute("balance");
            Map<String, String> response = new HashMap<>();
            response.put("isLoggedIn", "true");
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(Collections.singletonMap("isLoggedIn", "false"));
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    // 서명 기반 로그인 요청 처리
    @PostMapping("/auth/login-with-signature")
    public ResponseEntity<?> loginWithSignature(@RequestBody MetaMaskRequest request) {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setAddress(request.getAccount());
        authRequest.setSignature(request.getSignature());

        ResponseEntity<?> response = metaMaskAuthService.authenticate(authRequest);
        if (response.getStatusCode().is2xxSuccessful()) {
            UserDTO user = userService.findOrCreateUser(request.getAccount());
            setUserSession(user);

            logger.info("User session set with Account ID: {}", user.getAccountId());

            // 로그인 성공 시 userId를 함께 반환
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "로그인 성공!");
            responseBody.put("userId", user.getId()); // userId 반환

            return ResponseEntity.ok(responseBody);
        } else {
            return ResponseEntity.badRequest().body("서명 검증 실패");
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    // 클라이언트에서 전송한 잔액을 세션에 저장하는 메서드
    @PostMapping("/auth/save-balance")
    public ResponseEntity<?> saveBalance(@RequestBody Map<String, String> balanceData) {
        String balance = balanceData.get("balance");
        httpSession.setAttribute("balance", balance); // 세션에 잔액 저장
        logger.info("Save balance: {}", balance);
        return ResponseEntity.ok("Balance saved");
    }
    //------------------------------------------------------------------------------------------------------------------

    //로그아웃 요청 처리
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout() {
        httpSession.invalidate(); // 현재 세션 무효화
        return ResponseEntity.ok("Logged out successfully");
    }
    //------------------------------------------------------------------------------------------------------------------

    //세션에 사용자 정보를 설정하는 헬퍼 메서드
    private void setUserSession(UserDTO user) {
        httpSession.setAttribute("user", user);
        httpSession.setAttribute("userId", user.getId()); // userId도 세션에 저장
        httpSession.setAttribute("isLoggedIn", true);
    }
    //------------------------------------------------------------------------------------------------------------------

    //글쓴이 userID찾는 메서드
    @GetMapping("/api/getAuthorAccount/{itemId}")
    @ResponseBody
    public ResponseEntity<?> getAuthorAccount(@PathVariable Long itemId) {
        Long authorId = userService.findAuthorIdByItemId(itemId); // itemId로 글쓴이 ID 조회
        if (authorId != null) {
            UserDTO user = userService.getUserById(authorId);
            if (user != null && user.getAccountId() != null) {
                Map<String, String> response = new HashMap<>();
                response.put("account", user.getAccountId()); // 글쓴이 계좌 주소 반환
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(404).body("작성자를 찾을 수 없습니다.");
    }
    //------------------------------------------------------------------------------------------------------------------
}