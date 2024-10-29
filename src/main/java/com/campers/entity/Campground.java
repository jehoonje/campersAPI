package com.campers.entity;

import javax.persistence.*;

@Entity
public class Campground {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String feature = "무료";

    @Column(columnDefinition = "TEXT") // 데이터 타입 명시
    private String description; // overview 저장

    @Column(length = 500) // 길이 제한 설정 (예: VARCHAR(500))
    private String address; // addr1 저장

    // Getters and Setters

    public Long getId() {
        return id;
    }

    // ID가 JSON에 없으므로 setter 생략하거나 필요 시 추가
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() { // 추가
        return address;
    }

    public void setAddress(String address) { // 추가
        this.address = address;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }
}
