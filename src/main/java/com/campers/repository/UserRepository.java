// src/main/java/com/campers/repository/UserRepository.java
package com.campers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.campers.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
