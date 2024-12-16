package com.campers.controller;

import com.campers.entity.Beach;
import com.campers.service.BeachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beaches")
@CrossOrigin(origins = "*")
public class BeachController {

    private static final Logger logger = LoggerFactory.getLogger(BeachController.class);


    @Autowired
    private BeachService beachService;

    @GetMapping
    public List<Beach> getAllBeaches() {
        return beachService.getAllBeaches();
    }

    @GetMapping("/{contentId}")
    public Beach getBeachByContentId(@PathVariable Long contentId) {
        logger.debug("Received request to get beach with id: {}", contentId);
        Beach beach = beachService.getBeachByContentId(contentId);
        if (beach == null) {
            throw new ResourceNotFoundException("해수욕장을 찾을 수 없습니다. ContentId: " + contentId);
        }
        return beach;
    }



    // 해수욕장 데이터를 즉시 업데이트하는 엔드포인트 추가
    @PostMapping("/update")
    public String updateBeachesData() {
        beachService.updateBeachesData();
        return "해수욕장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
