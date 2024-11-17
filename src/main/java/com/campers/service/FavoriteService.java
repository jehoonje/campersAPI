package com.campers.service;

import com.campers.entity.Favorite;
import com.campers.entity.MarkerType;
import com.campers.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // 즐겨찾기 추가
    public Favorite addFavorite(Long userId, MarkerType markerType, Long markerId) throws Exception {
        if (favoriteRepository.existsByUserIdAndMarkerTypeAndMarkerId(userId, markerType, markerId)) {
            throw new Exception("이미 즐겨찾기에 추가된 마커입니다.");
        }
        Favorite favorite = new Favorite(userId, markerType, markerId);
        return favoriteRepository.save(favorite);
    }

    // 즐겨찾기 삭제
    public void removeFavorite(Long userId, MarkerType markerType, Long markerId) throws Exception {
        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndMarkerTypeAndMarkerId(userId, markerType, markerId);
        if (!favoriteOpt.isPresent()) {
            throw new Exception("즐겨찾기에 추가되지 않은 마커입니다.");
        }
        favoriteRepository.delete(favoriteOpt.get());
    }

    // 즐겨찾기 상태 확인
    public boolean isFavorite(Long userId, MarkerType markerType, Long markerId) {
        return favoriteRepository.existsByUserIdAndMarkerTypeAndMarkerId(userId, markerType, markerId);
    }

    // 사용자 즐겨찾기 목록 조회
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }
}
