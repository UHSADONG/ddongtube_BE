package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Video;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
    // Playlist Code와 Video Code로 Video를 찾는다.
    Optional<Video> findFirstByPlaylistCodeAndCode(String playlistCode, String code);
    // Playlist Code로 Video를 찾고 CreatedAt으로 정렬한다.
    List<Video> findAllByPlaylistCodeOrderByCreatedAt(String playlistCode);

    // Playlist Code에 해당하는 모든 Video의 개수를 센다.
    Optional<Video> findFirstByPlaylistCodeOrderByPriorityDesc(String playlistCode);

}
