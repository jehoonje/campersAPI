// src/main/java/com/campers/controller/AuthController.java
package com.campers.controller;

import com.campers.entity.User;
import com.campers.repository.UserRepository;
import com.campers.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/request-verification-code")
    public ResponseEntity<?> requestVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if (email == null || email.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일을 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.findByEmail(email) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이미 사용 중인 이메일입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 인증번호 생성
        String verificationCode = generateVerificationCode();

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setVerificationCode(verificationCode);

        // 이메일 발송
        sendVerificationEmail(email, verificationCode);

        // 데이터베이스에 저장
        userRepository.save(newUser);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");

        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일과 인증번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        User existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일을 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (existingUser.getVerificationCode().equals(code)) {
            existingUser.setEmailVerified(true);
            userRepository.save(existingUser);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증번호가 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일과 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        User existingUser = userRepository.findByEmail(email);
        if (existingUser == null || !existingUser.isEmailVerified()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 인증이 완료되지 않았습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        existingUser.setPassword(passwordEncoder.encode(password));
        existingUser.setVerificationCode(null); // 인증번호 삭제
        userRepository.save(existingUser);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일과 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null && passwordEncoder.matches(password, existingUser.getPassword())) {
            if (!existingUser.isEmailVerified()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "이메일 인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            String token = jwtTokenUtil.generateToken(existingUser.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.status(401).body(response);
        }
    }

    private void sendVerificationEmail(String email, String code) {
        String subject = "인증번호를 확인해주세요";
        String message = "인증번호: " + code;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom("limjhoon8@naver.com"); // 실제 네이버 이메일 주소로 변경

        mailSender.send(mailMessage);
    }



    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 인증번호 생성
        return String.valueOf(code);
    }
}

class AuthResponse {
    private String token;

    public AuthResponse(String token) { this.token = token; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }
}
