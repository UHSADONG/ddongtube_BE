package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.YoutubeOEmbedDTO;
import com.uhsadong.ddtube.domain.dto.request.AddVideoToPlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.MoveVideoResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.service.UserQueryService;
import com.uhsadong.ddtube.domain.service.VideoCommandService;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.domain.validator.VideoValidator;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.sse.SseStatus;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.YoutubeOEmbed;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoCommandServiceTest {

    private final int videoCodeLength = 8;
    private final long priorityStep = 1000L;
    @InjectMocks
    private VideoCommandService videoCommandService;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private PlaylistRepositoryService playlistRepositoryService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private SseService sseService;
    @Mock
    private VideoValidator videoValidator;
    @Mock
    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoCommandService, "VIDEO_CODE_LENGTH", videoCodeLength);
        ReflectionTestUtils.setField(videoCommandService, "PRIORITY_STEP", priorityStep);
    }

    @Test
    @DisplayName("비디오 추가 - 성공 케이스: 유효한 비디오 URL로 추가한다")
    void addVideoToPlaylist_success() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";
        String videoUrl = "https://www.youtube.com/watch?v=abc123";
        String videoDescription = "Test Video Description";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);

        YoutubeOEmbedDTO youtubeOEmbedDTO = new YoutubeOEmbedDTO(
            "Test Video Title",
            "Test Author",
            "https://example.com/author",
            "Video",
            1080,
            780,
            "version",
            "provider",
            "https://example.com/video",
            "1080",
            "720",
            "https://example.com/thumbnail.jpg",
            "https://example.com/thumbnail.jpg"
        );

        Video savedVideo = Video.toEntity(
            playlist,
            user,
            videoCode,
            videoDescription,
            videoUrl,
            youtubeOEmbedDTO,
            priorityStep
        );

        AddVideoToPlaylistRequestDTO requestDTO = new AddVideoToPlaylistRequestDTO(
            videoUrl,
            videoDescription
        );

        try (MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class);
            MockedStatic<YoutubeOEmbed> youtubeOEmbedMock = Mockito.mockStatic(
                YoutubeOEmbed.class)) {

            idGeneratorMock.when(() -> IdGenerator.generateShortId(videoCodeLength))
                .thenReturn(videoCode);
            youtubeOEmbedMock.when(() -> YoutubeOEmbed.getVideoInfo(videoUrl))
                .thenReturn(youtubeOEmbedDTO);

            when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
            when(videoRepository.findFirstByPlaylistCodeOrderByPriorityDesc(playlistCode))
                .thenReturn(Optional.empty());
            when(videoRepository.save(any(Video.class))).thenReturn(savedVideo);

            // when
            videoCommandService.addVideoToPlaylist(user, playlistCode, requestDTO);

            // then
            verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
            verify(videoRepository).findFirstByPlaylistCodeOrderByPriorityDesc(playlistCode);
            verify(videoRepository).save(any(Video.class));
            verify(sseService).sendVideoEventToClients(playlistCode, savedVideo, SseStatus.ADD);
        }
    }

    @Test
    @DisplayName("비디오 삭제 - 성공 케이스: 본인이 추가한 비디오를 삭제한다")
    void deleteVideoFromPlaylist_videoOwner_success() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);
        ReflectionTestUtils.setField(user, "id", 1L);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);
        ReflectionTestUtils.setField(video, "user", user);

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode))
            .thenReturn(Optional.of(video));

        // when
        videoCommandService.deleteVideoFromPlaylist(user, playlistCode, videoCode);

        // then
        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(videoRepository).findFirstByPlaylistCodeAndCode(playlistCode, videoCode);
        verify(videoRepository).delete(video);
        verify(sseService).sendVideoEventToClients(playlistCode, video, SseStatus.DELETE);
    }

    @Test
    @DisplayName("비디오 삭제 - 성공 케이스: 관리자가 다른 사용자의 비디오를 삭제한다")
    void deleteVideoFromPlaylist_admin_success() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User adminUser = User.toEntity(playlist, "adminCode", "adminUser", "password", true);
        ReflectionTestUtils.setField(adminUser, "id", 1L);

        User normalUser = User.toEntity(playlist, "userCode", "normalUser", "password", false);
        ReflectionTestUtils.setField(normalUser, "id", 2L);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);
        ReflectionTestUtils.setField(video, "user", normalUser);

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode))
            .thenReturn(Optional.of(video));

        // when
        videoCommandService.deleteVideoFromPlaylist(adminUser, playlistCode, videoCode);

        // then
        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(videoRepository).findFirstByPlaylistCodeAndCode(playlistCode, videoCode);
        verify(videoRepository).delete(video);
        verify(sseService).sendVideoEventToClients(playlistCode, video, SseStatus.DELETE);
    }

    @Test
    @DisplayName("비디오 이동 - 성공 케이스: 대상 비디오 앞으로 이동한다")
    void moveVideo_positionBefore_success() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "video1";
        String targetVideoCode = "video2";
        boolean positionBefore = true;
        Long newPriority = 500L;

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User adminUser = User.toEntity(playlist, "adminCode", "adminUser", "password", true);

        Video targetVideo = new Video();
        ReflectionTestUtils.setField(targetVideo, "code", targetVideoCode);
        ReflectionTestUtils.setField(targetVideo, "priority", 1000L);

        Video movedVideo = new Video();
        ReflectionTestUtils.setField(movedVideo, "code", videoCode);
        ReflectionTestUtils.setField(movedVideo, "priority", 2000L);

        MoveVideoResponseDTO moveVideoResponseDTO = MoveVideoResponseDTO.builder()
            .conflict(false)
            .videoCode(videoCode)
            .newPriority(newPriority)
            .oldPriority(2000L)
            .build();

        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, targetVideoCode))
            .thenReturn(Optional.of(targetVideo));
        when(videoRepository.findPreviousVideoExcept(playlistCode, targetVideo.getPriority(),
            videoCode))
            .thenReturn(Optional.empty());
        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode))
            .thenReturn(Optional.of(movedVideo));

        // VideoCommandService를 부분적으로 모킹
        VideoCommandService spyService = spy(videoCommandService);
        doReturn(moveVideoResponseDTO).when(spyService)
            .updateVideoPriorityWithCheck(playlistCode, videoCode, newPriority);

        // when
        MoveVideoResponseDTO result = spyService.moveVideo(adminUser, playlistCode, videoCode,
            targetVideoCode, positionBefore);

        // then
        assertThat(result).isNotNull();
        assertThat(result.conflict()).isFalse();
        assertThat(result.videoCode()).isEqualTo(videoCode);
        assertThat(result.newPriority()).isEqualTo(newPriority);

        verify(videoRepository).findFirstByPlaylistCodeAndCode(playlistCode, targetVideoCode);
        verify(videoRepository).findPreviousVideoExcept(playlistCode, targetVideo.getPriority(),
            videoCode);
        verify(spyService).updateVideoPriorityWithCheck(playlistCode, videoCode, newPriority);
        verify(sseService).sendVideoEventToClients(eq(playlistCode), any(Video.class),
            eq(SseStatus.MOVE));
    }

    @Test
    @DisplayName("비디오 우선순위 업데이트 - 성공 케이스: 비디오의 우선순위가 성공적으로 업데이트된다")
    void updateVideoPriorityWithCheck_success() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";
        Long oldPriority = 2000L;
        Long newPriority = 1000L;

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);
        ReflectionTestUtils.setField(video, "priority", oldPriority);

        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode))
            .thenReturn(Optional.of(video));
        when(videoRepository.save(video)).thenReturn(video);

        // when
        MoveVideoResponseDTO result = videoCommandService.updateVideoPriorityWithCheck(playlistCode,
            videoCode, newPriority);

        // then
        assertThat(result).isNotNull();
        assertThat(result.conflict()).isFalse();
        assertThat(result.videoCode()).isEqualTo(videoCode);
        assertThat(result.newPriority()).isEqualTo(newPriority);
        assertThat(result.oldPriority()).isEqualTo(oldPriority);
        assertThat(video.getPriority()).isEqualTo(newPriority);

        verify(videoRepository).findFirstByPlaylistCodeAndCode(playlistCode, videoCode);
        verify(videoRepository).save(video);
    }

    @Test
    @DisplayName("비디오 우선순위 업데이트 - 실패 케이스: 낙관적 락 예외 발생 시 충돌 예외로 변환된다")
    void updateVideoPriorityWithCheck_optimisticLockException_throwsConflictException() {
        // given
        String playlistCode = "playlistCode";
        String videoCode = "videoCode";
        Long newPriority = 1000L;

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);
        ReflectionTestUtils.setField(video, "priority", 2000L);

        when(videoRepository.findFirstByPlaylistCodeAndCode(playlistCode, videoCode))
            .thenReturn(Optional.of(video));
        when(videoRepository.save(video)).thenThrow(OptimisticLockException.class);

        // when, then
        assertThatThrownBy(
            () -> videoCommandService.updateVideoPriorityWithCheck(playlistCode, videoCode,
                newPriority))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._VIDEO_MOVE_CONFLICT.getMessage());
    }
}
