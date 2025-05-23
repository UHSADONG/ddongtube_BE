package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.PlaylistDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistHealthResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistPublicMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.utils.PlaylistUtil;
import com.uhsadong.ddtube.domain.validator.UserValidator;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepositoryService playlistRepositoryService;
    private final UserQueryService userQueryService;
    private final VideoQueryService videoQueryService;
    private final UserValidator userValidator;


    @Transactional(readOnly = true)
    public PlaylistPublicMetaResponseDTO getPlaylistPublicMetaInformation(String playlistCode) {
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        return PlaylistPublicMetaResponseDTO.builder()
            .title(playlist.getTitle())
            .thumbnailUrl(playlist.getThumbnailUrl())
            .description(playlist.getDescription())
            .build();
    }

    @Transactional(readOnly = true)
    public PlaylistMetaResponseDTO getPlaylistMetaInformation(User user, String playlistCode) {
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);
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
        Playlist playlist = playlistRepositoryService.findByCodeOrThrow(playlistCode);
        userValidator.checkUserInPlaylist(playlist, user);

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
        Optional<Playlist> playlistOptional = playlistRepositoryService.findByCodeOptional(
            playlistCode);

        // 플레이리스트가 존재하지 않는 경우
        if (playlistOptional.isEmpty()) {
            return PlaylistHealthResponseDTO.builder()
                .health(PlaylistHealth.NOT_EXIST)
                .playlistCode(playlistCode)
                .title(null)
                .thumbnailUrl(null)
                .description(null)
                .build();
        }

        // 플레이리스트가 존재하는 경우
        PlaylistHealth health = PlaylistUtil.getPlaylistHealth(playlistOptional.get());
        Playlist playlist = playlistOptional.get();
        return PlaylistHealthResponseDTO.builder()
            .health(health)
            .playlistCode(playlistCode)
            .title(playlist.getTitle())
            .thumbnailUrl(playlist.getThumbnailUrl())
            .description(playlist.getDescription())
            .build();
    }

}
