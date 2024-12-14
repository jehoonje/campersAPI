// src/main/java/com/campers/controller/CampgroundController.java
package com.campers.controller;

import com.campers.entity.Campground;
import com.campers.service.CampgroundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campgrounds")
@CrossOrigin(origins = "*") // CORS 설정
public class CampgroundController {

    private static final Logger logger = LoggerFactory.getLogger(CampgroundController.class);

    @Autowired
    private CampgroundService campgroundService;

    @GetMapping
    public List<Campground> getAllCampgrounds() {
        return campgroundService.getAllCampgrounds();
    }

    @GetMapping("/{id}")
    public Campground getCampgroundById(@PathVariable Long id) {
        logger.debug("Received request to get campground with id: {}", id);
        Campground campground = campgroundService.getCampgroundById(id);
        if (campground == null) {
            throw new ResourceNotFoundException("캠핑장을 찾을 수 없습니다. ID: " + id);
        }
        return campground;
    }

    // 캠핑장 데이터를 즉시 업데이트하는 엔드포인트 추가
    @PostMapping("/update")
    public String updateCampgroundsData() {
        campgroundService.updateCampgroundsDataImmediately();
        return "캠핑장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
