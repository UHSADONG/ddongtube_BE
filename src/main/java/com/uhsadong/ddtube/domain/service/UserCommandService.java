package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.request.CreateUserRequestDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PlaylistQueryService playlistQueryService;
    private final JwtUtil jwtUtil;
    @Value("${ddtube.user.code_length}")
    private Integer USER_CODE_LENGTH;

    @Transactional
    public String createPlaylistCreator(Playlist playlist, String name, String password) {
        String code = IdGenerator.generateShortId(USER_CODE_LENGTH);
        User user = userRepository.save(
            User.toEntity(playlist, code, name, password, true)
        );
        return jwtUtil.generateAccessToken(user.getCode());
    }

    @Transactional
    public String createPlaylistViewer(String playlistCode, CreateUserRequestDTO requestDTO) {
        Playlist playlist = playlistQueryService.getPlaylistByCodeOrThrow(playlistCode);
        String code = IdGenerator.generateShortId(USER_CODE_LENGTH);
        User user = userRepository.save(
            User.toEntity(playlist, code, requestDTO.name(), requestDTO.password(), false)
        );
        return jwtUtil.generateAccessToken(user.getCode());
    }
}
