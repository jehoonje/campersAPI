package com.campers.entity;

import javax.persistence.*;

@Entity
public class Beach {
    @Id
    private Long contentId; // API의 contentid를 ID로 사용

    private String title; // 해수욕장 이름 (title)

    @Column(length = 500)
    private String addr; // 주소 (addr1)

    private String image1; // 이미지 URL1 (firstimage)
    private String image2; // 이미지 URL2 (firstimage2)

    private Double lat; // 위도 (mapy)
    private Double lng; // 경도 (mapx)

    @Column(columnDefinition = "TEXT")
    private String description; // 개요 (overview)

    // Getters and Setters

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
