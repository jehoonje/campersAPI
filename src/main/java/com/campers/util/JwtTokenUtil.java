// src/main/java/com/campers/util/JwtTokenUtil.java

package com.campers.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration; // 예: 3600000 (1시간)

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration; // 예: 604800000 (7일)

    /**
     * Access Token 생성
     *
     * @param email 사용자 이메일
     * @return 생성된 Access Token
     */
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * Refresh Token 생성
     *
     * @param email 사용자 이메일
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * 토큰에서 이메일 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Expired JWT token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token 발급
     *
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(refreshToken)
                    .getBody();
            String email = claims.getSubject();
            return generateAccessToken(email);
        } catch (ExpiredJwtException e) {
            System.err.println("Expired Refresh Token: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            System.err.println("Invalid Refresh Token: " + e.getMessage());
            return null;
        }
    }
}
