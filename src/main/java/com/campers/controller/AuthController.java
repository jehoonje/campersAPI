package com.campers.controller;

import com.campers.entity.Role;
import com.campers.entity.User;
import com.campers.entity.VerificationToken;
import com.campers.repository.RoleRepository;
import com.campers.repository.UserRepository;
import com.campers.repository.VerificationTokenRepository;
import com.campers.service.KakaoOAuthService;
import com.campers.util.JwtTokenUtil;
import com.campers.store.RefreshTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;

    public AuthController(KakaoOAuthService kakaoOAuthService) {
        this.kakaoOAuthService = kakaoOAuthService;
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    // 랜덤한 6자리 숫자로 고유한 userName 생성
    private String generateUniqueUserName() {
        Random random = new Random();
        String userName;
        int maxAttempts = 5;
        int attempts = 0;

        do {
            int randomNumber = 100000 + random.nextInt(900000); // 100000 ~ 999999 사이의 숫자
            userName = "user" + randomNumber;
            attempts++;
            // 중복되지 않을 때까지 시도
        } while (userRepository.existsByUserName(userName) && attempts < maxAttempts);

        if (userRepository.existsByUserName(userName)) {
            throw new RuntimeException("고유한 사용자 이름을 생성할 수 없습니다.");
        }

        return userName;
    }

    // 비밀번호 유효성 검사 메서드
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) return false;
        if (!password.matches(".*[a-zA-Z].*")) return false;
        if (!password.matches(".*[0-9].*")) return false;
        return true;
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

        // 이메일 인증 여부 확인
        VerificationToken token = verificationTokenRepository.findByEmail(email);
        if (token == null || !token.isVerified()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 인증이 완료되지 않았습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(email) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이미 가입된 이메일입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 랜덤한 6자리 숫자로 고유한 userName 생성
        String userName = generateUniqueUserName();

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .userName(userName)
                .emailVerified(true)
                .profileImageUrl("") // 필드 이름 수정
                .roles(Collections.singleton(roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("ROLE_USER not found"))))
                .build();

        userRepository.save(newUser);

        // VerificationToken 삭제
        verificationTokenRepository.delete(token);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        System.out.println("로그인 시도 - 이메일: " + email);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일과 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        User existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            System.out.println("사용자를 찾을 수 없음");
        } else {
            System.out.println("사용자 발견 - 이메일 인증 여부: " + existingUser.isEmailVerified());
            System.out.println("비밀번호 일치 여부: " + passwordEncoder.matches(password, existingUser.getPassword()));
        }

        if (existingUser != null && passwordEncoder.matches(password, existingUser.getPassword())) {
            if (!existingUser.isEmailVerified()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "이메일 인증이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Access Token과 Refresh Token 생성
            String accessToken = jwtTokenUtil.generateAccessToken(existingUser.getEmail(), existingUser.getId(), existingUser.getUserName());
            String refreshToken = jwtTokenUtil.generateRefreshToken(existingUser.getEmail());

            System.out.println("Access Token 생성 완료: " + accessToken);
            System.out.println("Refresh Token 생성 완료: " + refreshToken);

            // Refresh Token 저장
            refreshTokenStore.storeRefreshToken(existingUser.getEmail(), refreshToken);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            System.out.println("로그인 성공 - 토큰 반환");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Refresh Token을 제공해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (jwtTokenUtil.validateToken(refreshToken)) {
            String email = jwtTokenUtil.getEmailFromToken(refreshToken);
            String storedRefreshToken = refreshTokenStore.getRefreshToken(email);
            if (refreshToken.equals(storedRefreshToken)) {
                String newAccessToken = jwtTokenUtil.refreshAccessToken(refreshToken);
                if (newAccessToken != null) {
                    // 새로운 Access Token 발급
                    Map<String, String> response = new HashMap<>();
                    response.put("accessToken", newAccessToken);
                    response.put("refreshToken", refreshToken); // 기존 Refresh Token 유지
                    return ResponseEntity.ok(response);
                }
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "유효하지 않은 Refresh Token입니다.");
        return ResponseEntity.status(401).body(response);
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if (email == null || email.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일을 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean isDuplicate = userRepository.findByEmail(email) != null || verificationTokenRepository.findByEmail(email) != null;
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }

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

        // VerificationToken 엔티티 생성 및 저장
        VerificationToken token = new VerificationToken();
        token.setEmail(email);
        token.setVerificationCode(verificationCode);

        // 만료 시간 설정
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        token.setExpiryDate(calendar.getTime());

        // 이메일 발송
        sendVerificationEmail(email, verificationCode);

        // 데이터베이스에 저장
        verificationTokenRepository.save(token);

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

        VerificationToken token = verificationTokenRepository.findByEmail(email);
        if (token == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증 요청을 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 인증번호 만료 시간 확인
        Date now = new Date();
        if (token.getExpiryDate() == null || now.after(token.getExpiryDate())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증번호가 만료되었습니다. 다시 요청해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (token.getVerificationCode().equals(code)) {
            // 이메일 인증 상태 업데이트
            token.setVerified(true);
            verificationTokenRepository.save(token);

            Map<String, Boolean> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증번호가 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/resend-verification-code")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> payload) {
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

        // 새로운 인증번호 생성
        String verificationCode = generateVerificationCode();

        VerificationToken token = verificationTokenRepository.findByEmail(email);
        if (token == null) {
            token = new VerificationToken();
            token.setEmail(email);
        }
        token.setVerificationCode(verificationCode);

        // 만료 시간 재설정
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        token.setExpiryDate(calendar.getTime());

        // 이메일 재발송
        sendVerificationEmail(email, verificationCode);

        verificationTokenRepository.save(token);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    private void sendVerificationEmail(String email, String code) {
        String subject = "인증번호를 확인해주세요";
        String message = "<html><body>"
                + "<div style='max-width:600px;margin:auto;font-family:sans-serif;'>"
                + "<h1 style='color:#1e90ff;'>캠브릿지 이메일 인증</h1>"
                + "<p>안녕하세요,</p>"
                + "<p>아래의 인증번호를 앱에서 입력하여 이메일 인증을 완료해주세요:</p>"
                + "<div style='padding:20px;background-color:#f2f2f2;border-radius:8px;'>"
                + "<h2 style='text-align:center;'>" + code + "</h2>"
                + "</div>"
                + "<p style='color:#888;'>인증번호는 5분 후에 만료됩니다.</p>"
                + "<p>감사합니다.<br/>캠브릿지 팀 드림</p>"
                + "</div>"
                + "</body></html>";

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true); // HTML 컨텐츠 전송을 위해 true로 설정
            helper.setFrom("limjhoon8@naver.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 인증번호 생성
        return String.valueOf(code);
    }

    @PostMapping("/auth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> data) {
        String accessToken = data.get("accessToken");
        try {
            Map<String, String> tokens = kakaoOAuthService.kakaoLogin(accessToken);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            // 예외 로그 출력
            e.printStackTrace();

            // 클라이언트로 상세한 에러 메시지 반환
            Map<String, String> response = new HashMap<>();
            response.put("message", "카카오 로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
