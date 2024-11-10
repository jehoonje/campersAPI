// src/main/java/com/campers/dto/ReviewRequestDTO.java

package com.campers.dto;

public class ReviewRequestDTO {

    private String contentType;
    private Long contentId;
    private String content;
    private int rating;

    // 기본 생성자
    public ReviewRequestDTO() {}

    // Getters and Setters

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
}

