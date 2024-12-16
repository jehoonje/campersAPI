// src/main/java/com/campers/entity/User.java
package com.campers.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(unique = true, nullable = true)
    private String email;

    @Builder.Default
    private boolean emailVerified = false;

    private String password;

    @Column(unique = true)
    private String kakaoId;

    private String nickname;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "profile_image_url") // 필드 이름 변경
    private String profileImageUrl; // 필드 이름 변경

    // 필요 시 다른 필드 추가

    // Update 메서드 수정
    public User update(String name, String profileImageUrl) {
        this.userName = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }
}
