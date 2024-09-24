package com.study.controller;

import com.study.entity.CampingSite;
import com.study.service.CampingSiteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/campsites")
public class CampingSiteController {

    private final CampingSiteService campingSiteService;

    public CampingSiteController(CampingSiteService campingSiteService) {
        this.campingSiteService = campingSiteService;
    }

    @GetMapping
    public List<CampingSite> getAllCampingSites() {
        // 서비스에서 모든 캠핑장 정보를 가져와 반환
        return campingSiteService.getAllCampingSites();
    }
}
