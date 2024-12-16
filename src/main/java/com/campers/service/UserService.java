// src/main/java/com/campers/service/UserService.java

package com.campers.service;

import com.campers.entity.Role;
import com.campers.entity.User;
import com.campers.repository.RoleRepository;
import com.campers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final Path rootLocation = Paths.get("upload-dir");

    // 프로필 이미지 저장 메서드
    public String saveProfileImage(Long userId, MultipartFile imageFile) throws IOException {
        // 업로드 디렉토리 확인 및 생성
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 이미지 파일 이름 생성
        String fileName = "profile_" + userId + "_" + imageFile.getOriginalFilename();
        Path uploadPath = uploadDir.resolve(fileName);

        // 파일 저장
        Files.copy(imageFile.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

        // 이미지 URL 생성 (서버 주소에 맞게 수정)
        String imageUrl = "http://13.124.234.143:8080/uploads/" + fileName;

        return imageUrl;
    }

    public void updateUserProfile(Long userId, String userName, MultipartFile imageFile) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 유저 이름 업데이트
            user.setUserName(userName);

            // 프로필 이미지가 있는 경우 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                // 기존 이미지 파일 삭제 (선택 사항)
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    String existingFileName = user.getProfileImageUrl().replace("http://localhost:8080/uploads/", "");
                    Path existingFilePath = Paths.get("uploads").resolve(existingFileName);
                    Files.deleteIfExists(existingFilePath);
                }

                // 이미지 저장 로직 구현
                String imagePath = saveProfileImage(userId, imageFile);
                user.setProfileImageUrl(imagePath);
            }

            userRepository.save(user);
        } else {
            throw new RuntimeException("유저를 찾을 수 없습니다.");
        }
    }


    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public void assignRoleToUser(Long userId, String roleName) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findByName(roleName);

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            User user = userOpt.get();
            Role role = roleOpt.get();
            user.getRoles().add(role);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User or Role not found");
        }
    }

    // 이메일로 사용자 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 회원 삭제
    public void deleteUser(Long userId) throws Exception {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        } else {
            throw new Exception("User not found");
        }
    }

    // 유저 이름 업데이트
    public User updateUserName(Long userId, String newUserName) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));
        user.setUserName(newUserName);  // userName으로 업데이트
        return userRepository.save(user);
    }

    // 기타 사용자 관련 서비스 메서드...
}

