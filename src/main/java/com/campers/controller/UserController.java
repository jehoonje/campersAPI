// src/main/java/com/campers/controller/UserController.java
package com.campers.controller;

import com.campers.entity.User;
import com.campers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            response.put("profileImage", imagePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "프로필 이미지 업로드 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 사용자 정보 업데이트 엔드포인트
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestPart("userName") String userName,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            userService.updateUserProfile(userId, userName, imageFile);
            return ResponseEntity.ok().build();
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
            response.put("profileImage", user.getProfileImage());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 유저 이름 업데이트 엔드포인트
    @PutMapping("/{userId}/update")
    public ResponseEntity<String> updateUserName(
            @PathVariable Long userId,
            @RequestBody User updatedUser
    ) {
        try {
            // userId에 해당하는 유저가 존재하는지 확인하고, userName을 업데이트
            userService.updateUserName(userId, updatedUser.getUserName());
            return ResponseEntity.ok("유저 이름이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("유저 이름 업데이트 중 오류가 발생했습니다.");
        }
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
