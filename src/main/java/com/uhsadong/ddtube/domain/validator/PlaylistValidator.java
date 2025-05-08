package com.uhsadong.ddtube.domain.validator;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaylistValidator {

    private final S3Util s3Util;

    public void checkThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            throw new GeneralException(ErrorStatus._INVALID_THUMBNAIL_URL);
        }
        if (!s3Util.isS3Url(thumbnailUrl)) {
            throw new GeneralException(ErrorStatus._INVALID_THUMBNAIL_URL);
        }
    }

    public void checkVideoInPlaylist(Playlist playlist, Video video){
        if (!playlist.getId().equals(video.getPlaylist().getId())) {
            throw new GeneralException(ErrorStatus._VIDEO_NOT_IN_PLAYLIST);
        }
    }
}
