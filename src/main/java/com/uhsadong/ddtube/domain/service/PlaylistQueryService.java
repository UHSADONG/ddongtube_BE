package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.PlaylistDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistHealthResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistPublicMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;
    private final UserQueryService userQueryService;
    private final VideoQueryService videoQueryService;

    @Value("${ddtube.playlist.delete_days}")
    private Integer PLAYLIST_DELETE_DAYS;

    public Playlist getPlaylistByCodeOrThrow(String code) {
        return playlistRepository.findFirstByCode(code)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
    }

    public Optional<Playlist> getPlaylistByCodeOptional(String code) {
        return playlistRepository.findFirstByCode(code);
    }

    @Transactional(readOnly = true)
    public PlaylistPublicMetaResponseDTO getPlaylistPublicMetaInformation(String playlistCode) {
        Playlist playlist = getPlaylistByCodeOrThrow(playlistCode);
        return PlaylistPublicMetaResponseDTO.builder()
            .title(playlist.getTitle())
            .thumbnailUrl(playlist.getThumbnailUrl())
            .description(playlist.getDescription())
            .build();
    }

    @Transactional(readOnly = true)
    public PlaylistMetaResponseDTO getPlaylistMetaInformation(User user, String playlistCode) {
        Playlist playlist = getPlaylistByCodeOrThrow(playlistCode);
        userQueryService.checkUserInPlaylist(user, playlist);
        List<User> userList = userQueryService.getUserListByPlaylistCode(playlistCode);

        String ownerName = userList.stream()
            .filter(User::isAdmin)
            .findFirst()
            .map(User::getName)
            .orElseThrow(() -> new GeneralException(ErrorStatus._CREATOR_NOT_FOUND));

        List<String> userNameList = userList.stream()
            .filter(u -> !u.isAdmin())
            .map(User::getName)
            .toList();

        return PlaylistMetaResponseDTO.builder()
            .title(playlist.getTitle())
            .thumbnailUrl(playlist.getThumbnailUrl())
            .description(playlist.getDescription())
            .owner(ownerName)
            .userList(userNameList)
            .build();
    }

    @Transactional(readOnly = true)
    public PlaylistDetailResponseDTO getPlaylistDetail(User user, String playlistCode) {
        Playlist playlist = getPlaylistByCodeOrThrow(playlistCode);
        userQueryService.checkUserInPlaylist(user, playlist);

        List<VideoDetailResponseDTO> videoResponseList = videoQueryService.getVideoDetailListByPlaylistCode(
            playlistCode);

        // 현재 실행중인 비디오 코드를 반환함
        String nowPlayingVideoCode =
            playlist.getNowPlayVideo() == null
                ? null
                : playlist.getNowPlayVideo().getCode();

        return PlaylistDetailResponseDTO.builder()
            .title(playlist.getTitle())
            .nowPlayingVideoCode(nowPlayingVideoCode)
            .videoList(videoResponseList)
            .build();
    }

    @Transactional(readOnly = true)
    public PlaylistHealthResponseDTO checkPlaylistHealth(String playlistCode) {
        Optional<Playlist> playlistOptional = getPlaylistByCodeOptional(playlistCode);

        // 플레이리스트가 존재하지 않는 경우
        if (playlistOptional.isEmpty()) {
            return PlaylistHealthResponseDTO.builder()
                .health(PlaylistHealth.NOT_EXIST)
                .playlistCode(playlistCode)
                .build();
        }

        // 플레이리스트가 존재하는 경우
        Playlist playlist = playlistOptional.get();
        PlaylistHealth health;

        // 마지막으로 활성화된 시간 기준으로 PLAYLIST_DELETE_DAYS가 지났으면 INACTIVE 상태
        if (playlist.getLastLoginAt().plusDays(PLAYLIST_DELETE_DAYS)
            .isBefore(LocalDateTime.now())) {
            health = PlaylistHealth.INACTIVE;
        } else {
            // 그 외의 경우 ACTIVE 상태
            health = PlaylistHealth.ACTIVE;
        }

        return PlaylistHealthResponseDTO.builder()
            .health(health)
            .playlistCode(playlistCode)
            .build();
    }

}
