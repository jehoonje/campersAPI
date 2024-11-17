package com.campers.repository;

import com.campers.entity.Favorite;
import com.campers.entity.MarkerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndMarkerTypeAndMarkerId(Long userId, MarkerType markerType, Long markerId);

    List<Favorite> findByUserId(Long userId);

    void deleteByUserIdAndMarkerTypeAndMarkerId(Long userId, MarkerType markerType, Long markerId);

    boolean existsByUserIdAndMarkerTypeAndMarkerId(Long userId, MarkerType markerType, Long markerId);
}
