package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.util.IdGenerator;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

    @Value("${ddtube.playlist.code_length}")
    private Integer PLAYLIST_CODE_LENGTH;
    @Value("${ddtube.playlist.delete_hours}")
    private Integer PLAYLIST_DELETE_HOURS;

    private final PlaylistRepository playlistRepository;
    private final UserCommandService userCommandService;

    /**
     * 재생목록을 생성함 + 동시에 재생목록을 생성한 사람의 정보도 생성함
     *
     * @param requestDTO
     * @return
     */
    @Transactional
    public String createPlaylist(CreatePlaylistRequestDTO requestDTO) {
        String code = IdGenerator.generateShortId(PLAYLIST_CODE_LENGTH);
        LocalDateTime willDeleteAt = LocalDateTime.now().plusHours(PLAYLIST_DELETE_HOURS);
        Playlist playlist = playlistRepository.save(
            Playlist.toEntity(
                code, requestDTO.playlistTitle(), requestDTO.playlistPin(), willDeleteAt
            )
        );

        User user = userCommandService.createPlaylistCreator(
            playlist, requestDTO.userName(), requestDTO.userPassword()
        );

        return playlist.getCode();
    }

}
