// src/main/java/com/campers/controller/UserController.java
package com.campers.controller;

import com.campers.entity.User;
import com.campers.service.UserService;
import com.campers.service.exception.DuplicateNicknameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    // 프로필 이미지 업로드 엔드포인트
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            String imagePath = userService.saveProfileImage(userId, imageFile);
            Map<String, String> response = new HashMap<>();
            response.put("profileImageUrl", imagePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 이미지 업로드 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestPart("userName") String userName,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            userService.updateUserProfile(userId, userName, imageFile);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException dive) {
            // DataIntegrityViolationException 발생 시 중복 닉네임 등 DB 제약 위반으로 판단
            if (isDuplicateKeyException(dive)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 닉네임 입니다.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB 제약 오류 발생");
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 업데이트 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 사용자 정보 가져오기 엔드포인트
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("userName", user.getUserName());
            response.put("email", user.getEmail());
            response.put("profileImageUrl", user.getProfileImageUrl());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{userId}/update")
    public ResponseEntity<String> updateUserName(
            @PathVariable Long userId,
            @RequestPart("userName") String userName,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            userService.updateUserProfile(userId, userName, imageFile);
            return ResponseEntity.ok("유저 이름이 성공적으로 업데이트되었습니다.");
        } catch (DataIntegrityViolationException dive) {
            if (isDuplicateKeyException(dive)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 닉네임 입니다.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB 제약 오류 발생");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("유저 이름 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataIntegrityViolationException이 중복 키(닉네임 중복)로 인한 것인지 판별하는 메서드
     */
    private boolean isDuplicateKeyException(DataIntegrityViolationException dive) {
        Throwable cause = dive.getRootCause();
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            String message = cause.getMessage();
            if (message != null && message.contains("Duplicate entry")) {
                return true;
            }
        }
        return false;
    }




    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<String> deleteAccount(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }
}
