package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Video;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findFirstByPlaylistCodeAndCode(String playlistCode, String code);
    List<Video> findAllByPlaylistCodeOrderByCreatedAt(String playlistCode);
}
