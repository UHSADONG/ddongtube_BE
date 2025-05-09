package com.uhsadong.ddtube.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.validator.PlaylistValidator;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.S3Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlaylistValidatorTest {

    @InjectMocks
    private PlaylistValidator playlistValidator;

    @Mock
    private S3Util s3Util;

    @Mock
    private Playlist playlist;

    @Mock
    private Video video;

    @Mock
    private Playlist otherPlaylist;

    @Test
    @DisplayName("유효한 썸네일 URL이 확인되면 예외가 발생하지 않는다")
    void checkThumbnailUrl_withValidUrl_shouldNotThrowException() {
        // given
        String validThumbnailUrl = "https://s3-bucket-name.s3.region.amazonaws.com/image.jpg";
        when(s3Util.isS3Url(validThumbnailUrl)).thenReturn(true);

        // when & then
        playlistValidator.checkThumbnailUrl(validThumbnailUrl);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("null 썸네일 URL이 입력되면 예외가 발생한다")
    void checkThumbnailUrl_withNullUrl_shouldThrowException() {
        // given
        String nullThumbnailUrl = null;

        // when & then
        assertThatThrownBy(() -> playlistValidator.checkThumbnailUrl(nullThumbnailUrl))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._INVALID_THUMBNAIL_URL.getMessage());
    }

    @Test
    @DisplayName("빈 썸네일 URL이 입력되면 예외가 발생한다")
    void checkThumbnailUrl_withEmptyUrl_shouldThrowException() {
        // given
        String emptyThumbnailUrl = "";

        // when & then
        assertThatThrownBy(() -> playlistValidator.checkThumbnailUrl(emptyThumbnailUrl))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._INVALID_THUMBNAIL_URL.getMessage());
    }

    @Test
    @DisplayName("S3 형식이 아닌 썸네일 URL이 입력되면 예외가 발생한다")
    void checkThumbnailUrl_withNonS3Url_shouldThrowException() {
        // given
        String nonS3ThumbnailUrl = "https://example.com/image.jpg";
        when(s3Util.isS3Url(nonS3ThumbnailUrl)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> playlistValidator.checkThumbnailUrl(nonS3ThumbnailUrl))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._INVALID_THUMBNAIL_URL.getMessage());
    }

    @Test
    @DisplayName("비디오가 동일한 플레이리스트에 속해 있으면 예외가 발생하지 않는다")
    void checkVideoInPlaylist_videoInSamePlaylist_shouldNotThrowException() {
        // given
        ReflectionTestUtils.setField(playlist, "id", 1L);

        when(video.getPlaylist()).thenReturn(playlist);

        // when & then
        playlistValidator.checkVideoInPlaylist(playlist, video);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("비디오가 다른 플레이리스트에 속해 있으면 예외가 발생한다")
    void checkVideoInPlaylist_videoInDifferentPlaylist_shouldThrowException() {
        // given
        ReflectionTestUtils.setField(playlist, "id", 1L);
        ReflectionTestUtils.setField(otherPlaylist, "id", 2L);

        // video가 otherPlaylist를 반환하도록 설정
        when(video.getPlaylist()).thenReturn(otherPlaylist);

        // 추가로 반환되는 ID 값도 설정
        when(video.getPlaylist().getId()).thenReturn(2L);

        // when & then
        assertThatThrownBy(() -> playlistValidator.checkVideoInPlaylist(playlist, video))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._VIDEO_NOT_IN_PLAYLIST.getMessage());
    }
}
