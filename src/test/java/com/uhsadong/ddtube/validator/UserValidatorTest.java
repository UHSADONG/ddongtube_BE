package com.uhsadong.ddtube.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.validator.UserValidator;
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
class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;

    @Mock
    private Playlist playlist;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 기본 ID 설정
        ReflectionTestUtils.setField(playlist, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    @DisplayName("관리자가 플레이리스트의 권한을 확인하면 예외가 발생하지 않는다")
    void checkUserIsAdminOfPlaylist_withAdmin_shouldNotThrowException() {
        // given
        when(user.isAdmin()).thenReturn(true);
        when(user.getPlaylist()).thenReturn(playlist);

        // when & then
        userValidator.checkUserIsAdminOfPlaylist(playlist, user);
        // 예외가 발생하지 않으면 테스트 성공
    }

    @Test
    @DisplayName("일반 사용자가 플레이리스트의 관리자 권한을 확인하면 예외가 발생한다")
    void checkUserIsAdminOfPlaylist_withNonAdmin_shouldThrowException() {
        // given
        when(user.isAdmin()).thenReturn(false);
        when(user.getPlaylist()).thenReturn(playlist);

        // when & then
        assertThatThrownBy(() -> userValidator.checkUserIsAdminOfPlaylist(playlist, user))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._PLAYLIST_DELETE_PERMISSION_DENIED.getMessage());
    }

    @Test
    @DisplayName("사용자가 같은 플레이리스트에 속해 있으면 예외가 발생하지 않는다")
    void checkUserInPlaylist_userInSamePlaylist_shouldNotThrowException() {

        // given
        when(user.getPlaylist()).thenReturn(playlist);

        // when & then
        userValidator.checkUserInPlaylist(playlist, user);
    }

    @Test
    @DisplayName("사용자가 다른 플레이리스트에 속해 있으면 예외가 발생한다")
    void checkUserInPlaylist_userInDifferentPlaylist_shouldThrowException() {
        // given
        Playlist otherPlaylist = new Playlist();
        ReflectionTestUtils.setField(otherPlaylist, "id", 2L);
        
        when(user.getPlaylist()).thenReturn(otherPlaylist);

        // when & then
        assertThatThrownBy(() -> userValidator.checkUserInPlaylist(playlist, user))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._USER_NOT_IN_PLAYLIST.getMessage());
    }
}
