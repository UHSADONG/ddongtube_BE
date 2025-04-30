package com.uhsadong.ddtube.domain.repositoryservice;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistRepositoryService {

    private final PlaylistRepository playlistRepository;

    public Playlist save(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void updateLastLoginAtToNowByPlaylistCode(String playlistCode) {
        playlistRepository.updateLastLoginAtByPlaylistCode(playlistCode,
            LocalDateTime.now());
    }

    public Optional<Playlist> findByCodeOptional(String code) {
        return playlistRepository.findFirstByCode(code);
    }

    public Playlist findByCodeOrThrow(String playlistCode) {
        return playlistRepository.findFirstByCode(playlistCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
    }

    public void delete(Playlist playlist) {
        playlistRepository.delete(playlist);
    }
}
