package com.campers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 인식하지 못하는 필드 무시
public class CampgroundDTO {
    private String name;
    private Location location;
    private String address; // 추가
    private String description; // 추가

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAddress() { // 추가
        return address;
    }

    public void setAddress(String address) { // 추가
        this.address = address;
    }

    public String getDescription() { // 추가
        return description;
    }

    public void setDescription(String description) { // 추가
        this.description = description;
    }

    // 중첩 클래스 정의
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double lat;
        private Double lng;

        // Getters and Setters

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
    }
}
