package com.uhsadong.ddtube.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.domain.validator.VideoValidator;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoValidatorTest {

    @InjectMocks
    private VideoValidator videoValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private Playlist playlist;

    @Mock
    private User user;

    @Mock
    private User otherUser;

    @Mock
    private Video video;

    @BeforeEach
    void setUp() {
        // ID 설정
        ReflectionTestUtils.setField(playlist, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(otherUser, "id", 2L);
        ReflectionTestUtils.setField(video, "id", 1L);
    }

    @Test
    @DisplayName("두 개의 다른 비디오 코드를 확인하면 예외가 발생하지 않는다")
    void checkVideosAreDifferent_withDifferentCodes_shouldNotThrowException() {
        // given
        String videoCode1 = "video1";
        String videoCode2 = "video2";

        // when & then
        videoValidator.checkVideosAreDifferent(videoCode1, videoCode2);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("두 개의 같은 비디오 코드를 확인하면 예외가 발생한다")
    void checkVideosAreDifferent_withSameCodes_shouldThrowException() {
        // given
        String videoCode = "video1";

        // when & then
        assertThatThrownBy(() -> videoValidator.checkVideosAreDifferent(videoCode, videoCode))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._TARGET_VIDEO_IS_SAME.getMessage());
    }

    @Test
    @DisplayName("비디오 소유자가 업데이트 권한을 확인하면 예외가 발생하지 않는다")
    void checkPermissionOfVideoUpdate_withVideoOwner_shouldNotThrowException() {
        // given
        when(user.getId()).thenReturn(1L);

        when(video.getUser()).thenReturn(user);

        // when & then
        videoValidator.checkPermissionOfVideoUpdate(video, user);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("관리자가 다른 사용자의 비디오 업데이트 권한을 확인하면 예외가 발생하지 않는다")
    void checkPermissionOfVideoUpdate_withAdmin_shouldNotThrowException() {
        // given
        when(user.isAdmin()).thenReturn(true);
        when(user.getId()).thenReturn(1L);

        when(video.getUser()).thenReturn(otherUser);
        when(otherUser.getId()).thenReturn(2L); // 다른 ID 명시적 설정

        // when & then
        videoValidator.checkPermissionOfVideoUpdate(video, user);
        // 예외가 발생하지 않으면 테스트 성공
    }


    @Test
    @DisplayName("일반 사용자가 다른 사용자의 비디오 업데이트를 시도하면 예외가 발생한다")
    void checkPermissionOfVideoUpdate_nonOwnerNonAdmin_shouldThrowException() {
        when(user.isAdmin()).thenReturn(false);
        when(user.getId()).thenReturn(1L);

        when(video.getUser()).thenReturn(otherUser);
        when(otherUser.getId()).thenReturn(2L); // 다른 사용자 ID

        // when & then
        assertThatThrownBy(() -> videoValidator.checkPermissionOfVideoUpdate(video, user))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._VIDEO_DELETE_PERMISSION_DENIED.getMessage());
    }

    @Test
    @DisplayName("현재 재생 중이 아닌 비디오를 확인하면 예외가 발생하지 않는다")
    void checkVideoIsNowPlayingInPlaylist_withNonPlayingVideo_shouldNotThrowException() {
        // given
        when(playlist.getNowPlayVideo()).thenReturn(null);

        // when & then
        videoValidator.checkVideoIsNowPlayingInPlaylist(playlist, video);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("다른 비디오가 현재 재생 중일 때 확인하면 예외가 발생하지 않는다")
    void checkVideoIsNowPlayingInPlaylist_withDifferentPlayingVideo_shouldNotThrowException() {
        // given
        Video playingVideo = new Video();
        ReflectionTestUtils.setField(playingVideo, "id", 2L);

        when(playlist.getNowPlayVideo()).thenReturn(playingVideo);

        // when & then
        videoValidator.checkVideoIsNowPlayingInPlaylist(playlist, video);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("현재 재생 중인 비디오를 확인하면 예외가 발생한다")
    void checkVideoIsNowPlayingInPlaylist_withCurrentlyPlayingVideo_shouldThrowException() {
        // given
        when(playlist.getNowPlayVideo()).thenReturn(video);

        // when & then
        assertThatThrownBy(() -> videoValidator.checkVideoIsNowPlayingInPlaylist(playlist, video))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._CANNOT_DELETE_NOW_PLAY_VIDEO.getMessage());
    }
}
