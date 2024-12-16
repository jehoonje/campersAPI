package com.campers.controller;

import com.campers.entity.Fishing;
import com.campers.service.FishingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fishings")
public class FishingController {

    private static final Logger logger = LoggerFactory.getLogger(FishingController.class);


    @Autowired
    private FishingService fishingService;

    // 모든 낚시터 정보 조회
    @GetMapping
    public List<Fishing> getAllFishings() {
        return fishingService.getAllFishings();
    }

    @GetMapping("/{contentId}")
    public Fishing getFishingByContentId(@PathVariable Long contentId) {
        logger.debug("Received request to get fishing with id: {}", contentId);
        Fishing fishing = fishingService.getFishingByContentId(contentId);
        if (fishing == null) {
            throw new ResourceNotFoundException("낚시터를 찾을 수 없습니다. ContentId: " + contentId);
        }
        return fishing;
    }


    // 낚시터 데이터 업데이트 (수동으로 업데이트를 원할 경우)
    @PostMapping("/update")
    public String updateFishingData() {
        fishingService.updateFishingData();
        return "낚시터 데이터 업데이트가 시작되었습니다.";
    }
}
