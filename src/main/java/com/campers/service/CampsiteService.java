package com.campers.service;

import com.campers.entity.Campsite;
import com.campers.repository.CampsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampsiteService {

    @Autowired
    private CampsiteRepository campsiteRepository;

    @Value("${api.service-key}")
    private String serviceKey;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void scheduledUpdateCampsitesData() {
        updateCampsitesData();
    }

    // 실행 함수
    public void updateCampsitesData() {
        try {
            String keyword = "야영장";
            String apiUrl = "http://apis.data.go.kr/B551011/KorService1/searchKeyword1"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=1000"
                    + "&pageNo=1"
                    + "&MobileOS=ETC"
                    + "&MobileApp=AppTest"
                    + "&keyword=" + URLEncoder.encode(keyword, "UTF-8")
                    + "&contentTypeId=28"
                    + "&cat1=A03"
                    + "&cat2=A0302"
                    + "&cat3=A03021700";

            List<Campsite> campsites = fetchCampsitesFromApi(apiUrl);

            // Save to database
            for (Campsite campsite : campsites) {
                if (campsite.getImage1() != null || campsite.getImage2() != null) {
                    // Check for duplicates
                    if (campsiteRepository.existsByLatAndLng(campsite.getLat(), campsite.getLng())) {
                        System.out.println("이미 존재하는 야영장, 건너뜀: contentId=" + campsite.getContentId());
                        continue;
                    }

                    // Fetch overview and additional details
                    String overview = fetchCampsiteOverview(campsite.getContentId());
                    campsite.setDescription(overview);

                    // Fetch detailIntro
                    fetchCampsiteDetailIntro(campsite);

                    // Fetch detailInfo
                    fetchCampsiteDetailInfo(campsite);

                    campsiteRepository.save(campsite);
                    System.out.println("야영장 저장 완료: " + campsite.getTitle());
                } else {
                    System.out.println("이미지가 없는 야영장, 건너뜀: " + campsite.getTitle());
                }
            }

            System.out.println("야영장 데이터 업데이트 완료.");
        } catch (Exception e) {
            System.err.println("야영장 데이터를 업데이트하는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 콘텐츠 아이디, 이미지와 기본정보 가져오기
    private List<Campsite> fetchCampsitesFromApi(String apiUrl) throws Exception {
        List<Campsite> campsites = new ArrayList<>();

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);

        InputStream is = conn.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("item");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                Campsite campsite = new Campsite();

                // Map fields
                campsite.setContentId(Long.parseLong(getTagValue("contentid", eElement)));
                campsite.setTitle(getTagValue("title", eElement));
                campsite.setAddr(getTagValue("addr1", eElement));
                campsite.setImage1(getTagValue("firstimage", eElement));
                campsite.setImage2(getTagValue("firstimage2", eElement));

                String mapy = getTagValue("mapy", eElement);
                String mapx = getTagValue("mapx", eElement);
                if (mapy != null && mapx != null) {
                    campsite.setLat(Double.parseDouble(mapy));
                    campsite.setLng(Double.parseDouble(mapx));
                }

                campsites.add(campsite);
            }
        }

        return campsites;
    }

    // 시설 개요
    private String fetchCampsiteOverview(Long contentId) throws Exception {
        String apiUrl = "https://apis.data.go.kr/B551011/KorService1/detailCommon1"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=10"
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&contentId=" + contentId
                + "&defaultYN=Y"
                + "&addrinfoYN=Y"
                + "&overviewYN=Y";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);

        InputStream is = conn.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("item");

        if (nList.getLength() > 0) {
            Node nNode = nList.item(0);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                return getTagValue("overview", eElement);
            }
        }

        return null;
    }

    // 시설 기본정보 가져오기
    private void fetchCampsiteDetailIntro(Campsite campsite) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailIntro1"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=10"
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&contentId=" + campsite.getContentId()
                + "&contentTypeId=28";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);

        InputStream is = conn.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("item");

        if (nList.getLength() > 0) {
            Node nNode = nList.item(0);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                campsite.setChkpetleports(getTagValue("chkpetleports", eElement));
                campsite.setInfocenterleports(getTagValue("infocenterleports", eElement));
                campsite.setOpenperiod(getTagValue("openperiod", eElement));
                campsite.setParkingfeeleports(getTagValue("parkingfeeleports", eElement));
                campsite.setParkingleports(getTagValue("parkingleports", eElement));
                campsite.setReservation(getTagValue("reservation", eElement));
                campsite.setRestdateleports(getTagValue("restdateleports", eElement));
                campsite.setUsetimeleports(getTagValue("usetimeleports", eElement));
            }
        }
    }

    // 시설 상세정보 가져오기
    private void fetchCampsiteDetailInfo(Campsite campsite) throws Exception {
        String apiUrl = "https://apis.data.go.kr/B551011/KorService1/detailInfo1"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=100"
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&contentId=" + campsite.getContentId()
                + "&contentTypeId=28";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setDoOutput(true);

        InputStream is = conn.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("item");

        // 각 item을 순회하며 infoname을 확인
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String infoname = getTagValue("infoname", eElement);
                String infotext = getTagValue("infotext", eElement);

                if (infoname != null && infotext != null) {
                    switch (infoname) {
                        case "이용요금":
                            campsite.setCampingfee(infotext);
                            break;
                        case "부대시설":
                            campsite.setFacilities(infotext);
                            break;
                        case "주요시설":
                            campsite.setMainfacilities(infotext);
                            break;
                        default:
                            // 필요한 경우 다른 infoname 처리
                            break;
                    }
                }
            }
        }
    }


    private String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag);

        if (nlList == null || nlList.getLength() == 0) {
            return null;
        }

        NodeList childNodes = nlList.item(0).getChildNodes();
        if (childNodes == null || childNodes.getLength() == 0) {
            return null;
        }

        Node nValue = childNodes.item(0);
        return nValue.getNodeValue();
    }

    // Retrieve all campsites
    public List<Campsite> getAllCampsites() {
        return campsiteRepository.findAll();
    }
}
