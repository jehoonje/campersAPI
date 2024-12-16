package com.campers.controller;

import com.campers.entity.Campsite;
import com.campers.service.CampsiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campsites")
@CrossOrigin(origins = "*")
public class CampsiteController {

    private static final Logger logger = LoggerFactory.getLogger(CampsiteController.class);

    @Autowired
    private CampsiteService campsiteService;

    @GetMapping
    public List<Campsite> getAllCampsites() {
        return campsiteService.getAllCampsites();
    }

    @GetMapping("/{contentId}")
    public Campsite getCampsiteByContentId(@PathVariable Long contentId) {
        logger.debug("Received request to get campsite with id: {}", contentId);
        Campsite campsite = campsiteService.getCampsiteByContentId(contentId);
        if (campsite == null) {
            throw new ResourceNotFoundException("야영장을 찾을 수 없습니다. ContentId: " + contentId);
        }
        return campsite;
    }

    @PostMapping("/update")
    public String updateCampsitesData() {
        campsiteService.updateCampsitesData();
        return "야영장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
