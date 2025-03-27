package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.response.PlaylistMetaResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.PlaylistRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistQueryService {

    private final PlaylistRepository playlistRepository;
    private final UserQueryService userQueryService;

    public Playlist getPlaylistByCodeOrThrow(String code) {
        return playlistRepository.findFirstByCode(code)
            .orElseThrow(() -> new GeneralException(ErrorStatus._PLAYLIST_NOT_FOUND));
    }

    public PlaylistMetaResponseDTO getPlaylistMetaInformation(User user, String playlistCode) {
        Playlist playlist = getPlaylistByCodeOrThrow(playlistCode);
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
            .owner(ownerName)
            .userList(userNameList)
            .build();

    }

}
