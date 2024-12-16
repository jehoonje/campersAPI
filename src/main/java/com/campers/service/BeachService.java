package com.campers.service;

import com.campers.entity.Beach;
import com.campers.repository.BeachRepository;
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
public class BeachService {

    @Autowired
    private BeachRepository beachRepository;

    @Value("${api.service-key}")
    private String serviceKey;

//    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
//    public void scheduledUpdateBeachesData() {
//        updateBeachesData();
//    }

//    @PostConstruct
//    public void init() {
//        updateBeachesData();
//    }

    public void updateBeachesData() {
        try {
            // 1. API 호출하여 데이터 가져오기
            String apiUrl = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1"
                    + "?serviceKey=" + serviceKey
                    + "&contentTypeId=12"
                    + "&cat1=A01"
                    + "&cat2=A0101"
                    + "&cat3=A01011200"
                    + "&numOfRows=1000"
                    + "&pageNo=1"
                    + "&MobileOS=ETC"
                    + "&MobileApp=AppTest";

            List<Beach> beaches = fetchBeachesFromApi(apiUrl);

            // 2. 데이터베이스에 저장
            for (Beach beach : beaches) {
                // 이미지가 있는 경우만 저장
                if (beach.getImage1() != null || beach.getImage2() != null) {
                    // 중복 체크: 위도와 경도가 모두 일치하는 경우 건너뜀
                    if (beachRepository.existsByLatAndLng(beach.getLat(), beach.getLng())) {
                        System.out.println("이미 존재하는 해수욕장, 건너뜀: contentId=" + beach.getContentId());
                        continue;
                    }

                    // 상세 정보 가져오기 (overview)
                    String overview = fetchBeachOverview(beach.getContentId());
                    beach.setDescription(overview);

                    beachRepository.save(beach);
                    System.out.println("해수욕장 저장 완료: " + beach.getTitle());
                } else {
                    System.out.println("이미지가 없는 해수욕장, 건너뜀: " + beach.getTitle());
                }
            }

            System.out.println("해수욕장 데이터 업데이트 완료.");
        } catch (Exception e) {
            System.err.println("해수욕장 데이터를 업데이트하는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Beach> fetchBeachesFromApi(String apiUrl) throws Exception {
        List<Beach> beaches = new ArrayList<>();

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

                Beach beach = new Beach();

                // 필드 매핑
                beach.setContentId(Long.parseLong(getTagValue("contentid", eElement)));
                beach.setTitle(getTagValue("title", eElement));
                beach.setAddr(getTagValue("addr1", eElement));
                beach.setImage1(getTagValue("firstimage", eElement));
                beach.setImage2(getTagValue("firstimage2", eElement));
                beach.setLat(Double.parseDouble(getTagValue("mapy", eElement)));
                beach.setLng(Double.parseDouble(getTagValue("mapx", eElement)));

                beaches.add(beach);
            }
        }

        return beaches;
    }

    private String fetchBeachOverview(Long contentId) throws Exception {
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

                String overview = getTagValue("overview", eElement);
                return overview;
            }
        }

        return null;
    }

    private String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();

        if (nlList == null || nlList.getLength() == 0) {
            return null;
        }

        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    // 해수욕장 데이터 가져오기
    public List<Beach> getAllBeaches() {
        return beachRepository.findAll();
    }

    public Beach getBeachByContentId(Long contentId) {
        return beachRepository.findByContentId(contentId).orElse(null);
    }
}
