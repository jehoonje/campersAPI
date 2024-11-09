package com.campers.service;

import com.campers.entity.Fishing;
import com.campers.repository.FishingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class FishingService {

    @Autowired
    private FishingRepository fishingRepository;

    @Value("${api.service-key}")
    private String serviceKey;

    // 초기화 시 데이터 업데이트 (필요 시 주석 해제)
//     @PostConstruct
//     public void init() {
//         updateFishingData();
//     }

    // 실행 함수
    public void updateFishingData() {
        try {
            String apiUrl = "http://apis.data.go.kr/B551011/KorService1/areaBasedList1"
                    + "?numOfRows=205"
                    + "&pageNo=1"
                    + "&MobileOS=ETC"
                    + "&MobileApp=AppTest"
                    + "&ServiceKey=" + serviceKey
                    + "&listYN=Y"
                    + "&arrange=A"
                    + "&contentTypeId=28"
                    + "&areaCode="
                    + "&sigunguCode="
                    + "&cat1=A03"
                    + "&cat2=A0303"
                    + "&cat3=A03030500";

            List<Fishing> fishings = fetchFishingsFromApi(apiUrl);

            // 데이터베이스에 저장
            for (Fishing fishing : fishings) {
                // 이미지 유효성 검사 (image1이 '문의'인 경우도 제외)
                if (isValidImage(fishing.getImage1()) && isValidImage(fishing.getImage2())) {
                    // 중복 검사
                    if (fishingRepository.existsByLatAndLng(fishing.getLat(), fishing.getLng())) {
                        System.out.println("이미 존재하는 낚시터, 건너뜀: contentId=" + fishing.getContentId());
                        continue;
                    }

                    // 추가 데이터 가져오기
                    String overview = fetchFishingOverview(fishing.getContentId());
                    fishing.setDescription(overview != null ? overview : "문의");

                    fetchFishingDetailIntro(fishing);
                    fetchFishingDetailInfo(fishing);
                    fetchFishingDetailImage(fishing);

                    // infocenterleports, restdateleports, usetimeleports, parkingleports 검사
                    if ("문의".equals(fishing.getInfocenterleports()) &&
                            "문의".equals(fishing.getRestdateleports()) &&
                            "문의".equals(fishing.getUsetimeleports()) &&
                            "문의".equals(fishing.getParkingleports())) {
                        System.out.println("기타 정보가 모두 문의인 낚시터, 건너뜀: contentId=" + fishing.getContentId());
                        continue;
                    }

                    fishingRepository.save(fishing);
                    System.out.println("낚시터 저장 완료: " + fishing.getTitle());
                } else {
                    System.out.println("유효하지 않은 이미지가 있는 낚시터, 건너뜀: " + fishing.getTitle());
                }
            }

            System.out.println("낚시터 데이터 업데이트 완료.");
        } catch (Exception e) {
            System.err.println("낚시터 데이터를 업데이트하는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 이미지 URL 유효성 검사
    private boolean isValidImage(String imageUrl) {
        return imageUrl != null && !imageUrl.trim().isEmpty() && !"문의".equals(imageUrl.trim());
    }

    // 초기 API로부터 기본 정보 가져오기
    private List<Fishing> fetchFishingsFromApi(String apiUrl) throws Exception {
        List<Fishing> fishings = new ArrayList<>();

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

                Fishing fishing = new Fishing();

                fishing.setContentId(parseLong(getTagValue("contentid", eElement)));
                fishing.setTitle(getTagValueOrDefault("title", eElement));
                fishing.setAddr(getTagValueOrDefault("addr1", eElement));
                fishing.setImage1(getTagValueOrDefault("firstimage", eElement));
                fishing.setImage2(getTagValueOrDefault("firstimage2", eElement));

                String mapy = getTagValue("mapy", eElement);
                String mapx = getTagValue("mapx", eElement);
                if (mapy != null && mapx != null) {
                    fishing.setLat(parseDouble(mapy));
                    fishing.setLng(parseDouble(mapx));
                } else {
                    fishing.setLat(0.0);
                    fishing.setLng(0.0);
                }

                fishings.add(fishing);
            }
        }

        return fishings;
    }

    // overview 가져오기
    private String fetchFishingOverview(Long contentId) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailCommon1"
                + "?ServiceKey=" + serviceKey
                + "&contentTypeId=28"
                + "&contentId=" + contentId
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&defaultYN=Y"
                + "&firstImageYN=Y"
                + "&areacodeYN=Y"
                + "&catcodeYN=Y"
                + "&addrinfoYN=Y"
                + "&mapinfoYN=Y"
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
                return getTagValueOrDefault("overview", eElement);
            }
        }

        return "문의";
    }

    // detailIntro1 API를 통해 추가 정보 가져오기
    private void fetchFishingDetailIntro(Fishing fishing) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailIntro1"
                + "?ServiceKey=" + serviceKey
                + "&contentTypeId=28"
                + "&contentId=" + fishing.getContentId()
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest";

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

                fishing.setInfocenterleports(getTagValueOrDefault("infocenterleports", eElement));
                fishing.setRestdateleports(getTagValueOrDefault("restdateleports", eElement));
                fishing.setUsetimeleports(getTagValueOrDefault("usetimeleports", eElement));
                fishing.setParkingleports(getTagValueOrDefault("parkingleports", eElement));
            }
        } else {
            // 값이 없을 경우 "문의"로 설정
            fishing.setInfocenterleports("문의");
            fishing.setRestdateleports("문의");
            fishing.setUsetimeleports("문의");
            fishing.setParkingleports("문의");
        }
    }

    // detailInfo1 API를 통해 fishingfee와 facilities 가져오기
    private void fetchFishingDetailInfo(Fishing fishing) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailInfo1"
                + "?ServiceKey=" + serviceKey
                + "&contentTypeId=28"
                + "&contentId=" + fishing.getContentId()
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest";

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

                String infoname = getTagValue("infoname", eElement);
                String infotext = getTagValueOrDefault("infotext", eElement);

                if (infoname != null && infotext != null) {
                    switch (infoname) {
                        case "이용요금":
                            fishing.setFishingfee(infotext);
                            break;
                        case "보유장비":
                            fishing.setFacilities(infotext);
                            break;
                        case "부대시설":
                            fishing.setFacilities(infotext);
                            break;
                        default:
                            // 필요한 경우 다른 infoname 처리
                            break;
                    }
                }
            }
        }
    }

    // detailImage1 API를 통해 추가 이미지 가져오기
    private void fetchFishingDetailImage(Fishing fishing) throws Exception {
        String apiUrl = "http://apis.data.go.kr/B551011/KorService1/detailImage1"
                + "?ServiceKey=" + serviceKey
                + "&contentId=" + fishing.getContentId()
                + "&MobileOS=ETC"
                + "&MobileApp=AppTest"
                + "&imageYN=Y"
                + "&subImageYN=Y"
                + "&numOfRows=10";

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

        int imageCount = 3; // image3부터 저장

        for (int temp = 0; temp < nList.getLength(); temp++) {
            if (imageCount > 5) break; // image5까지만 저장

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String originimgurl = getTagValueOrDefault("originimgurl", eElement);

                if (originimgurl != null && !originimgurl.trim().isEmpty()) {
                    switch (imageCount) {
                        case 3:
                            fishing.setImage3(originimgurl);
                            break;
                        case 4:
                            fishing.setImage4(originimgurl);
                            break;
                        case 5:
                            fishing.setImage5(originimgurl);
                            break;
                        default:
                            break;
                    }
                    imageCount++;
                }
            }
        }
    }

    // XML 태그 값 추출, null인 경우 "문의"로 대체
    private String getTagValueOrDefault(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag);

        if (nlList == null || nlList.getLength() == 0) {
            return "문의";
        }

        NodeList childNodes = nlList.item(0).getChildNodes();
        if (childNodes == null || childNodes.getLength() == 0) {
            return "문의";
        }

        Node nValue = childNodes.item(0);
        if (nValue == null || nValue.getNodeValue() == null || nValue.getNodeValue().trim().isEmpty()) {
            return "문의";
        }

        return nValue.getNodeValue();
    }

    // 특정 태그 값 가져오기, null일 경우 null 반환
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
        if (nValue == null || nValue.getNodeValue() == null || nValue.getNodeValue().trim().isEmpty()) {
            return null;
        }

        return nValue.getNodeValue();
    }

    // 문자열을 Long으로 변환, 실패 시 null 반환
    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    // 문자열을 Double로 변환, 실패 시 null 반환
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    // 모든 낚시터 조회
    public List<Fishing> getAllFishings() {
        return fishingRepository.findAll();
    }
}
