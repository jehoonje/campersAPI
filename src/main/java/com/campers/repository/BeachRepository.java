package com.campers.repository;

import com.campers.entity.Beach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeachRepository extends JpaRepository<Beach, Long> {
    // 위도와 경도가 모두 일치하는지 확인
    boolean existsByLatAndLng(Double lat, Double lng);

    List<Beach> findByTitleContainingOrAddrContaining(String title, String addr);

    Optional<Beach> findByContentId(Long contentId);

    // 필요한 경우 추가적인 커스텀 쿼리 메서드 작성 가능
}
