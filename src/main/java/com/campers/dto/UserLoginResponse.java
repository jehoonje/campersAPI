// src/main/java/com/campers/dto/UserLoginResponse.java
package com.campers.dto;

import com.campers.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    private Long userId;
    private String email;
    private String name;
    private String profileImageUrl; // 프로필 이미지 URL 추가
    private String accessToken;
    private String refreshToken;

    public static UserLoginResponse response(User user, String accessToken, String refreshToken) {
        return UserLoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getUserName())
                .profileImageUrl(user.getProfileImageUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
