package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreatePlaylistResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.S3Util;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

    private final PlaylistRepository playlistRepository;
    private final UserCommandService userCommandService;
    private final S3Util s3Util;
    private final UserQueryService userQueryService;
    @Value("${ddtube.playlist.code_length}")
    private Integer PLAYLIST_CODE_LENGTH;
    @Value("${ddtube.playlist.delete_hours}")
    private Integer PLAYLIST_DELETE_HOURS;

    @Value("${aws.s3.default-thumbneil-url}")
    private String defaultThumbnailUrl;

    /**
     * 재생목록을 생성함 + 동시에 재생목록을 생성한 사람의 정보도 생성함
     */
    @Transactional
    public CreatePlaylistResponseDTO createPlaylist(
        CreatePlaylistRequestDTO requestDTO
    ) {
        String code = IdGenerator.generateShortId(PLAYLIST_CODE_LENGTH);
        LocalDateTime willDeleteAt = LocalDateTime.now().plusHours(PLAYLIST_DELETE_HOURS);

        if (!(s3Util.isS3Url(requestDTO.thumbnailUrl()) || requestDTO.thumbnailUrl().isEmpty())) {
            throw new GeneralException(ErrorStatus._INVALID_THUMBNAIL_URL);
        }
        String thumbnailUrl = requestDTO.thumbnailUrl().isEmpty()
            ? defaultThumbnailUrl : requestDTO.thumbnailUrl();

        Playlist playlist = playlistRepository.save(
            Playlist.toEntity(code, requestDTO.playlistTitle(), requestDTO.playlistDescription(), thumbnailUrl, willDeleteAt)
        );

        String accessToken = userCommandService.createPlaylistCreator(
            playlist, requestDTO.userName(), requestDTO.userPassword()
        );

        return CreatePlaylistResponseDTO.builder()
            .playlistCode(playlist.getCode())
            .accessToken(accessToken)
            .build();
    }

    @Transactional
    public void deletePlaylist(User user, String playlistCode) {
        Playlist playlist = playlistRepository.findFirstByCode(playlistCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
        userQueryService.checkUserInPlaylist(user, playlist);
        if (!user.isAdmin()) {
            throw new GeneralException(ErrorStatus._PLAYLIST_DELETE_PERMISSION_DENIED);
        }
        playlistRepository.delete(playlist);
    }

}
