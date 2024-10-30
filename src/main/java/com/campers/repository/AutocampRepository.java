package com.campers.repository;

import com.campers.entity.Autocamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutocampRepository extends JpaRepository<Autocamp, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);
}
