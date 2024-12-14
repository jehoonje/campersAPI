// src/main/java/com/campers/config/SecurityConfig.java

package com.campers.config;

import com.campers.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // PasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // SecurityFilterChain 빈 등록하여 HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().and()
                .authorizeRequests()
                .antMatchers(
                        "/api/login",
                        "/api/signup",
                        "/api/request-verification-code",
                        "/api/verify-code",
                        "/api/campgrounds/**",
                        "/api/beaches/**",
                        "/api/campsites/**",
                        "/api/autocamps/**",
                        "/api/fishings/**",
                        "/api/check-email",
                        "/api/reviews/average/**",
                        "/api/reviews/**/**",
                        "/api/users/**/**",
                        "/uploads/**",
                        "/api/auth/**"
                ).permitAll()
                .antMatchers("/api/reviews/**").authenticated() // 리뷰 작성, 수정, 삭제는 인증 필요
                .antMatchers("/api/favorites/**").authenticated() // 즐겨찾기 관련 API는 인증 필요
                .anyRequest().authenticated();

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
