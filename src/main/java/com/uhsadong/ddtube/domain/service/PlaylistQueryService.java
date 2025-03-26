package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;

    public Playlist getPlaylistByCodeOrThrow(String code) {
        return playlistRepository.findFirstByCode(code)
            .orElseThrow(IllegalArgumentException::new);
    }

}
