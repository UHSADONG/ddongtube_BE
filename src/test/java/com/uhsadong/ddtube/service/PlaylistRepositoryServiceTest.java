package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistRepositoryServiceTest {

    @InjectMocks
    private PlaylistRepositoryService playlistRepositoryService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Test
    @DisplayName("플레이리스트 저장 - 저장 요청이 성공적으로 수행된다")
    void save_success() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        when(playlistRepository.save(playlist)).thenReturn(playlist);

        // when
        Playlist result = playlistRepositoryService.save(playlist);

        // then
        assertThat(result)
            .isNotNull()
            .isEqualTo(playlist);

        verify(playlistRepository).save(playlist);
    }

    @Test
    @DisplayName("마지막 로그인 시간 업데이트 - 업데이트 요청이 성공적으로 수행된다")
    void updateLastLoginAtToNowByPlaylistCode_success() {
        // given
        String playlistCode = "playlistCode";

        // when
        playlistRepositoryService.updateLastLoginAtToNowByPlaylistCode(playlistCode);

        // then
        verify(playlistRepository).updateLastLoginAtByPlaylistCode(any(String.class),
            any(LocalDateTime.class));
    }

    @Test
    @DisplayName("코드로 플레이리스트 조회 (옵셔널) - 존재하는 플레이리스트가 반환된다")
    void findByCodeOptional_existingPlaylist_returnsOptionalPlaylist() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        when(playlistRepository.findFirstByCode(playlistCode)).thenReturn(Optional.of(playlist));

        // when
        Optional<Playlist> result = playlistRepositoryService.findByCodeOptional(playlistCode);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(playlist);

        verify(playlistRepository).findFirstByCode(playlistCode);
    }

    @Test
    @DisplayName("코드로 플레이리스트 조회 (옵셔널) - 존재하지 않는 플레이리스트의 경우 빈 옵셔널이 반환된다")
    void findByCodeOptional_nonExistingPlaylist_returnsEmptyOptional() {
        // given
        String playlistCode = "nonExistingCode";

        when(playlistRepository.findFirstByCode(playlistCode)).thenReturn(Optional.empty());

        // when
        Optional<Playlist> result = playlistRepositoryService.findByCodeOptional(playlistCode);

        // then
        assertThat(result).isEmpty();

        verify(playlistRepository).findFirstByCode(playlistCode);
    }

    @Test
    @DisplayName("코드로 플레이리스트 조회 (예외 발생) - 존재하는 플레이리스트가 반환된다")
    void findByCodeOrThrow_existingPlaylist_returnsPlaylist() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        when(playlistRepository.findFirstByCode(playlistCode)).thenReturn(Optional.of(playlist));

        // when
        Playlist result = playlistRepositoryService.findByCodeOrThrow(playlistCode);

        // then
        assertThat(result)
            .isNotNull()
            .isEqualTo(playlist);

        verify(playlistRepository).findFirstByCode(playlistCode);
    }

    @Test
    @DisplayName("코드로 플레이리스트 조회 (예외 발생) - 존재하지 않는 플레이리스트의 경우 예외가 발생한다")
    void findByCodeOrThrow_nonExistingPlaylist_throwsException() {
        // given
        String playlistCode = "nonExistingCode";

        when(playlistRepository.findFirstByCode(playlistCode)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> playlistRepositoryService.findByCodeOrThrow(playlistCode))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._PLAYLIST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("플레이리스트 삭제 - 삭제 요청이 성공적으로 수행된다")
    void delete_success() {
        // given
        String playlistCode = "playlistCode";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        // when
        playlistRepositoryService.delete(playlist);

        // then
        verify(playlistRepository).delete(playlist);
    }
}
