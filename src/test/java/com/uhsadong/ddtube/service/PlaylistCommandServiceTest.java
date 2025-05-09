package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.request.CreatePlaylistRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreatePlaylistResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.service.PlaylistCommandService;
import com.uhsadong.ddtube.domain.service.UserCommandService;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.domain.validator.PlaylistValidator;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.global.sse.SseService;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.S3Util;
import java.time.LocalDateTime;
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
class PlaylistCommandServiceTest {

    private final String defaultThumbnailUrl = "https://default-thumbnail.com";
    private final Integer playlistCodeLength = 8;
    @InjectMocks
    private PlaylistCommandService playlistCommandService;
    @Mock
    private UserCommandService userCommandService;
    @Mock
    private S3Util s3Util;
    @Mock
    private VideoQueryService videoQueryService;
    @Mock
    private SseService sseService;
    @Mock
    private PlaylistRepositoryService playlistRepositoryService;
    @Mock
    private PlaylistValidator playlistValidator;
    @Mock
    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(playlistCommandService, "defaultThumbnailUrl",
            defaultThumbnailUrl);
        ReflectionTestUtils.setField(playlistCommandService, "PLAYLIST_CODE_LENGTH",
            playlistCodeLength);
    }

    @Test
    @DisplayName("재생목록 생성 - 성공 케이스: 유효한 썸네일 URL로 재생목록을 생성한다")
    void createPlaylist_success() {
        // given
        String generatedCode = "abcdefgh";
        String accessToken = "test-access-token";
        String thumbnailUrl = "https://example.com/thumbnail.jpg";

        CreatePlaylistRequestDTO requestDTO = new CreatePlaylistRequestDTO(
            "testUser",
            "password123",
            "Test Playlist",
            "Test Description",
            thumbnailUrl
        );

        Playlist savedPlaylist = Playlist.toEntity(
            generatedCode,
            requestDTO.playlistTitle(),
            requestDTO.playlistDescription(),
            thumbnailUrl,
            LocalDateTime.now()
        );

        try (MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class)) {
            idGeneratorMock.when(() -> IdGenerator.generateShortId(playlistCodeLength))
                .thenReturn(generatedCode);

            when(playlistRepositoryService.save(any(Playlist.class))).thenReturn(savedPlaylist);
            when(userCommandService.createPlaylistCreator(any(Playlist.class), anyString(),
                anyString()))
                .thenReturn(accessToken);

            // when
            CreatePlaylistResponseDTO responseDTO = playlistCommandService.createPlaylist(
                requestDTO);

            // then
            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.playlistCode()).isEqualTo(generatedCode);
            assertThat(responseDTO.accessToken()).isEqualTo(accessToken);

            verify(playlistRepositoryService).save(any(Playlist.class));
            verify(userCommandService).createPlaylistCreator(savedPlaylist, requestDTO.userName(),
                requestDTO.userPassword());
        }
    }

    @Test
    @DisplayName("재생목록 생성 - 성공 케이스: 빈 썸네일 URL로 재생목록 생성 시 기본 썸네일을 사용한다")
    void createPlaylist_withEmptyThumbnail_useDefaultThumbnail() {
        // given
        String generatedCode = "abcdefgh";
        String accessToken = "test-access-token";
        String emptyThumbnailUrl = "";

        CreatePlaylistRequestDTO requestDTO = new CreatePlaylistRequestDTO(
            "testUser",
            "password123",
            "Test Playlist",
            "Test Description",
            emptyThumbnailUrl
        );

        Playlist savedPlaylist = Playlist.toEntity(
            generatedCode,
            requestDTO.playlistTitle(),
            requestDTO.playlistDescription(),
            defaultThumbnailUrl,
            LocalDateTime.now()
        );

        try (MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class)) {
            idGeneratorMock.when(() -> IdGenerator.generateShortId(playlistCodeLength))
                .thenReturn(generatedCode);

            when(playlistRepositoryService.save(any(Playlist.class))).thenReturn(savedPlaylist);
            when(userCommandService.createPlaylistCreator(any(Playlist.class), anyString(),
                anyString()))
                .thenReturn(accessToken);

            // when
            CreatePlaylistResponseDTO responseDTO = playlistCommandService.createPlaylist(
                requestDTO);

            // then
            assertThat(responseDTO).isNotNull();
            verify(playlistRepositoryService).save(any(Playlist.class));
        }
    }

    @Test
    @DisplayName("재생목록 삭제 - 성공 케 이스: 관리자가 재생목록을 삭제한다")
    void deletePlaylist_success() {
        // given
        String playlistCode = "abcdefgh";
        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User adminUser = User.toEntity(playlist, "userCode", "adminUser", "password", true);

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);

        // when
        playlistCommandService.deletePlaylist(adminUser, playlistCode);

        // then
        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(playlistRepositoryService).delete(playlist);
    }

    @Test
    @DisplayName("현재 재생 비디오 설정 - 성공 케이스: 비디오가 정상적으로 설정된다")
    void setNowPlayingVideo_success() {
        // given
        String playlistCode = "abcdefgh";
        String videoCode = "video123";
        boolean autoPlay = true;

        Playlist playlist = new Playlist(
            1L,
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);
        Video video = new Video();
        ReflectionTestUtils.setField(video, "id", 1L);
        ReflectionTestUtils.setField(video, "code", videoCode);
        ReflectionTestUtils.setField(video, "playlist", playlist);

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
        when(videoQueryService.getVideoByCodeOrThrow(videoCode)).thenReturn(video);

        // when
        playlistCommandService.setNowPlayingVideo(user, playlistCode, videoCode, autoPlay);

        // then
        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(videoQueryService).getVideoByCodeOrThrow(videoCode);
        verify(sseService).sendNowPlayingVideoEventToClients(playlistCode, video, user.getName(),
            autoPlay);

        assertThat(playlist.getNowPlayVideo()).isEqualTo(video);
    }
}
