package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Video;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

    // Playlist Code와 Video Code로 Video를 찾는다.
    Optional<Video> findFirstByPlaylistCodeAndCode(String playlistCode, String code);

    // Playlist Code로 Video를 찾고 CreatedAt으로 정렬한다.
    List<Video> findAllByPlaylistCodeOrderByPriority(String playlistCode);

    // Playlist Code에 해당하는 모든 Video의 개수를 센다.
    Optional<Video> findFirstByPlaylistCodeOrderByPriorityDesc(String playlistCode);

    Optional<Video> findFirstByCode(String code);

    // 특정 우선순위보다 작고 특정 코드가 아닌 비디오 중 가장 우선순위가 큰 비디오
    @Query("SELECT v "
        + "FROM Video v "
        + "WHERE v.playlist.code = :playlistCode "
        + "AND v.priority < :priority "
        + "AND v.code != :excludeVideoCode "
        + "ORDER BY v.priority DESC "
        + "LIMIT 1")
    Optional<Video> findPreviousVideoExcept(
        @Param("playlistCode") String playlistCode,
        @Param("priority") Long priority,
        @Param("excludeVideoCode") String excludeVideoCode);

    // 특정 우선순위보다 크고 특정 코드가 아닌 비디오 중 가장 우선순위가 작은 비디오
    @Query("SELECT v "
        + "FROM Video v "
        + "WHERE v.playlist.code = :playlistCode "
        + "AND v.priority > :priority "
        + "AND v.code != :excludeVideoCode "
        + "ORDER BY v.priority ASC "
        + "LIMIT 1")
    Optional<Video> findNextVideoExcept(
        @Param("playlistCode") String playlistCode,
        @Param("priority") Long priority,
        @Param("excludeVideoCode") String excludeVideoCode);


    /**
     * 특정 재생목록에서 지정된 우선순위를 가진 비디오가 있는지 확인합니다. (현재 이동 중인 비디오는 제외)
     *
     * @param playlistCode     재생목록 코드
     * @param priority         확인할 우선순위 값
     * @param excludeVideoCode 제외할 비디오 코드 (현재 이동 중인 비디오)
     * @return 해당 우선순위를 가진 비디오가 있으면 true, 없으면 false
     */
    @Query("SELECT CASE "
        + "WHEN COUNT(v) > 0 "
        + "THEN true ELSE false END "
        + "FROM Video v "
        + "WHERE v.playlist.code = :playlistCode "
        + "AND v.priority = :priority "
        + "AND v.code != :excludeVideoCode")
    boolean existsByPlaylistCodeAndPriorityAndCodeNot(
        @Param("playlistCode") String playlistCode,
        @Param("priority") Long priority,
        @Param("excludeVideoCode") String excludeVideoCode);
}
