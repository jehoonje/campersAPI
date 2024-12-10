// src/main/java/com/campers/service/KakaoOAuthService.java
package com.campers.service;

import com.campers.dto.KakaoUserResponse;
import com.campers.entity.User;
import com.campers.repository.RoleRepository;
import com.campers.repository.UserRepository;
import com.campers.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public Map<String, String> kakaoLogin(String kakaoAccessToken) throws JSONException {
        // 카카오 API를 통해 사용자 정보 가져오기
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 카카오 API 요구사항에 맞게 설정

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                KakaoUserResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            KakaoUserResponse kakaoUserResponse = response.getBody();
            if (kakaoUserResponse == null) {
                throw new RuntimeException("카카오 사용자 정보가 비어있습니다.");
            }

            Long kakaoId = kakaoUserResponse.getId();

            // 필요한 사용자 정보 추출
            KakaoUserResponse.KakaoAccount kakaoAccount = kakaoUserResponse.getKakaoAccount();
            String email = kakaoAccount != null ? kakaoAccount.getEmail() : null;
            String nickName = null;
            String profileImageURL = null;

            if (kakaoAccount != null) {
                KakaoUserResponse.Profile profile = kakaoAccount.getProfile();
                if (profile != null) {
                    nickName = profile.getNickname();
                    profileImageURL = profile.getProfileImageUrl();
                }
            }

            // 닉네임이 없을 경우 기본값 설정
            if (nickName == null || nickName.isEmpty()) {
                nickName = "카카오사용자";
            }

            // 이메일이 없는 경우 고유한 이메일 생성
            if (email == null || email.isEmpty()) {
                email = "kakao_" + kakaoId + "@kakao.com";
            }

            // 사용자 정보를 데이터베이스에 저장하거나 업데이트
            User user = userRepository.findByKakaoId(String.valueOf(kakaoId));
            if (user == null) {
                user = User.builder()
                        .kakaoId(String.valueOf(kakaoId))
                        .email(email)
                        .userName(nickName)
                        .profileImageUrl(profileImageURL != null ? profileImageURL : "")
                        .emailVerified(true)
                        .roles(Collections.singleton(roleRepository.findByName("ROLE_USER")
                                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"))))
                        .build();
                userRepository.save(user);
            } else {
                // 기존 사용자 정보 업데이트 (필요한 경우)
                boolean isUpdated = false;
                if (!email.equals(user.getEmail())) {
                    user.setEmail(email);
                    isUpdated = true;
                }
                if (!nickName.equals(user.getUserName())) {
                    user.setUserName(nickName);
                    isUpdated = true;
                }
                if (profileImageURL != null && !profileImageURL.equals(user.getProfileImageUrl())) {
                    user.setProfileImageUrl(profileImageURL);
                    isUpdated = true;
                }
                if (isUpdated) {
                    userRepository.save(user);
                }
            }

            // JWT 토큰 생성
            String jwtAccessToken = jwtTokenUtil.generateAccessToken(user.getEmail(), user.getId(), user.getUserName());
            String jwtRefreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtAccessToken);
            tokens.put("refreshToken", jwtRefreshToken);

            return tokens;
        } else {
            throw new RuntimeException("카카오 사용자 정보 조회 실패: " + response.getStatusCode());
        }
    }
}
