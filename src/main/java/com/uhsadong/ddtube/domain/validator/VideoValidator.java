package com.uhsadong.ddtube.domain.validator;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.entity.Video;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoValidator {

    private final UserValidator userValidator;

    public void checkVideosAreDifferent(String videoCode1, String videoCode2) {
        if (videoCode1.equals(videoCode2)) {
            throw new GeneralException(ErrorStatus._TARGET_VIDEO_IS_SAME);
        }
    }

    public void checkPermissionOfVideoUpdate(Playlist playlist, Video video, User user) {
        if (!user.getPlaylist().getId().equals(playlist.getId())) {
            throw new GeneralException(ErrorStatus._USER_NOT_IN_PLAYLIST);
        }

        if (!(video.getUser().getId().equals(user.getId()) || user.isAdmin())) {
            throw new GeneralException(ErrorStatus._VIDEO_DELETE_PERMISSION_DENIED);
        }
    }

    public void checkVideoIsNowPlayingInPlaylist(Playlist playlist, Video video) {
        if (playlist.getNowPlayVideo() != null && video.getId()
            .equals(playlist.getNowPlayVideo().getId())) {
            throw new GeneralException(ErrorStatus._CANNOT_DELETE_NOW_PLAY_VIDEO);
        }
    }
}
