// src/main/java/com/yourapp/repository/CampgroundRepository.java
package com.campers.repository;

import com.campers.entity.Campground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampgroundRepository extends JpaRepository<Campground, Long> {
    boolean existsByLatitudeOrLongitude(Double lat, Double lng);

    // Custom query methods if needed
}
