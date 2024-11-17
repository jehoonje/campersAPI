package com.campers.controller;

import com.campers.entity.Favorite;
import com.campers.entity.MarkerType;
import com.campers.service.FavoriteService;
import com.campers.dto.FavoriteRequest;
import com.campers.dto.FavoriteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteRequest request) {
        try {
            Favorite favorite = favoriteService.addFavorite(request.getUserId(), request.getMarkerType(), request.getMarkerId());
            return new ResponseEntity<>(new FavoriteResponse(favorite), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 즐겨찾기 삭제
    @DeleteMapping
    public ResponseEntity<?> removeFavorite(@RequestBody FavoriteRequest request) {
        try {
            favoriteService.removeFavorite(request.getUserId(), request.getMarkerType(), request.getMarkerId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 즐겨찾기 상태 확인
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String markerType,
            @RequestParam(required = false) Long markerId) {

        if (userId == null || markerType == null || markerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameters");
        }

        try {
            boolean isFavorite = favoriteService.isFavorite(userId, MarkerType.valueOf(markerType.toUpperCase()), markerId);
            return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid markerType value");
        }
    }


    // 사용자 즐겨찾기 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserFavorites(@PathVariable Long userId) {
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        List<FavoriteResponse> response = favorites.stream()
                .map(FavoriteResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
