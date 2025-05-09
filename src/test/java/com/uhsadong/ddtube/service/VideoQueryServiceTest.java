package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.response.UserDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.repository.VideoRepository;
import com.uhsadong.ddtube.domain.service.VideoQueryService;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoQueryServiceTest {

    @InjectMocks
    private VideoQueryService videoQueryService;

    @Mock
    private VideoRepository videoRepository;

    @Test
    @DisplayName("특정 플레이리스트의 비디오 목록 조회 - 플레이리스트 코드에 해당하는 모든 비디오를 우선순위 순으로 반환한다")
    void getVideoListByPlaylistCode_success() {
        // given
        String playlistCode = "playlistCode";

        Video video1 = new Video();
        ReflectionTestUtils.setField(video1, "code", "video1");
        ReflectionTestUtils.setField(video1, "priority", 1000L);

        Video video2 = new Video();
        ReflectionTestUtils.setField(video2, "code", "video2");
        ReflectionTestUtils.setField(video2, "priority", 2000L);

        List<Video> expectedVideos = Arrays.asList(video1, video2);

        when(videoRepository.findAllByPlaylistCodeOrderByPriority(playlistCode)).thenReturn(
            expectedVideos);

        // when
        List<Video> result = videoQueryService.getVideoListByPlaylistCode(playlistCode);

        // then
        assertThat(result)
            .isNotNull()
            .hasSize(2)
            .isEqualTo(expectedVideos);
    }

    @Test
    @DisplayName("특정 플레이리스트의 비디오 상세 정보 목록 조회 - DTO 변환이 올바르게 수행된다")
    void getVideoDetailListByPlaylistCode_success() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", false);
        ReflectionTestUtils.setField(user, "id", 1L);

        Video video1 = new Video();
        ReflectionTestUtils.setField(video1, "code", "video1");
        ReflectionTestUtils.setField(video1, "title", "Video 1");
        ReflectionTestUtils.setField(video1, "description", "Video 1 Description");
        ReflectionTestUtils.setField(video1, "url", "https://www.youtube.com/watch?v=video1");
        ReflectionTestUtils.setField(video1, "authorName", "Author 1");
        ReflectionTestUtils.setField(video1, "height", 1080);
        ReflectionTestUtils.setField(video1, "width", 1920);
        ReflectionTestUtils.setField(video1, "thumbnailUrl", "https://example.com/thumbnail1.jpg");
        ReflectionTestUtils.setField(video1, "thumbnailHeight", 720);
        ReflectionTestUtils.setField(video1, "thumbnailWidth", 1280);
        ReflectionTestUtils.setField(video1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(video1, "priority", 1000L);
        ReflectionTestUtils.setField(video1, "user", user);

        Video video2 = new Video();
        ReflectionTestUtils.setField(video2, "code", "video2");
        ReflectionTestUtils.setField(video2, "title", "Video 2");
        ReflectionTestUtils.setField(video2, "description", "Video 2 Description");
        ReflectionTestUtils.setField(video2, "url", "https://www.youtube.com/watch?v=video2");
        ReflectionTestUtils.setField(video2, "authorName", "Author 2");
        ReflectionTestUtils.setField(video2, "height", 1080);
        ReflectionTestUtils.setField(video2, "width", 1920);
        ReflectionTestUtils.setField(video2, "thumbnailUrl", "https://example.com/thumbnail2.jpg");
        ReflectionTestUtils.setField(video2, "thumbnailHeight", 720);
        ReflectionTestUtils.setField(video2, "thumbnailWidth", 1280);
        ReflectionTestUtils.setField(video2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(video2, "priority", 2000L);
        ReflectionTestUtils.setField(video2, "user", user);

        List<Video> videos = Arrays.asList(video1, video2);

        when(videoRepository.findAllByPlaylistCodeOrderByPriority(playlistCode)).thenReturn(videos);

        // when
        List<VideoDetailResponseDTO> result = videoQueryService.getVideoDetailListByPlaylistCode(
            playlistCode);

        // then
        assertThat(result)
            .isNotNull()
            .hasSize(2);

        VideoDetailResponseDTO dto1 = result.get(0);
        assertThat(dto1.code()).isEqualTo("video1");
        assertThat(dto1.title()).isEqualTo("Video 1");
        assertThat(dto1.description()).isEqualTo("Video 1 Description");

        VideoDetailResponseDTO dto2 = result.get(1);
        assertThat(dto2.code()).isEqualTo("video2");
        assertThat(dto2.title()).isEqualTo("Video 2");
        assertThat(dto2.description()).isEqualTo("Video 2 Description");
    }

    @Test
    @DisplayName("비디오 코드로 비디오 조회 - 존재하는 비디오인 경우 해당 비디오를 반환한다")
    void getVideoByCodeOrThrow_existingVideo_returnsVideo() {
        // given
        String videoCode = "videoCode";

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", videoCode);

        when(videoRepository.findFirstByCode(videoCode)).thenReturn(Optional.of(video));

        // when
        Video result = videoQueryService.getVideoByCodeOrThrow(videoCode);

        // then
        assertThat(result)
            .isNotNull()
            .isEqualTo(video);
    }

    @Test
    @DisplayName("비디오 코드로 비디오 조회 - 존재하지 않는 비디오인 경우 예외가 발생한다")
    void getVideoByCodeOrThrow_nonExistingVideo_throwsException() {
        // given
        String videoCode = "nonExistingCode";

        when(videoRepository.findFirstByCode(videoCode)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> videoQueryService.getVideoByCodeOrThrow(videoCode))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._VIDEO_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("비디오를 DTO로 변환 - 모든 필드가 올바르게 변환된다")
    void convertToVideoDetailResponseDTO_success() {
        // given
        Playlist playlist = Playlist.toEntity(
            "playlistCode",
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User user = User.toEntity(playlist, "userCode", "testUser", "password", true);
        ReflectionTestUtils.setField(user, "id", 1L);

        LocalDateTime createdAt = LocalDateTime.now();
        Long priority = 1000L;

        Video video = new Video();
        ReflectionTestUtils.setField(video, "code", "videoCode");
        ReflectionTestUtils.setField(video, "title", "Test Video");
        ReflectionTestUtils.setField(video, "description", "Test Description");
        ReflectionTestUtils.setField(video, "url", "https://www.youtube.com/watch?v=test");
        ReflectionTestUtils.setField(video, "authorName", "Test Author");
        ReflectionTestUtils.setField(video, "height", 1080);
        ReflectionTestUtils.setField(video, "width", 1920);
        ReflectionTestUtils.setField(video, "thumbnailUrl", "https://example.com/thumbnail.jpg");
        ReflectionTestUtils.setField(video, "thumbnailHeight", 720);
        ReflectionTestUtils.setField(video, "thumbnailWidth", 1280);
        ReflectionTestUtils.setField(video, "createdAt", createdAt);
        ReflectionTestUtils.setField(video, "priority", priority);
        ReflectionTestUtils.setField(video, "user", user);

        // when
        VideoDetailResponseDTO result = videoQueryService.convertToVideoDetailResponseDTO(video);

        // then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("videoCode");
        assertThat(result.title()).isEqualTo("Test Video");
        assertThat(result.description()).isEqualTo("Test Description");
        assertThat(result.url()).isEqualTo("https://www.youtube.com/watch?v=test");
        assertThat(result.authorName()).isEqualTo("Test Author");
        assertThat(result.height()).isEqualTo(1080);
        assertThat(result.width()).isEqualTo(1920);
        assertThat(result.thumbnailUrl()).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(result.thumbnailHeight()).isEqualTo(720);
        assertThat(result.thumbnailWidth()).isEqualTo(1280);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.priority()).isEqualTo(priority);

        UserDetailResponseDTO userDTO = result.user();
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.name()).isEqualTo("testUser");
        assertThat(userDTO.code()).isEqualTo("userCode");
        assertThat(userDTO.isAdmin()).isTrue();
    }
}
