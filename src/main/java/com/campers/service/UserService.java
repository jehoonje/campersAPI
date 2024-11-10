// src/main/java/com/campers/service/UserService.java

package com.campers.service;

import com.campers.entity.User;
import com.campers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 이메일로 사용자 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 기타 사용자 관련 서비스 메서드...
}

