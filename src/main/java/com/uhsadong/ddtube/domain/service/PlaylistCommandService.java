package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreatePlaylistResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.validator.PlaylistValidator;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.util.IdGenerator;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistCommandService {

    private final UserCommandService userCommandService;
    private final VideoQueryService videoQueryService;
    private final SseService sseService;
    private final PlaylistRepositoryService playlistRepositoryService;
    private final PlaylistValidator playlistValidator;
    private final UserValidator userValidator;
    private final PlaylistRepository playlistRepository;

    @Value("${ddtube.playlist.code_length}")
    private Integer PLAYLIST_CODE_LENGTH;

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
        LocalDateTime lastLoginAt = LocalDateTime.now();

        // 썸네일 URL 검증
        playlistValidator.checkThumbnailUrl(requestDTO.thumbnailUrl());

        String thumbnailUrl = requestDTO.thumbnailUrl().isEmpty()
            ? defaultThumbnailUrl : requestDTO.thumbnailUrl();

        Playlist playlist = playlistRepositoryService.save(
            Playlist.toEntity(code, requestDTO.playlistTitle(), requestDTO.playlistDescription(),
                thumbnailUrl, lastLoginAt)
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
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);

        userValidator.checkUserIsAdminOfPlaylist(playlist, user);

        playlistRepositoryService.delete(playlist);
    }

    @Transactional
    public void setNowPlayingVideo(User user, String playlistCode, String videoCode,
        Boolean autoPlay) {
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);
        Video video = videoQueryService.getVideoByCodeOrThrow(videoCode);
        if (playlist.getNowPlayVideo() != null && video.getId()
            .equals(playlist.getNowPlayVideo().getId())) {
            return;
        }

        playlistValidator.checkVideoInPlaylist(playlist, video);

        playlist.setNowPlayVideo(video);

        sseService.sendNowPlayingVideoEventToClients(playlistCode, video, user.getName(), autoPlay);

    }

    @Transactional
    public void restorePlaylist(String playlistCode) {
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        // 플리가 비활성 상태인지 확인
        playlistValidator.checkPlaylistIsInactive(playlist);
        playlistRepository.updateLastLoginAtByPlaylistCode(playlistCode, LocalDateTime.now());
    }

}
