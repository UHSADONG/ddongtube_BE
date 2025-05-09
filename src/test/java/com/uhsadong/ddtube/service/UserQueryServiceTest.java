package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.domain.service.UserQueryService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks
    private UserQueryService userQueryService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 플레이리스트의 사용자 목록 조회 - 플레이리스트 코드에 해당하는 모든 사용자를 반환한다")
    void getUserListByPlaylistCode_success() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User adminUser = User.toEntity(playlist, "admin123", "adminUser", "password", true);
        User normalUser1 = User.toEntity(playlist, "user1", "normalUser1", "password", false);
        User normalUser2 = User.toEntity(playlist, "user2", "normalUser2", "password", false);

        List<User> expectedUsers = Arrays.asList(adminUser, normalUser1, normalUser2);

        when(userRepository.findAllByPlaylistCode(playlistCode)).thenReturn(expectedUsers);

        // when
        List<User> result = userQueryService.getUserListByPlaylistCode(playlistCode);

        // then
        assertThat(result)
            .isNotNull()
            .hasSize(3)
            .isEqualTo(expectedUsers);

        verify(userRepository).findAllByPlaylistCode(playlistCode);
    }
}
