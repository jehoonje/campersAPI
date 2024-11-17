package com.campers.dto;

import com.campers.entity.Favorite;
import com.campers.entity.MarkerType;

public class FavoriteRequest {
    private Long userId;
    private MarkerType markerType;
    private Long markerId;

    // Getters and Setters

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
