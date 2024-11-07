package com.inhatc.SafeCommerce.security;

import com.inhatc.SafeCommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 저장하고 조회하기 위한 레포지토리

    @Autowired
    private MetaMaskAuthenticationProvider metaMaskAuthenticationProvider; // MetaMask 인증을 처리하는 AuthenticationProvider

    // AuthenticationManager Bean 정의
    @Bean
    public AuthenticationManager authenticationManager() {
        // ProviderManager에 MetaMaskAuthenticationProvider를 추가하여 사용자 인증 처리
        return new ProviderManager(List.of(metaMaskAuthenticationProvider));
    }

    // SecurityFilterChain Bean 정의 (Spring Security의 보안 설정)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/nonce/*").permitAll()
                        .requestMatchers("/login", "/auth/**", "/home", "/addItem", "/items/*", "/cart/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login") // 로그인 페이지 경로 설정
                        .defaultSuccessUrl("/home", true) // 로그인 성공 시 이동할 기본 경로 설정
                        .failureUrl("/login?error=true") // 로그인 실패 시 이동할 경로 설정
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 경로 설정
                        .logoutSuccessUrl("/login?logout=true") // 로그아웃 성공 시 이동할 경로 설정
                        .permitAll())

                .csrf().disable(); // CSRF 보호 비활성화 (테스트나 외부 API 접근 시 주로 사용)

        return http.build(); // 보안 필터 체인을 반환
    }
}