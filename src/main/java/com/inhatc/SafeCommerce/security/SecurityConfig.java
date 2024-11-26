package com.inhatc.SafeCommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MetaMaskAuthenticationProvider metaMaskAuthenticationProvider; // MetaMask 인증 처리

    /**
     * AuthenticationManager Bean 정의
     * MetaMaskAuthenticationProvider를 ProviderManager에 추가하여 사용자 인증 처리
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(metaMaskAuthenticationProvider));
    }

    /**
     * SecurityFilterChain Bean 정의 (Spring Security의 보안 설정)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 요청 URL에 대한 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 공용 리소스 및 특정 GET 요청 허용
                        .requestMatchers(HttpMethod.GET, "/nonce/*").permitAll()
                        .requestMatchers(
                                "/login",
                                "/auth/**",
                                "/home",
                                "/addItem",
                                "/items/**",
                                "/cart/**",
                                "/pay/**",
                                "/api/**",
                                "/sendEther",
                                "/editItem/**",
                                "/delete/**",
                                "/smartContract/**",
                                "/payment/submit",
                                "/myPage"
                        ).permitAll()
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll() // 정적 리소스 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                // 폼 기반 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login") // 로그인 페이지 경로
                        .defaultSuccessUrl("/home", true) // 로그인 성공 시 기본 경로
                        .failureUrl("/login?error=true") // 로그인 실패 시 이동 경로
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 경로
                        .logoutSuccessUrl("/login?logout=true") // 로그아웃 성공 시 이동 경로
                        .permitAll()
                )
                // CSRF 보호 비활성화 (테스트나 외부 API 접근 시 주로 사용)
                .csrf().disable();

        return http.build(); // SecurityFilterChain 반환
    }
}