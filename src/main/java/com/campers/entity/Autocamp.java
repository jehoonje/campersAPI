package com.campers.entity;

import javax.persistence.*;

@Entity
public class Autocamp {

    @Id
    private Long contentId; // API의 contentid를 ID로 사용

    private String title; // 야영장 이름 (title)

    @Column(length = 500)
    private String addr; // 주소 (addr1)

    private String image1; // 이미지 URL1 (firstimage)
    private String image2; // 이미지 URL2 (firstimage2)
    private String image3; // 이미지 URL1 (firstimage)
    private String image4;
    private String image5;

    private Double lat; // 위도 (mapy)
    private Double lng; // 경도 (mapx)

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description; // 개요 (overview)

    private String chkpetleports; // 애완동물동반가능정보

    @Lob
    @Column(columnDefinition = "TEXT")
    private String infocenterleports; // 문의및안내

    private String openperiod; // 개장기간

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parkingfeeleports; // 주차요금

    private String parkingleports; // 주차시설

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reservation; // 예약안내

    private String restdateleports; // 쉬는날

    @Lob
    @Column(columnDefinition = "TEXT")
    private String usetimeleports; // 이용시간

    // 새로 추가된 필드들
    @Lob
    @Column(columnDefinition = "TEXT")
    private String campingfee; // 이용요금

    @Lob
    @Column(columnDefinition = "TEXT")
    private String facilities; // 부대시설

    @Lob
    @Column(columnDefinition = "TEXT")
    private String mainfacilities; // 주요시설

    // Getters and Setters
    // (Include getters and setters for all fields)


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

    public String getChkpetleports() {
        return chkpetleports;
    }

    public void setChkpetleports(String chkpetleports) {
        this.chkpetleports = chkpetleports;
    }

    public String getInfocenterleports() {
        return infocenterleports;
    }

    public void setInfocenterleports(String infocenterleports) {
        this.infocenterleports = infocenterleports;
    }

    public String getOpenperiod() {
        return openperiod;
    }

    public void setOpenperiod(String openperiod) {
        this.openperiod = openperiod;
    }

    public String getParkingfeeleports() {
        return parkingfeeleports;
    }

    public void setParkingfeeleports(String parkingfeeleports) {
        this.parkingfeeleports = parkingfeeleports;
    }

    public String getParkingleports() {
        return parkingleports;
    }

    public void setParkingleports(String parkingleports) {
        this.parkingleports = parkingleports;
    }

    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
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

    public String getCampingfee() {
        return campingfee;
    }

    public void setCampingfee(String campingfee) {
        this.campingfee = campingfee;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getMainfacilities() {
        return mainfacilities;
    }

    public void setMainfacilities(String mainfacilities) {
        this.mainfacilities = mainfacilities;
    }
}
