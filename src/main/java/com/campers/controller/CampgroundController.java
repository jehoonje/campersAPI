// src/main/java/com/campers/controller/CampgroundController.java
package com.campers.controller;

import com.campers.entity.Campground;
import com.campers.service.CampgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campgrounds")
@CrossOrigin(origins = "*") // CORS 설정
public class CampgroundController {

    @Autowired
    private CampgroundService campgroundService;

    @GetMapping
    public List<Campground> getAllCampgrounds() {
        return campgroundService.getAllCampgrounds();
    }

    // 캠핑장 데이터를 즉시 업데이트하는 엔드포인트 추가
    @PostMapping("/update")
    public String updateCampgroundsData() {
        campgroundService.updateCampgroundsDataImmediately();
        return "캠핑장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
