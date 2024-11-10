// src/main/java/com/campers/dto/SignupRequest.java

package com.campers.dto;

public class SignupRequest {
    private String email;
    private String password;

    // 기본 생성자
    public SignupRequest() {}

    // Getter 및 Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
