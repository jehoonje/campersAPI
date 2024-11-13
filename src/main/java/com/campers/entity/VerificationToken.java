// src/main/java/com/campers/entity/VerificationToken.java

package com.campers.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String verificationCode;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    private boolean isVerified = false; // 이메일 인증 완료 여부

    // Getter와 Setter

    public Long getId() {
        return id;
    }

    // ID는 자동 생성되므로 Setter는 생략 가능

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
