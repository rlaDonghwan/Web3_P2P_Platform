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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetaMaskAuthenticationProvider metaMaskAuthenticationProvider;

    @Bean
    public AuthenticationManager authenticationManager() {
        // MetaMaskAuthenticationProvider를 사용하는 ProviderManager 생성
        return new ProviderManager(List.of(metaMaskAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/nonce/*").permitAll() // Nonce 엔드포인트는 허용
                        .requestMatchers("/login", "/auth/**", "/home").permitAll() // 로그인 및 인증 경로 접근 허용
                        .anyRequest().authenticated()) // 그 외 요청은 인증 필요
                .formLogin(form -> form
                        .loginPage("/login") // 로그인 페이지 경로
                        .defaultSuccessUrl("/home", true) // 로그인 성공 시 홈으로
                        .failureUrl("/login?error=true") // 로그인 실패 시
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/login?logout=true") // 로그아웃 성공 시
                        .permitAll())
                .csrf().disable(); // 개발 중에는 CSRF 비활성화

        return http.build();
    }

    private MetaMaskAuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        MetaMaskAuthenticationFilter filter = new MetaMaskAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(new MetaMaskAuthenticationSuccessHandler(userRepository));
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error=true"));
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        return filter;
    }
}