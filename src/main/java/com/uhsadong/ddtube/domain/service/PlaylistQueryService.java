package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;

    public Playlist getPlaylistByCodeOrThrow(String code) {
        return playlistRepository.findFirstByCode(code)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
    }

}
