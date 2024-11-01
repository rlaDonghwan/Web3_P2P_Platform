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
        return new ProviderManager(List.of(metaMaskAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/nonce/*").permitAll()
                        .requestMatchers("/login", "/auth/**", "/home").permitAll()
                        .requestMatchers("/css/**", "/js/**").permitAll()  // 정적 리소스 접근 허용
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login") // 로그인 페이지 경로
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())
                .csrf().disable();

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