// src/main/java/com/campers/util/JwtTokenUtil.java

package com.campers.util;

import com.campers.entity.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.campers.entity.User;
import io.jsonwebtoken.Claims;
import com.campers.repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration; // 예: 3600000 (1시간)

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration; // 예: 604800000 (7일)

    @Autowired
    private UserRepository userRepository;

    /**
     * Access Token 생성
     *
     * @param email  사용자 이메일
     * @param userId 사용자 ID
     * @return 생성된 Access Token
     */
    public String generateAccessToken(String email, Long userId, String userName) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName) // Role 엔티티의 이름을 가져오는 메서드
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("userName", userName)
                .claim("roles", roles) // 역할 정보 추가
                .setIssuedAt(new Date(System.currentTimeMillis()))
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
                .setIssuedAt(new Date(System.currentTimeMillis()))
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

            // email로 userId 조회
            User user = userRepository.findByEmail(email);
            if (user == null) {
                System.err.println("User not found with email: " + email);
                return null;
            }
            Long userId = user.getId();
            String userName = user.getUserName();

            return generateAccessToken(email, userId, userName);
        } catch (ExpiredJwtException e) {
            System.err.println("Expired Refresh Token: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            System.err.println("Invalid Refresh Token: " + e.getMessage());
            return null;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }


    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        List<String> roles = claims.get("roles", List.class);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
