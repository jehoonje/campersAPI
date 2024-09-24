package com.study.repository;

import com.study.entity.CampingSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampingSiteRepository extends JpaRepository<CampingSite, Long> {
}
