package com.campers.controller;

import com.campers.entity.Autocamp;
import com.campers.service.AutocampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autocamps")
@CrossOrigin(origins = "*")
public class AutocampController {

    @Autowired
    private AutocampService autocampService;

    @GetMapping
    public List<Autocamp> getAllAutocamps() {
        return autocampService.getAllAutocamps();
    }

    @PostMapping("/update")
    public String updateAutocampsData() {
        autocampService.updateAutocampsData();
        return "오토캠핑장 데이터가 성공적으로 업데이트되었습니다.";
    }
}
