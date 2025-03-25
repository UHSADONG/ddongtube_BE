package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.global.util.IdGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    @Value("${ddtube.user.code_length}")
    private Integer USER_CODE_LENGTH;

    private final UserRepository userRepository;

    @Transactional
    public User createPlaylistCreator(Playlist playlist, String name, String password) {
        String code = IdGenerator.generateShortId(USER_CODE_LENGTH);
        return userRepository.save(
            User.toEntity(playlist, code, name, password, true)
        );
    }
}
