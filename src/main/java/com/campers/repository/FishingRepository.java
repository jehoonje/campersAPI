package com.campers.repository;

import com.campers.entity.Fishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FishingRepository extends JpaRepository<Fishing, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);
}
