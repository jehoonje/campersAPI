package com.campers.repository;

import com.campers.entity.Campsite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampsiteRepository extends JpaRepository<Campsite, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);
}
