package com.campers.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "marker_type", "marker_id"})
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "marker_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MarkerType markerType;


    @Column(name = "marker_id", nullable = false)
    private Long markerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public Favorite() {
        this.createdAt = LocalDateTime.now();
    }

    // 생성자
    public Favorite(Long userId, MarkerType markerType, Long markerId) {
        this.userId = userId;
        this.markerType = markerType;
        this.markerId = markerId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    // ID는 자동 생성되므로 setter는 필요 없습니다.

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MarkerType getMarkerType() {
        return markerType;
    }

    public void setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
    }

    public Long getMarkerId() {
        return markerId;
    }

    public void setMarkerId(Long markerId) {
        this.markerId = markerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
