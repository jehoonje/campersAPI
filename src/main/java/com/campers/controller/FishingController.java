package com.campers.controller;

import com.campers.entity.Fishing;
import com.campers.service.FishingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fishings")
public class FishingController {

    @Autowired
    private FishingService fishingService;

    // 모든 낚시터 정보 조회
    @GetMapping
    public List<Fishing> getAllFishings() {
        return fishingService.getAllFishings();
    }

    // 낚시터 데이터 업데이트 (수동으로 업데이트를 원할 경우)
    @PostMapping("/update")
    public String updateFishingData() {
        fishingService.updateFishingData();
        return "낚시터 데이터 업데이트가 시작되었습니다.";
    }
}
