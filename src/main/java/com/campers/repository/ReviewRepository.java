// src/main/java/com/campers/repository/ReviewRepository.java

package com.campers.repository;

import com.campers.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 컨텐츠에 대한 모든 리뷰 가져오기
    List<Review> findByContentTypeAndContentId(String contentType, Long contentId);
}

