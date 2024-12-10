// src/main/java/com/campers/service/GoogleOAuthService.java
package com.campers.service;

import com.campers.entity.Role;
import com.campers.entity.User;
import com.campers.repository.RoleRepository;
import com.campers.repository.UserRepository;
import com.campers.util.JwtTokenUtil;
import com.campers.store.RefreshTokenStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GoogleOAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Value("${google.client.id}")
    private String clientId;

    public Optional<User> verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // 사용자 정보 추출
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            if (email != null && emailVerified) {
                // 사용자 조회 또는 생성
                User user = userRepository.findByEmail(email);
                if (user == null) {
                    // 신규 사용자 생성
                    String userName = name != null ? name : generateUniqueUserName();
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

                    user = User.builder()
                            .email(email)
                            .password(null) // 소셜 로그인이므로 비밀번호 없음
                            .userName(userName)
                            .emailVerified(true)
                            .profileImageUrl(pictureUrl != null ? pictureUrl : "")
                            .roles(Collections.singleton(roleRepository.findByName("ROLE_USER")
                                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"))))
                            .build();

                    userRepository.save(user);
                }

                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private String generateUniqueUserName() {
        // 유니크한 사용자 이름 생성 로직
        int randomNumber = (int) (Math.random() * 900000) + 100000; // 100000 ~ 999999
        String userName = "user" + randomNumber;
        while (userRepository.existsByUserName(userName)) {
            randomNumber = (int) (Math.random() * 900000) + 100000;
            userName = "user" + randomNumber;
        }
        return userName;
    }

    public Map<String, String> generateTokens(User user) {
        String accessToken = jwtTokenUtil.generateAccessToken(user.getEmail(), user.getId(), user.getUserName());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());

        // Refresh Token 저장
        refreshTokenStore.storeRefreshToken(user.getEmail(), refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }
}
