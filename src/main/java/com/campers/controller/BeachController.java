package com.campers.controller;

import com.campers.entity.Beach;
import com.campers.service.BeachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beaches")
@CrossOrigin(origins = "*")
public class BeachController {

    @Autowired
    private BeachService beachService;

    @GetMapping
    public List<Beach> getAllBeaches() {
        return beachService.getAllBeaches();
    }

    // 해수욕장 데이터를 즉시 업데이트하는 엔드포인트 추가
    @PostMapping("/update")
    public String updateBeachesData() {
        beachService.updateBeachesData();
        return "해수욕장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
