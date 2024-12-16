package com.campers.repository;

import com.campers.entity.Autocamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutocampRepository extends JpaRepository<Autocamp, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);

    List<Autocamp> findByTitleContainingOrAddrContaining(String title, String addr);

    Optional<Autocamp> findByContentId(Long contentId);
}
