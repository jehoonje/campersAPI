package com.study.service;

import com.study.entity.CampingSite;
import com.study.repository.CampingSiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampingSiteService {

    private final CampingSiteRepository campingSiteRepository;

    public CampingSiteService(CampingSiteRepository campingSiteRepository) {
        this.campingSiteRepository = campingSiteRepository;
    }

    public List<CampingSite> getAllCampingSites() {
        // 리포지토리에서 모든 캠핑장 데이터를 가져옵니다.
        return campingSiteRepository.findAll();
    }
}
