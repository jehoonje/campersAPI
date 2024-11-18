// src/main/java/com/campers/filter/JwtAuthenticationFilter.java

package com.campers.filter;

import com.campers.store.RefreshTokenStore;
import com.campers.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import com.campers.service.CustomUserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshHeader = request.getHeader("Refresh-Token"); // Refresh Token 헤더

        String email = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtTokenUtil.getEmailFromToken(token);
            } catch (ExpiredJwtException e) {
                logger.warn("Access Token expired: " + e.getMessage());
                // Access Token 만료 시 Refresh Token을 사용하여 새로운 Access Token 발급 시도
                if (refreshHeader != null && !refreshHeader.isEmpty()) {
                    String refreshToken = refreshHeader;
                    if (jwtTokenUtil.validateToken(refreshToken)) {
                        String refreshEmail = jwtTokenUtil.getEmailFromToken(refreshToken);
                        String storedRefreshToken = refreshTokenStore.getRefreshToken(refreshEmail);
                        if (refreshToken.equals(storedRefreshToken)) {
                            String newAccessToken = jwtTokenUtil.refreshAccessToken(refreshToken);
                            if (newAccessToken != null) {
                                // 새로운 Access Token을 응답 헤더에 추가
                                response.setHeader("Authorization", "Bearer " + newAccessToken);
                                email = jwtTokenUtil.getEmailFromToken(newAccessToken);
                                token = newAccessToken; // 새로운 토큰을 사용하도록 설정
                            }
                        }
                    }
                }
            } catch (JwtException e) {
                logger.error("JWT parsing error: " + e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // JWT 토큰이 유효한 경우 인증 설정
            if (jwtTokenUtil.validateToken(token)) {
                List<GrantedAuthority> authorities = jwtTokenUtil.getAuthoritiesFromToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
