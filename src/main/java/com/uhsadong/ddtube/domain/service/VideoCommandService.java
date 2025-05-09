package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.MoveVideoResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.domain.validator.VideoValidator;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.sse.SseStatus;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.YoutubeOEmbed;
import jakarta.persistence.OptimisticLockException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCommandService {

    private final VideoRepository videoRepository;
    private final PlaylistRepositoryService playlistRepositoryService;
    private final UserValidator userValidator;
    private final VideoValidator videoValidator;
    private final SseService sseService;
    @Value("${ddtube.video.code_length}")
    private int VIDEO_CODE_LENGTH;
    @Value("${ddtube.playlist.priority_step}")
    private long PRIORITY_STEP;

    @Transactional
    public void addVideoToPlaylist(
        User user, String playlistCode, AddVideoToPlaylistRequestDTO requestDTO) {
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);

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
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);
        Video video = videoRepository.findFirstByPlaylistCodeAndCode(playlist.getCode(), videoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));
        // 영상을 추가한 사람이거나 플레이리스트의 관리자가 아니면 에러
        videoValidator.checkPermissionOfVideoUpdate(video, user);
        videoValidator.checkVideoIsNowPlayingInPlaylist(playlist, video);

        videoRepository.delete(video);
        sseService.sendVideoEventToClients(playlistCode, video, SseStatus.DELETE);
    }

    public MoveVideoResponseDTO moveVideo(User user, String playlistCode, String videoCode,
        String targetVideoCode, boolean positionBefore) {

        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);

        userValidator.checkUserIsAdminOfPlaylist(playlist, user);
        videoValidator.checkVideosAreDifferent(videoCode, targetVideoCode);

        // 이동할 위치에 있는 영상
        Video targetVideo = videoRepository.findFirstByPlaylistCodeAndCode(playlistCode,
                targetVideoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));

        Long priorityToChange;
        if (positionBefore) {
            // targetVideoCode 앞에 위치
            Optional<Video> beforeTargetVideoOpt = videoRepository.findPreviousVideoExcept(
                playlistCode, targetVideo.getPriority(), videoCode);

            // 앞 비디오가 있으면 중간값 계산, 없으면 targetVideo보다 앞에 위치
            Long prevPriority = beforeTargetVideoOpt.map(Video::getPriority).orElse(null);
            priorityToChange = calculatePriority(prevPriority, targetVideo.getPriority());

        } else {
            // targetVideoCode 뒤에 위치
            Optional<Video> afterTargetVideoOpt = videoRepository.findNextVideoExcept(
                playlistCode, targetVideo.getPriority(), videoCode);

            // 뒤 비디오가 있으면 중간값 계산, 없으면 targetVideo보다 뒤에 위치
            Long nextPriority = afterTargetVideoOpt.map(Video::getPriority).orElse(null);
            priorityToChange = calculatePriority(targetVideo.getPriority(), nextPriority);
        }

        MoveVideoResponseDTO moveVideoResponseDTO = updateVideoPriorityWithCheck(
            playlistCode, videoCode, priorityToChange);

        // updateVideoPriorityWithCheck에서 변경한 videoToMove를 다시 가져옴
        Video movedVideo = videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));
        sseService.sendVideoEventToClients(playlistCode, movedVideo, SseStatus.MOVE);
        return moveVideoResponseDTO;
    }

    // 우선순위 중복 확인 및 업데이트를 위한 별도 트랜잭션
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MoveVideoResponseDTO updateVideoPriorityWithCheck(String playlistCode, String videoCode,
        Long newPriority) {

        Video videoToMove = videoRepository.findFirstByPlaylistCodeAndCode(playlistCode,
                videoCode)
            .orElseThrow(() -> new GeneralException(ErrorStatus._VIDEO_NOT_FOUND));

        Long oldPriority = videoToMove.getPriority();

        try {
            // 우선순위 업데이트
            videoToMove.updatePriority(newPriority);
            videoRepository.save(videoToMove);

            return MoveVideoResponseDTO.builder()
                .conflict(false)
                .videoCode(videoCode)
                .newPriority(newPriority)
                .oldPriority(oldPriority)
                .build();

        } catch (OptimisticLockException e) {
            throw new GeneralException(ErrorStatus._VIDEO_MOVE_CONFLICT);
        }
    }

    // 우선순위 계산 함수 분리
    private Long calculatePriority(Long prevPriority, Long nextPriority) {
        if (nextPriority == null) {
            return prevPriority + PRIORITY_STEP; // 마지막 위치
        } else if (prevPriority == null) {
            return nextPriority / 2; // 첫 위치
        } else {
            return prevPriority + (nextPriority - prevPriority) / 2; // 중간 위치
        }
    }
}
