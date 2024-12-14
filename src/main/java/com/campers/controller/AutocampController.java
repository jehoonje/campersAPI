package com.campers.controller;

import com.campers.entity.Autocamp;
import com.campers.service.AutocampService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autocamps")
@CrossOrigin(origins = "*")
public class AutocampController {

    private static final Logger logger = LoggerFactory.getLogger(AutocampController.class);


    @Autowired
    private AutocampService autocampService;

    @GetMapping
    public List<Autocamp> getAllAutocamps() {
        return autocampService.getAllAutocamps();
    }

    @GetMapping("/{contentId}")
    public Autocamp getAutocampByContentId(@PathVariable Long contentId) {
        logger.debug("Received request to get Autocamp with id: {}", contentId);
        Autocamp autocamp = autocampService.getAutocampByContentId(contentId);
        if (autocamp == null) {
            throw new ResourceNotFoundException("오토캠핑장을 찾을 수 없습니다. ContentId: " + contentId);
        }
        return autocamp;
    }


    @PostMapping("/update")
    public String updateAutocampsData() {
        autocampService.updateAutocampsData();
        return "오토캠핑장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
