package com.uhsadong.ddtube.domain.validator;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    public void checkUserIsAdminOfPlaylist(Playlist playlist, User user) {
        checkUserInPlaylist(playlist, user);
        if (!user.isAdmin()) {
            throw new GeneralException(ErrorStatus._PLAYLIST_DELETE_PERMISSION_DENIED);
        }
    }

    public void checkUserInPlaylist(Playlist playlist, User user) {
        if (!Objects.equals(user.getPlaylist().getId(), playlist.getId())) {
            throw new GeneralException(ErrorStatus._USER_NOT_IN_PLAYLIST);
        }
    }

}
