package com.campers.controller;

import com.campers.entity.Campsite;
import com.campers.service.CampsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campsites")
@CrossOrigin(origins = "*")
public class CampsiteController {

    @Autowired
    private CampsiteService campsiteService;

    @GetMapping
    public List<Campsite> getAllCampsites() {
        return campsiteService.getAllCampsites();
    }

    @PostMapping("/update")
    public String updateCampsitesData() {
        campsiteService.updateCampsitesData();
        return "야영장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
