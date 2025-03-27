package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;

    public List<User> getUserListByPlaylistCode(String playlistCode) {
        return userRepository.findAllByPlaylistCode(playlistCode);
    }
}
