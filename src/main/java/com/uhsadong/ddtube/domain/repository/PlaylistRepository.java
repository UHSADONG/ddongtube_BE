package com.uhsadong.ddtube.domain.repository;

import com.uhsadong.ddtube.domain.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

}
