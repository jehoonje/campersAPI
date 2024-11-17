package com.campers.dto;

import com.campers.entity.MarkerType;

public class FavoriteResponse {
    private boolean isFavorite;
    private Long id;
    private Long userId;
    private MarkerType markerType;
    private Long markerId;

    // Constructors

    public FavoriteResponse() {}

    // 상태 확인용 생성자
    public FavoriteResponse(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    // 엔티티 변환용 생성자
    public FavoriteResponse(com.campers.entity.Favorite favorite) {
        this.id = favorite.getId();
        this.userId = favorite.getUserId();
        this.markerType = favorite.getMarkerType();
        this.markerId = favorite.getMarkerId();
        this.isFavorite = true;
    }

    // Getters and Setters

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Long getId() {
        return id;
    }

    // 기타 Getters and Setters 생략
    public void setId(Long id) {
        this.id = id;
    }

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
}
