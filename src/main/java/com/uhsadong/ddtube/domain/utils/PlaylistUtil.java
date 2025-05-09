package com.uhsadong.ddtube.domain.utils;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import java.time.LocalDateTime;
import java.util.Objects;

public class PlaylistUtil {

    public static PlaylistHealth getPlaylistHealth(Playlist playlist) {
        int playlistDeleteDays = 1;
        // 마지막으로 활성화된 시간 기준으로 PLAYLIST_DELETE_DAYS가 지났으면 INACTIVE 상태
        if (Objects.isNull(playlist)){
            return PlaylistHealth.NOT_EXIST;
        }
        else if (playlist.getLastLoginAt().plusDays(playlistDeleteDays)
            .isBefore(LocalDateTime.now())) {
            return PlaylistHealth.INACTIVE;
        }
        return PlaylistHealth.ACTIVE;
    }
}
