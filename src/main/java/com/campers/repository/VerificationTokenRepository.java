// src/main/java/com/campers/repository/VerificationTokenRepository.java

package com.campers.repository;

import com.campers.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByEmail(String email);
    void deleteByEmail(String email);
}
