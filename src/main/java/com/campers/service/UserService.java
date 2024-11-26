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

    public String saveProfileImage(Long userId, MultipartFile file) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        String filename = "user_" + userId + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.rootLocation.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);

        User user = userOpt.get();
        user.setProfileImage(filename);
        userRepository.save(user);

        return filename;
    }

    public void updateUserProfile(Long userId, String userName, MultipartFile imageFile) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        user.setUserName(userName);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = "user_" + userId + "_" + imageFile.getOriginalFilename();
            Files.copy(imageFile.getInputStream(), this.rootLocation.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            user.setProfileImage(filename);
        }

        userRepository.save(user);
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

