// src/main/java/com/campers/entity/Review.java

package com.campers.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 작성자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 컨텐츠 타입 (e.g., "Campground", "Fishing", "Autocamp")
    @Column(nullable = false)
    private String contentType;

    // 컨텐츠 ID
    @Column(nullable = false)
    private Long contentId;

    // 리뷰 내용
    @Column(nullable = false)
    private String content;

    // 별점 (1~5)
    @Column(nullable = false)
    private int rating;

    // 리뷰 작성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 리뷰 수정 시간
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 기본 생성자
    public Review() {}

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

