package com.campers.repository;

import com.campers.entity.Campsite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampsiteRepository extends JpaRepository<Campsite, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);

    List<Campsite> findByTitleContainingOrAddrContaining(String title, String addr);

    Optional<Campsite> findByContentId(Long contentId);
}
