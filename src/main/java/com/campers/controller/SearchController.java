// 예: SearchController.java
package com.campers.controller;

import com.campers.repository.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchController {

    private final CampgroundRepository campgroundRepository;
    private final CampsiteRepository campsiteRepository;
    private final AutocampRepository autocampRepository;
    private final FishingRepository fishingRepository;
    private final BeachRepository beachRepository;

    public SearchController(CampgroundRepository campgroundRepository, CampsiteRepository campsiteRepository, AutocampRepository autocampRepository,
                            FishingRepository fishingRepository, BeachRepository beachRepository) {
        this.campgroundRepository = campgroundRepository;
        this.campsiteRepository = campsiteRepository;
        this.autocampRepository = autocampRepository;
        this.fishingRepository = fishingRepository;
        this.beachRepository = beachRepository;
    }

    @GetMapping("/api/search")
    public List<SearchResultDto> search(@RequestParam String query) {
        List<SearchResultDto> results = new ArrayList<>();

        // campground 검색
        campgroundRepository.findByNameContainingOrAddressContaining(query, query).forEach(c -> {
            SearchResultDto dto = new SearchResultDto(c.getId(), c.getName(), c.getAddress(), "campground");
            results.add(dto);
        });

        // Campsite 검색
        campsiteRepository.findByTitleContainingOrAddrContaining(query, query).forEach(c -> {
            SearchResultDto dto = new SearchResultDto(c.getContentId(), c.getTitle(), c.getAddr(), "campsite");
            results.add(dto);
        });

        // Autocamp 검색
        autocampRepository.findByTitleContainingOrAddrContaining(query, query).forEach(a -> {
            SearchResultDto dto = new SearchResultDto(a.getContentId(), a.getTitle(), a.getAddr(), "autocamp");
            results.add(dto);
        });

        // Fishing 검색
        fishingRepository.findByTitleContainingOrAddrContaining(query, query).forEach(f -> {
            SearchResultDto dto = new SearchResultDto(f.getContentId(), f.getTitle(), f.getAddr(), "fishing");
            results.add(dto);
        });

        // Beach 검색
        beachRepository.findByTitleContainingOrAddrContaining(query, query).forEach(b -> {
            SearchResultDto dto = new SearchResultDto(b.getContentId(), b.getTitle(), b.getAddr(), "beach");
            results.add(dto);
        });

        return results;
    }

    // DTO 클래스
    static class SearchResultDto {
        private Long contentId;
        private String title;
        private String addr;
        private String type;

        public SearchResultDto(Long contentId, String title, String addr, String type) {
            this.contentId = contentId;
            this.title = title;
            this.addr = addr;
            this.type = type;
        }

        // getters & setters
        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAddr() { return addr; }
        public void setAddr(String addr) { this.addr = addr; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

}
