// src/main/java/com/campers/service/CampgroundService.java
package com.campers.service;

import com.campers.dto.CampgroundDTO; // 추가: DTO 클래스 임포트
import com.campers.entity.Campground;
import com.campers.repository.CampgroundRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampgroundService {

    @Autowired
    private CampgroundRepository campgroundRepository;

    // 외부 설정에서 서비스 키 주입
    @Value("${api.service-key}")
    private String serviceKey;

    @Value("${api.mobile-os}")
    private String mobileOs;

    @Value("${api.mobile-app}")
    private String mobileApp;

    @Value("${api.response-type}")
    private String responseType;

    private final HttpClient httpClient = HttpClient.newHttpClient();

//    // 애플리케이션 시작 시 자동으로 데이터 업데이트 실행
//    @PostConstruct
//    public void init() {
//        updateCampgroundsDataImmediately();
//    }

    // 캠핑장 데이터를 업데이트하는 메서드 (스케줄러로 사용할 수 있음)
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
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

        // 캠핑장 DTO 목록을 가져오는 메서드
        List<CampgroundDTO> campgroundDTOs = getCampgroundDTOs();
        System.out.println("캠핑장 DTO 목록 가져옴: " + campgroundDTOs.size() + "개");

        for (CampgroundDTO dto : campgroundDTOs) {
            String name = dto.getName();
            Double lat = dto.getLocation().getLat();
            Double lng = dto.getLocation().getLng();

            try {
                // 키워드를 3글자로 설정 (API 요구사항에 따라 조정)
                String keyword = name.length() >= 3 ? name.substring(0, 3) : name;
                // 공백이 포함된 키워드를 적절히 처리
                // 키워드를 캠핑장 이름 전체로 설정하되, 최소 3글자 이상
//                if (name.length() < 3) {
//                    System.out.println("캠핑장 이름이 3글자 미만이어서 건너뜀: " + name);
//                    continue; // 이름이 3글자 미만이면 건너뜀
//                }
                String trimmedkeyword = keyword.trim();

                System.out.println("트림된 키워드: " + trimmedkeyword);

                // 검색 API 호출 URL
                String searchUrl = "http://apis.data.go.kr/B551011/KorService1/searchKeyword1";

                System.out.println("캠핑장 검색 API 호출 중: " + name);

                // URI 빌더로 최종 URL 생성 (추가 인코딩 방지)
                String finalUrl = UriComponentsBuilder.fromHttpUrl(searchUrl)
                        .queryParam("serviceKey", serviceKey) // 인코딩 제거
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("keyword", trimmedkeyword)
                        .queryParam("contentTypeId", 12)
                        .queryParam("cat1", "A01") // 추가: 대분류 코드 A01로 필터링
                        .queryParam("_type", responseType)
                        .build(false) // 추가 인코딩 방지
                        .toUriString();

                // 요청 URL 출력
                System.out.println("요청 URL: " + finalUrl);

                // HTTP 요청 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(finalUrl))
                        .header("Accept", responseType.equals("json") ? "application/json" : "application/xml")
                        .GET()
                        .build();

                // HTTP 요청 보내기 및 응답 받기
                HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("응답 상태 코드: " + httpResponse.statusCode());
                // 응답 본문을 출력하면 로그가 너무 길어질 수 있으므로 주석 처리
                // System.out.println("응답 본문: " + httpResponse.body());

                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                    ObjectMapper objectMapper;
                    JsonNode root;

                    if (responseType.equals("json")) {
                        objectMapper = new ObjectMapper();
                        root = objectMapper.readTree(httpResponse.body());
                    } else {
                        XmlMapper xmlMapper = new XmlMapper();
                        root = xmlMapper.readTree(httpResponse.body());
                    }

                    JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
                    System.out.println("itemsNode isArray: " + itemsNode.isArray());

                    // **수정된 부분: 대분류 코드 A01인 경우만 처리**
                    if (itemsNode.isArray() && itemsNode.size() > 0) {
                        JsonNode firstItem = itemsNode.get(0);
                        String contentId = firstItem.path("contentid").asText();
                        String cat1 = firstItem.path("cat1").asText();

                        // 대분류 코드가 A01인지 확인
                        if ("A01".equals(cat1)) {
                            Long contentIdLong;
                            try {
                                contentIdLong = Long.parseLong(contentId);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid contentId format: " + contentId);
                                continue; // 유효하지 않은 contentId면 건너뜀
                            }

                            // **중복 체크: latitude 또는 longitude가 일치하는지 확인**
                            if (campgroundRepository.existsByLatitudeOrLongitude(lat, lng)) {
                                System.out.println("이미 존재하는 캠핑장 (lat 또는 lng 일치), 건너뜀: contentId=" + contentId);
                                continue; // 이미 존재하면 건너뜀
                            }


                            Campground campground = fetchCampgroundDetails(contentId, dto);
                            if (campground != null) {
                                campgrounds.add(campground);
                            }
                        } else {
                            System.out.println("대분류 코드가 A01이 아님: " + cat1);
                        }
                    } else if (itemsNode.isObject()) {
                        String contentId = itemsNode.path("contentid").asText();
                        String cat1 = itemsNode.path("cat1").asText();

                        // 대분류 코드가 A01인지 확인
                        if ("A01".equals(cat1)) {
                            Campground campground = fetchCampgroundDetails(contentId, dto);
                            if (campground != null) {
                                campgrounds.add(campground);
                            }
                        } else {
                            System.out.println("대분류 코드가 A01이 아님: " + cat1);
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
    private Campground fetchCampgroundDetails(String contentId, CampgroundDTO dto) { // dto 추가
        try {
            String detailUrl = "http://apis.data.go.kr/B551011/KorService1/detailCommon1";

            // URI 빌더로 상세 정보 요청 URL 생성 (추가 인코딩 방지)
            String finalUrl = UriComponentsBuilder.fromHttpUrl(detailUrl)
                    .queryParam("serviceKey", serviceKey) // 인코딩 제거
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("contentId", contentId)
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("addrinfoYN", "Y") // 추가
                    .queryParam("overviewYN", "Y") // 추가
                    .queryParam("_type", responseType)
                    .build(false)
                    .toUriString();

            // 요청 URL 출력
            System.out.println("상세 정보 요청 URL: " + finalUrl);

            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(finalUrl))
                    .header("Accept", responseType.equals("json") ? "application/json" : "application/xml")
                    .GET()
                    .build();

            // HTTP 요청 보내기 및 응답 받기
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("상세 정보 응답 상태 코드: " + httpResponse.statusCode());
            // 상세 응답 본문 출력 주석 처리
            // System.out.println("상세 정보 응답 본문: " + httpResponse.body());

            if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                ObjectMapper objectMapper;
                JsonNode root;

                if (responseType.equals("json")) {
                    objectMapper = new ObjectMapper();
                    root = objectMapper.readTree(httpResponse.body());
                } else {
                    XmlMapper xmlMapper = new XmlMapper();
                    root = xmlMapper.readTree(httpResponse.body());
                }

                JsonNode itemNode = root.path("response").path("body").path("items").path("item");

                System.out.println("detail itemNode isArray: " + itemNode.isArray());

                if (itemNode.isArray() && itemNode.size() > 0) {
                    JsonNode detailItem = itemNode.get(0);
                    return createCampgroundFromDetailItem(detailItem, dto, contentId);
                } else if (itemNode.isObject()) {
                    JsonNode detailItem = itemNode;
                    return createCampgroundFromDetailItem(detailItem, dto, contentId);
                } else {
                    System.out.println("상세 정보 없음: contentId " + contentId);
                }
            } else {
                System.out.println("상세 정보 API 요청 실패: " + httpResponse.statusCode());
            }
        } catch (Exception e) {
            System.err.println("contentId " + contentId + "에 대한 상세 정보 가져오기 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // 상세 정보 아이템으로부터 Campground 객체 생성
    private Campground createCampgroundFromDetailItem(JsonNode detailItem, CampgroundDTO dto, String contentId) {
        String apiImageUrl = detailItem.path("firstimage").asText();
        String dtoImageUrl = dto.getImageUrl();

        if ((apiImageUrl != null && !apiImageUrl.isEmpty()) || (dtoImageUrl != null && !dtoImageUrl.isEmpty())) {
            Campground campground = new Campground();
            try {
                campground.setId(Long.parseLong(contentId));
            } catch (NumberFormatException e) {
                System.err.println("Invalid contentId format: " + contentId);
                return null;
            }

            // 위도와 경도 설정
            campground.setLatitude(dto.getLocation().getLat());
            campground.setLongitude(dto.getLocation().getLng());

            // 이미지 URL 설정
            if (dtoImageUrl != null && !dtoImageUrl.isEmpty()) {
                campground.setImageUrl(dtoImageUrl);
            } else {
                campground.setImageUrl(apiImageUrl);
            }

            campground.setName(dto.getName());

            // 주소 설정
            String addr1 = detailItem.path("addr1").asText();
            campground.setAddress(addr1);

            // description 설정
            if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
                campground.setDescription(dto.getDescription());
            } else {
                String overview = detailItem.path("overview").asText();
                campground.setDescription(overview);
            }

            // feature 설정
            if (dto.getFeature() != null && !dto.getFeature().isEmpty()) {
                campground.setFeature(dto.getFeature());
            } else {
                campground.setFeature("무료");
            }

            return campground;
        } else {
            System.out.println("이미지가 없어 저장하지 않습니다: contentId " + contentId);
            return null;
        }
    }


    // JSON 파일에서 캠핑장 DTO 목록을 읽어오는 메서드
    private List<CampgroundDTO> getCampgroundDTOs() {
        try {
            // JSON 파일을 리소스에서 읽어오기
            ClassPathResource resource = new ClassPathResource("data/campgrounds.json");
            InputStream inputStream = resource.getInputStream();

            // JSON 파싱 (Jackson ObjectMapper 사용)
            ObjectMapper mapper = new ObjectMapper();
            List<CampgroundDTO> campgrounds = mapper.readValue(inputStream, new TypeReference<List<CampgroundDTO>>() {});

            return campgrounds;

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
