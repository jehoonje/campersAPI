package com.campers.entity;

import javax.persistence.*;

@Entity
@Table(name = "fishing")
public class Fishing {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentId;
    private String title;
    private String addr;
    private String image1;
    private String image2;
    private Double lat;
    private Double lng;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String infocenterleports;

    private String restdateleports;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String usetimeleports;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parkingleports;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String fishingfee;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String facilities;

    private String image3;
    private String image4;
    private String image5;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getInfocenterleports() {
        return infocenterleports;
    }

    public void setInfocenterleports(String infocenterleports) {
        this.infocenterleports = infocenterleports;
    }

    public String getRestdateleports() {
        return restdateleports;
    }

    public void setRestdateleports(String restdateleports) {
        this.restdateleports = restdateleports;
    }

    public String getUsetimeleports() {
        return usetimeleports;
    }

    public void setUsetimeleports(String usetimeleports) {
        this.usetimeleports = usetimeleports;
    }

    public String getParkingleports() {
        return parkingleports;
    }

    public void setParkingleports(String parkingleports) {
        this.parkingleports = parkingleports;
    }

    public String getFishingfee() {
        return fishingfee;
    }

    public void setFishingfee(String fishingfee) {
        this.fishingfee = fishingfee;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getImage5() {
        return image5;
    }

    public void setImage5(String image5) {
        this.image5 = image5;
    }

    // 기본 생성자
    public Fishing() {}

    // 필요한 생성자 및 메서드 추가 가능
}
