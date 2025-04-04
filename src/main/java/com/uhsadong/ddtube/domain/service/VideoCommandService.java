package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.sse.SseStatus;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.YoutubeOEmbed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCommandService {

    private final VideoRepository videoRepository;
    private final PlaylistQueryService playlistQueryService;
    private final UserQueryService userQueryService;
    private final SseService sseService;
    @Value("${ddtube.video.code_length}")
    private int VIDEO_CODE_LENGTH;
    @Value("${ddtube.playlist.priority_step}")
    private long PRIORITY_STEP;

    @Transactional
    public void addVideoToPlaylist(
        User user, String playlistCode, AddVideoToPlaylistRequestDTO requestDTO) {
        Playlist playlist = playlistQueryService.getPlaylistByCodeOrThrow(playlistCode);
        userQueryService.checkUserInPlaylist(user, playlist);

        String code = IdGenerator.generateShortId(VIDEO_CODE_LENGTH);
        YoutubeOEmbedDTO youtubeOEmbedDTO = YoutubeOEmbed.getVideoInfo(requestDTO.videoUrl());
        Long priority = videoRepository
            .findFirstByPlaylistCodeOrderByPriorityDesc(playlistCode)
            .map(Video::getPriority)
            .orElse(0L);
        Video video = videoRepository.save(
            Video.toEntity(playlist, user, code, requestDTO.videoDescription(),
                requestDTO.videoUrl(), youtubeOEmbedDTO,
                priority + PRIORITY_STEP)
        );
        sseService.sendVideoEventToClients(playlistCode, video, SseStatus.ADD);

    }

    @Transactional
    public void deleteVideoFromPlaylist(
        User user, String playlistCode, String videoCode) {
        Playlist playlist = playlistQueryService.getPlaylistByCodeOrThrow(playlistCode);
        userQueryService.checkUserInPlaylist(user, playlist);
        Video video = videoRepository.findFirstByPlaylistCodeAndCode(playlist.getCode(), videoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));
        // 영상을 추가한 사람이거나 플레이리스트의 관리자가 아니면 에러
        if (!(video.getUser().getId().equals(user.getId()) || user.isAdmin())) {
            throw new GeneralException(ErrorStatus._VIDEO_DELETE_PERMISSION_DENIED);
        }
        if (playlist.getNowPlayVideo() != null && video.getId()
            .equals(playlist.getNowPlayVideo().getId())) {
            throw new GeneralException(ErrorStatus._CANNOT_DELETE_NOW_PLAY_VIDEO);
        }

        videoRepository.delete(video);
        sseService.sendVideoEventToClients(playlistCode, video, SseStatus.DELETE);
    }

}
