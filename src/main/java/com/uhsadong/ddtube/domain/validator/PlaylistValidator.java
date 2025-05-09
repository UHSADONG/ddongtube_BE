package com.uhsadong.ddtube.domain.validator;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.domain.enums.PlaylistHealth;
import com.uhsadong.ddtube.domain.utils.PlaylistUtil;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.S3Util;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistValidator {

    private final S3Util s3Util;

    public void checkThumbnailUrl(String thumbnailUrl) {
        if (Objects.isNull(thumbnailUrl)) {
            throw new GeneralException(ErrorStatus._INVALID_THUMBNAIL_URL);
        }
        if (!s3Util.isS3Url(thumbnailUrl)) {
            throw new GeneralException(ErrorStatus._INVALID_THUMBNAIL_URL);
        }
    }

    public void checkVideoInPlaylist(Playlist playlist, Video video) {
        if (!Objects.equals(playlist.getId(), video.getPlaylist().getId())) {
            throw new GeneralException(ErrorStatus._VIDEO_NOT_IN_PLAYLIST);
        }
    }

    public void checkPlaylistIsInactive(Playlist playlist) {
        if (PlaylistUtil.getPlaylistHealth(playlist) == PlaylistHealth.ACTIVE) {
            throw new GeneralException(ErrorStatus._PLAYLIST_IS_ACTIVE);
        }
    }
}
