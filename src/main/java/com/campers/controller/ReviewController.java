// src/main/java/com/campers/controller/ReviewController.java

package com.campers.controller;

import com.campers.entity.Review;
import com.campers.entity.User;
import com.campers.service.ReviewService;
import com.campers.service.UserService;
import com.campers.util.JwtTokenUtil;
import com.campers.dto.ReviewDTO;
import com.campers.dto.ReviewRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    // 리뷰 생성 (인증 필요)
    @PostMapping
    public ResponseEntity<?> createReview(@RequestHeader("Authorization") String token, @RequestBody ReviewRequestDTO reviewRequest) {
        // 토큰에서 이메일 추출
        String email = jwtTokenUtil.getEmailFromToken(token.substring(7)); // "Bearer " 제거

        // 이메일로 사용자 정보 조회
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 중복 리뷰 확인
        boolean reviewExists = reviewService.existsByUserAndContentTypeAndContentId(user, reviewRequest.getContentType(), reviewRequest.getContentId());
        if (reviewExists) {
            return ResponseEntity.status(400).body("You have already written a review for this content.");
        }

        // 리뷰 생성
        Review review = new Review();
        review.setUser(user);
        review.setContentType(reviewRequest.getContentType());
        review.setContentId(reviewRequest.getContentId());
        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());

        Review createdReview = reviewService.createReview(review);

        // Return the created review as DTO
        ReviewDTO dto = new ReviewDTO();
        dto.setId(createdReview.getId());
        dto.setUserId(user.getId());
        dto.setUserName(user.getEmail()); // 또는 user.getName()
        dto.setContentType(createdReview.getContentType());
        dto.setContentId(createdReview.getContentId());
        dto.setContent(createdReview.getContent());
        dto.setRating(createdReview.getRating());
        dto.setCreatedAt(createdReview.getCreatedAt());
        dto.setUpdatedAt(createdReview.getUpdatedAt());

        return ResponseEntity.ok(dto);
    }

    // 특정 컨텐츠에 대한 리뷰 목록 조회 (인증 불필요)
    @GetMapping("/{contentType}/{contentId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByContent(@PathVariable String contentType, @PathVariable Long contentId) {
        List<Review> reviews = reviewService.getReviewsByContentTypeAndContentId(contentType, contentId);

        // Convert to DTOs
        List<ReviewDTO> reviewDTOs = reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getUserName()); // 또는 review.getUser().getName()
            dto.setContentType(review.getContentType());
            dto.setContentId(review.getContentId());
            dto.setContent(review.getContent());
            dto.setRating(review.getRating());
            dto.setCreatedAt(review.getCreatedAt());
            dto.setUpdatedAt(review.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(reviewDTOs);
    }

    // 리뷰 수정 (인증 필요)
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@RequestHeader("Authorization") String token, @PathVariable Long reviewId, @RequestBody ReviewRequestDTO reviewRequest) {
        // 토큰에서 이메일 추출
        String email = jwtTokenUtil.getEmailFromToken(token.substring(7));

        // 이메일로 사용자 정보 조회
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 기존 리뷰 조회
        Review existingReview = reviewService.getReviewById(reviewId);

        if (existingReview == null) {
            return ResponseEntity.status(404).body("Review not found");
        }

        // 작성자 일치 여부 확인
        if (!existingReview.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own reviews");
        }

        // 리뷰 내용 업데이트
        existingReview.setContent(reviewRequest.getContent());
        existingReview.setRating(reviewRequest.getRating());
        existingReview.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewService.updateReview(existingReview);

        // Return updated review as DTO
        ReviewDTO dto = new ReviewDTO();
        dto.setId(savedReview.getId());
        dto.setUserId(user.getId());
        dto.setUserName(user.getEmail()); // 또는 user.getName()
        dto.setContentType(savedReview.getContentType());
        dto.setContentId(savedReview.getContentId());
        dto.setContent(savedReview.getContent());
        dto.setRating(savedReview.getRating());
        dto.setCreatedAt(savedReview.getCreatedAt());
        dto.setUpdatedAt(savedReview.getUpdatedAt());

        return ResponseEntity.ok(dto);
    }

    // 리뷰 삭제 (인증 필요)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@RequestHeader("Authorization") String token, @PathVariable Long reviewId) {
        // 토큰에서 이메일 추출
        String email = jwtTokenUtil.getEmailFromToken(token.substring(7));

        // 이메일로 사용자 정보 조회
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 기존 리뷰 조회
        Review existingReview = reviewService.getReviewById(reviewId);

        if (existingReview == null) {
            return ResponseEntity.status(404).body("Review not found");
        }

        // 작성자 일치 여부 확인
        if (!existingReview.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own reviews");
        }

        // 리뷰 삭제
        reviewService.deleteReview(reviewId);

        return ResponseEntity.noContent().build();
    }

    // 특정 컨텐츠에 대한 평균 별점 조회
    @GetMapping("/average/{contentType}/{contentId}")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable String contentType, @PathVariable Long contentId) {
        List<Review> reviews = reviewService.getReviewsByContentTypeAndContentId(contentType, contentId);
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<String, Double> response = new HashMap<>();
        response.put("averageRating", average);

        return ResponseEntity.ok(response);
    }
}

