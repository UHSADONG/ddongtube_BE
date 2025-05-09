package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.response.PlaylistDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistHealthResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistPublicMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.service.PlaylistQueryService;
import com.uhsadong.ddtube.domain.service.UserQueryService;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlaylistQueryServiceTest {

    private final Integer deleteAfterDays = 30;
    @InjectMocks
    private PlaylistQueryService playlistQueryService;
    @Mock
    private PlaylistRepositoryService playlistRepositoryService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private VideoQueryService videoQueryService;
    @Mock
    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(playlistQueryService, "PLAYLIST_DELETE_DAYS", deleteAfterDays);
    }

    @Test
    @DisplayName("플레이리스트 공개 메타 정보 조회 - 코드에 해당하는 플레이리스트의 기본 정보를 반환한다")
    void getPlaylistPublicMetaInformation_success() {
        // given
        String playlistCode = "abcdefgh";
        String title = "Test Playlist";
        String description = "Test Description";
        String thumbnailUrl = "https://example.com/thumbnail.jpg";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            title,
            description,
            thumbnailUrl,
            LocalDateTime.now()
        );

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);

        // when
        PlaylistPublicMetaResponseDTO responseDTO = playlistQueryService.getPlaylistPublicMetaInformation(
            playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.title()).isEqualTo(title);
        assertThat(responseDTO.description()).isEqualTo(description);
        assertThat(responseDTO.thumbnailUrl()).isEqualTo(thumbnailUrl);

        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
    }

    @Test
    @DisplayName("플레이리스트 메타 정보 조회 - 코드와 소유자 정보가 포함된 상세 정보를 반환한다")
    void getPlaylistMetaInformation_success() {
        // given
        String playlistCode = "abcdefgh";
        String title = "Test Playlist";
        String description = "Test Description";
        String thumbnailUrl = "https://example.com/thumbnail.jpg";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            title,
            description,
            thumbnailUrl,
            LocalDateTime.now()
        );

        User adminUser = User.toEntity(playlist, "admin123", "adminUser", "password", true);
        User normalUser1 = User.toEntity(playlist, "user1", "normalUser1", "password", false);
        User normalUser2 = User.toEntity(playlist, "user2", "normalUser2", "password", false);

        List<User> userList = Arrays.asList(adminUser, normalUser1, normalUser2);

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
        when(userQueryService.getUserListByPlaylistCode(playlistCode)).thenReturn(userList);

        // when
        PlaylistMetaResponseDTO responseDTO = playlistQueryService.getPlaylistMetaInformation(
            adminUser, playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.title()).isEqualTo(title);
        assertThat(responseDTO.description()).isEqualTo(description);
        assertThat(responseDTO.thumbnailUrl()).isEqualTo(thumbnailUrl);
        assertThat(responseDTO.owner()).isEqualTo(adminUser.getName());
        assertThat(responseDTO.userList()).hasSize(2);
        assertThat(responseDTO.userList()).contains(normalUser1.getName(), normalUser2.getName());

        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(userQueryService).getUserListByPlaylistCode(playlistCode);
    }

    @Test
    @DisplayName("플레이리스트 상세 정보 조회 - 비디오 목록과 현재 재생 비디오 정보를 포함하여 반환한다")
    void getPlaylistDetail_success() {
        // given
        String playlistCode = "abcdefgh";
        String videoCode = "video123";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);
        playlist.setNowPlayVideo(video);

        List<VideoDetailResponseDTO> videoList = Arrays.asList(
            VideoDetailResponseDTO.builder().code("video1").title("Video 1").build(),
            VideoDetailResponseDTO.builder().code("video2").title("Video 2").build()
        );

        when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
        when(videoQueryService.getVideoDetailListByPlaylistCode(playlistCode)).thenReturn(
            videoList);

        // when
        PlaylistDetailResponseDTO responseDTO = playlistQueryService.getPlaylistDetail(user,
            playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.title()).isEqualTo(playlist.getTitle());
        assertThat(responseDTO.nowPlayingVideoCode()).isEqualTo(videoCode);
        assertThat(responseDTO.videoList()).hasSize(2);

        verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
        verify(videoQueryService).getVideoDetailListByPlaylistCode(playlistCode);
    }

    @Test
    @DisplayName("플레이리스트 상태 확인 - 존재하지 않는 플레이리스트는 NOT_EXIST 상태를 반환한다")
    void checkPlaylistHealth_notExist() {
        // given
        String playlistCode = "nonexistent";

        when(playlistRepositoryService.findByCodeOptional(playlistCode)).thenReturn(
            Optional.empty());

        // when
        PlaylistHealthResponseDTO responseDTO = playlistQueryService.checkPlaylistHealth(
            playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.health()).isEqualTo(PlaylistHealth.NOT_EXIST);
        assertThat(responseDTO.playlistCode()).isEqualTo(playlistCode);

        verify(playlistRepositoryService).findByCodeOptional(playlistCode);
    }

    @Test
    @DisplayName("플레이리스트 상태 확인 - 오랫동안 로그인하지 않은 플레이리스트는 INACTIVE 상태를 반환한다")
    void checkPlaylistHealth_inactive() {
        // given
        String playlistCode = "abcdefgh";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now().minusDays(deleteAfterDays + 1)
        );

        when(playlistRepositoryService.findByCodeOptional(playlistCode)).thenReturn(
            Optional.of(playlist));

        // when
        PlaylistHealthResponseDTO responseDTO = playlistQueryService.checkPlaylistHealth(
            playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.health()).isEqualTo(PlaylistHealth.INACTIVE);
        assertThat(responseDTO.playlistCode()).isEqualTo(playlistCode);

        verify(playlistRepositoryService).findByCodeOptional(playlistCode);
    }

    @Test
    @DisplayName("플레이리스트 상태 확인 - 최근에 로그인한 플레이리스트는 ACTIVE 상태를 반환한다")
    void checkPlaylistHealth_active() {
        // given
        String playlistCode = "abcdefgh";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        when(playlistRepositoryService.findByCodeOptional(playlistCode)).thenReturn(
            Optional.of(playlist));

        // when
        PlaylistHealthResponseDTO responseDTO = playlistQueryService.checkPlaylistHealth(
            playlistCode);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.health()).isEqualTo(PlaylistHealth.ACTIVE);
        assertThat(responseDTO.playlistCode()).isEqualTo(playlistCode);

        verify(playlistRepositoryService).findByCodeOptional(playlistCode);
    }
}
