// src/main/java/com/campers/service/CampgroundService.java
package com.campers.service;

import com.campers.entity.Campground;
import com.campers.repository.CampgroundRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CampgroundService {

    @Autowired
    private CampgroundRepository campgroundRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // 외부 설정에서 서비스 키 주입
    @Value("${api.service-key}")
    private String serviceKey;

    @Value("${api.mobile-os}")
    private String mobileOs;

    @Value("${api.mobile-app}")
    private String mobileApp;

    @Value("${api.response-type}")
    private String responseType;

    // 애플리케이션 시작 시 자동으로 데이터 업데이트 실행
    @PostConstruct
    public void init() {
        updateCampgroundsDataImmediately();
    }

    // 캠핑장 데이터를 업데이트하는 메서드 (스케줄러로 사용할 수 있음)
    // @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void updateCampgroundsData() {
        System.out.println("캠핑장 데이터 업데이트 시작...");
        List<Campground> campgrounds = fetchCampgroundsFromApi();
        System.out.println("가져온 캠핑장 데이터 수: " + campgrounds.size());
        campgroundRepository.saveAll(campgrounds);
        System.out.println("캠핑장 데이터가 성공적으로 업데이트되었습니다.");
    }

    // 즉시 캠핑장 데이터를 업데이트하는 메서드
    public void updateCampgroundsDataImmediately() {
        updateCampgroundsData();
    }

    // 외부 API에서 캠핑장 목록을 가져오는 메서드
    private List<Campground> fetchCampgroundsFromApi() {
        List<Campground> campgrounds = new ArrayList<>();

        // 캠핑장 이름 목록을 가져오는 메서드
        List<String> campgroundNames = getCampgroundNames();
        System.out.println("캠핑장 이름 목록 가져옴: " + campgroundNames.size() + "개");

        for (String name : campgroundNames) {
            try {
                // 키워드를 2글자로 설정
                String keyword = name.length() >= 2 ? name.substring(0, 2) : name;

                // 검색 API 호출 URL
                String searchUrl = "http://apis.data.go.kr/B551011/KorService1/searchKeyword1";

                System.out.println("캠핑장 검색 API 호출 중: " + name);

                // URI 빌더로 최종 URL 생성 (추가 인코딩 방지)
                String finalUrl = UriComponentsBuilder.fromHttpUrl(searchUrl)
                        .queryParam("serviceKey", serviceKey) // 인코딩되지 않은 키 사용
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("keyword", keyword)
                        .queryParam("contentTypeId", 12)
                        .queryParam("_type", responseType)
                        .build(false) // 추가 인코딩 방지
                        .toUriString();

                // 요청 URL 출력
                System.out.println("요청 URL: " + finalUrl);

                // 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", responseType.equals("json") ? "application/json" : "application/xml");

                // HttpEntity에 헤더 추가
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // exchange 메서드를 사용하여 요청
                ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);
                System.out.println("응답 헤더: " + response.getHeaders());
                System.out.println("응답 상태 코드: " + response.getStatusCode());
                System.out.println("응답 본문: " + response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    if (responseType.equals("json")) {
                        // JSON 응답 처리 (생략)
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode root = objectMapper.readTree(response.getBody());

                        JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
                        System.out.println("itemsNode isArray: " + itemsNode.isArray());

                        if (itemsNode.isArray()) {
                            for (JsonNode item : itemsNode) {
                                String contentId = item.path("contentid").asText();
                                Campground campground = fetchCampgroundDetails(contentId);
                                if (campground != null) {
                                    campgrounds.add(campground);
                                }
                            }
                        }
                    } else {
                        // XML 응답 처리
                        XmlMapper xmlMapper = new XmlMapper();
                        JsonNode root = xmlMapper.readTree(response.getBody());

                        JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
                        System.out.println("itemsNode isArray: " + itemsNode.isArray());

                        if (itemsNode.isArray()) {
                            for (JsonNode item : itemsNode) {
                                String contentId = item.path("contentid").asText();
                                Campground campground = fetchCampgroundDetails(contentId);
                                if (campground != null) {
                                    campgrounds.add(campground);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(name + "에 대한 데이터 가져오기 오류: ");
                e.printStackTrace();
            }
        }
        return campgrounds;
    }

    // 특정 캠핑장의 상세 정보를 가져오는 메서드 (XML 및 JSON 파싱 지원)
    private Campground fetchCampgroundDetails(String contentId) {
        try {
            String detailUrl = "http://apis.data.go.kr/B551011/KorService1/detailCommon1";

            // URI 빌더로 상세 정보 요청 URL 생성 (추가 인코딩 방지)
            String finalUrl = UriComponentsBuilder.fromHttpUrl(detailUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("contentId", contentId)
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("_type", responseType)
                    .build(false)
                    .toUriString();

            // 요청 URL 출력
            System.out.println("상세 정보 요청 URL: " + finalUrl);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", responseType.equals("json") ? "application/json" : "application/xml");

            // HttpEntity에 헤더 추가
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // exchange 메서드를 사용하여 요청
            ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, String.class);

            System.out.println("상세 정보 응답 상태 코드: " + response.getStatusCode());
            System.out.println("상세 정보 응답 본문: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                if (responseType.equals("json")) {
                    // JSON 응답 처리
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(response.getBody());

                    JsonNode itemNode = root.path("response").path("body").path("items").path("item");

                    System.out.println("detail itemNode isArray: " + itemNode.isArray());

                    if (itemNode.isArray() && itemNode.size() > 0) {
                        JsonNode detailItem = itemNode.get(0);
                        return createCampgroundFromDetailItem(detailItem, contentId);
                    } else if (itemNode.isObject()) {
                        JsonNode detailItem = itemNode;
                        return createCampgroundFromDetailItem(detailItem, contentId);
                    } else {
                        System.out.println("상세 정보 없음: contentId " + contentId);
                    }
                } else {
                    // XML 응답 처리
                    XmlMapper xmlMapper = new XmlMapper();
                    JsonNode root = xmlMapper.readTree(response.getBody());

                    JsonNode itemNode = root.path("response").path("body").path("items").path("item");

                    System.out.println("detail itemNode isArray: " + itemNode.isArray());

                    if (itemNode.isArray() && itemNode.size() > 0) {
                        JsonNode detailItem = itemNode.get(0);
                        return createCampgroundFromDetailItem(detailItem, contentId);
                    } else if (itemNode.isObject()) {
                        JsonNode detailItem = itemNode;
                        return createCampgroundFromDetailItem(detailItem, contentId);
                    } else {
                        System.out.println("상세 정보 없음: contentId " + contentId);
                    }
                }
            } else {
                System.out.println("상세 정보 API 요청 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("contentId " + contentId + "에 대한 상세 정보 가져오기 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // 상세 정보 아이템으로부터 Campground 객체 생성
    private Campground createCampgroundFromDetailItem(JsonNode detailItem, String contentId) {
        // 이미지 URL 가져오기
        String imageUrl = detailItem.path("firstimage").asText();
        System.out.println("상세 정보 이미지 URL: " + imageUrl);

        // 이미지가 있는 경우에만 Campground 객체 생성
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Campground campground = new Campground();
            try {
                campground.setId(Long.parseLong(contentId));
            } catch (NumberFormatException e) {
                System.err.println("Invalid contentId format: " + contentId);
                return null;
            }
            campground.setLatitude(detailItem.path("mapy").asDouble());
            campground.setLongitude(detailItem.path("mapx").asDouble());
            campground.setImageUrl(imageUrl);

            // 추가로 필요한 필드 설정 (필요 시 추가)
            // 예: campground.setName(detailItem.path("title").asText());

            return campground;
        } else {
            System.out.println("이미지가 없어 저장하지 않습니다: contentId " + contentId);
            return null;
        }
    }

    // JSON 파일에서 캠핑장 이름 목록을 읽어오는 메서드
    private List<String> getCampgroundNames() {
        try {
            // JSON 파일을 리소스에서 읽어오기
            ClassPathResource resource = new ClassPathResource("data/campgrounds.json");
            InputStream inputStream = resource.getInputStream();

            // JSON 파싱 (Jackson ObjectMapper 사용)
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> campgrounds = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

            // 캠핑장 이름 목록 추출
            return campgrounds.stream()
                    .map(campground -> (String) campground.get("name")) // "name" 필드에서 캠핑장 이름 가져오기
                    .collect(Collectors.toList()); // 리스트로 변환

        } catch (IOException e) {
            System.err.println("캠핑장 데이터를 불러오는 중 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 예외의 전체 스택 트레이스 출력
            return List.of(); // 오류 발생 시 빈 리스트 반환
        }
    }

    // 데이터베이스에서 모든 캠핑장 데이터를 가져오는 메서드
    public List<Campground> getAllCampgrounds() {
        return campgroundRepository.findAll();
    }
}
