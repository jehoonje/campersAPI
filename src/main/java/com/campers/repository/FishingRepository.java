package com.campers.repository;

import com.campers.entity.Fishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FishingRepository extends JpaRepository<Fishing, Long> {
    boolean existsByLatAndLng(Double lat, Double lng);

    List<Fishing> findByTitleContainingOrAddrContaining(String title, String addr);

    Optional<Fishing> findByContentId(Long contentId);
}
