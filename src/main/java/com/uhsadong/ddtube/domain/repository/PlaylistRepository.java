package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Playlist;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findFirstByCode(String code);

    /**
     * 입력된 모든 플레이리스트의 마지막 로그인 시간을 업데이트합니다.
     */
    @Modifying
    @Query("UPDATE Playlist p SET p.lastLoginAt = :lastLoginAt WHERE p.code IN :playlistCodes")
    int updateLastLoginAtByPlaylistCodeIn(@Param("playlistCodes") Set<String> playlistCodes,
        @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
