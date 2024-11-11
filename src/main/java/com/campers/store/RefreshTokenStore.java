// src/main/java/com/campers/store/RefreshTokenStore.java

package com.campers.store;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {
    private ConcurrentHashMap<String, String> refreshTokenMap = new ConcurrentHashMap<>();

    /**
     * Refresh Token 저장
     *
     * @param email        사용자 이메일
     * @param refreshToken Refresh Token
     */
    public void storeRefreshToken(String email, String refreshToken) {
        refreshTokenMap.put(email, refreshToken);
    }

    /**
     * Refresh Token 조회
     *
     * @param email 사용자 이메일
     * @return 저장된 Refresh Token
     */
    public String getRefreshToken(String email) {
        return refreshTokenMap.get(email);
    }

    /**
     * Refresh Token 삭제
     *
     * @param email 사용자 이메일
     */
    public void removeRefreshToken(String email) {
        refreshTokenMap.remove(email);
    }
}
