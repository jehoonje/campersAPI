// src/main/java/com/campers/service/ReviewService.java

package com.campers.service;

import com.campers.entity.Review;
import com.campers.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // 리뷰 생성
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    // 특정 컨텐츠에 대한 리뷰 목록 조회
    public List<Review> getReviewsByContentTypeAndContentId(String contentType, Long contentId) {
        return reviewRepository.findByContentTypeAndContentId(contentType, contentId);
    }

    // 리뷰 수정
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    // 리뷰 ID로 리뷰 조회
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }
}

