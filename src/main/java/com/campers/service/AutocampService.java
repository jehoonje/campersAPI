package com.campers.service;

import com.campers.entity.Autocamp;
import com.campers.repository.AutocampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutocampService {

    @Autowired
    private AutocampRepository autocampRepository;

    @Value("${api.service-key}")
    private String serviceKey;

    @PostConstruct
    public void init() {
        updateAutocampsData();
    }


//    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
//    public void scheduledUpdateAutocampsData() {
//        updateAutocampsData();
//    }

    public void updateAutocampsData() {
        try {
            String keyword = "오토캠핑";
            String apiUrl = "http://apis.data.go.kr/B551011/KorService1/searchKeyword1"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=337"
                    + "&pageNo=1"
                    + "&MobileOS=ETC"
                    + "&MobileApp=AppTest"
                    + "&keyword=" + URLEncoder.encode(keyword, "UTF-8")
                    + "&contentTypeId=28"
                    + "&cat1=A03"
                    + "&cat2=A0302"
                    + "&cat3=A03021700";

            List<Autocamp> autocamps = fetchAutocampsFromApi(apiUrl);

            // Save to database
            for (Autocamp autocamp : autocamps) {
                if (autocamp.getImage1() != null || autocamp.getImage2() != null) {
                    // Check for duplicates
                    if (autocampRepository.existsByLatAndLng(autocamp.getLat(), autocamp.getLng())) {
                        System.out.println("이미 존재하는 오토캠핑장, 건너뜀: contentId=" + autocamp.getContentId());
                        continue;
                    }

                    // Fetch overview and additional details
                    String overview = fetchAutocampOverview(autocamp.getContentId());
                    autocamp.setDescription(overview);

                    // Fetch detailIntro
                    fetchAutocampDetailIntro(autocamp);

                    // Fetch detailInfo
                    fetchAutocampDetailInfo(autocamp);

                    autocampRepository.save(autocamp);
                    System.out.println("오토캠핑장 저장 완료: " + autocamp.getTitle());
                } else {
                    System.out.println("이미지가 없는 오토캠핑장, 건너뜀: " + autocamp.getTitle());
                }
            }

            System.out.println("오토캠핑장 데이터 업데이트 완료.");
        } catch (Exception e) {
            System.err.println("오토캠핑장 데이터를 업데이트하는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Autocamp> fetchAutocampsFromApi(String apiUrl) throws Exception {
        List<Autocamp> autocamps = new ArrayList<>();

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

                Autocamp autocamp = new Autocamp();

                // Map fields
                autocamp.setContentId(Long.parseLong(getTagValue("contentid", eElement)));
                autocamp.setTitle(getTagValue("title", eElement));
                autocamp.setAddr(getTagValue("addr1", eElement));
                autocamp.setImage1(getTagValue("firstimage", eElement));
                autocamp.setImage2(getTagValue("firstimage2", eElement));

                String mapy = getTagValue("mapy", eElement);
                String mapx = getTagValue("mapx", eElement);
                if (mapy != null && mapx != null) {
                    autocamp.setLat(Double.parseDouble(mapy));
                    autocamp.setLng(Double.parseDouble(mapx));
                }

                autocamps.add(autocamp);
            }
        }

        return autocamps;
    }

    private String fetchAutocampOverview(Long contentId) throws Exception {
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

    private void fetchAutocampDetailIntro(Autocamp autocamp) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailIntro1"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=10"
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&contentId=" + autocamp.getContentId()
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

                autocamp.setChkpetleports(getTagValue("chkpetleports", eElement));
                autocamp.setInfocenterleports(getTagValue("infocenterleports", eElement));
                autocamp.setOpenperiod(getTagValue("openperiod", eElement));
                autocamp.setParkingfeeleports(getTagValue("parkingfeeleports", eElement));
                autocamp.setParkingleports(getTagValue("parkingleports", eElement));
                autocamp.setReservation(getTagValue("reservation", eElement));
                autocamp.setRestdateleports(getTagValue("restdateleports", eElement));
                autocamp.setUsetimeleports(getTagValue("usetimeleports", eElement));
            }
        }
    }

    private void fetchAutocampDetailInfo(Autocamp autocamp) throws Exception {
        String apiUrl = "https://apis.data.go.kr/B551011/KorService1/detailInfo1"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=100"
                + "&pageNo=1"
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&contentId=" + autocamp.getContentId()
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
                            autocamp.setCampingfee(infotext);
                            break;
                        case "부대시설":
                            autocamp.setFacilities(infotext);
                            break;
                        case "주요시설":
                            autocamp.setMainfacilities(infotext);
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

    // Retrieve all autocamps
    public List<Autocamp> getAllAutocamps() {
        return autocampRepository.findAll();
    }
}
