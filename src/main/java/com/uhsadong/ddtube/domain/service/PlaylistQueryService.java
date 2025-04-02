package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.PlaylistDetailResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.PlaylistPublicMetaResponseDTO;
import com.uhsadong.ddtube.domain.dto.response.VideoDetailResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;
    private final UserQueryService userQueryService;
    private final VideoQueryService videoQueryService;

    public Playlist getPlaylistByCodeOrThrow(String code) {
        return playlistRepository.findFirstByCode(code)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
    }

    public PlaylistPublicMetaResponseDTO getPlaylistPublicMetaInformation(String playlistCode) {
        Playlist playlist = getPlaylistByCodeOrThrow(playlistCode);
        return PlaylistPublicMetaResponseDTO.builder()
            .title(playlist.getTitle())
            .thumbnailUrl(playlist.getThumbnailUrl())
            .description(playlist.getDescription())
            .build();
    }

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

    @Transactional
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

}
