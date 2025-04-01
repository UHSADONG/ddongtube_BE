package com.uhsadong.ddtube.domain.service;

import com.uhsadong.ddtube.domain.dto.request.CreateUserRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreateUserResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.JwtUtil;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PlaylistQueryService playlistQueryService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    @Value("${ddtube.user.code_length}")
    private Integer USER_CODE_LENGTH;

    @Transactional
    public String createPlaylistCreator(Playlist playlist, String name, String password) {
        String code = IdGenerator.generateShortId(USER_CODE_LENGTH);
        User user = userRepository.save(
            User.toEntity(playlist, code, name, passwordEncoder.encode(password), true)
        );
        return jwtUtil.generateAccessToken(user.getCode());
    }

    /**
     * 존재하는 사용자면 로그인 절차 / 존재하지 않는 사용자면 회원가입 절차
     * <p>따라서 회원가입 시 이름 중복 체크 안해도 됨</p>
     */
    @Transactional
    public CreateUserResponseDTO getJwtTokenBySignInUp(String playlistCode, CreateUserRequestDTO requestDTO) {
        Optional<User> optionalUser = userRepository.findFirstByPlaylistCodeAndName(playlistCode,
            requestDTO.name());
        if (optionalUser.isPresent()) { // 사용자 데이터가 있으면 로그인
            return signIn(optionalUser.get(), requestDTO.password());
        }
        // 없으면 회원가입
        return signUp(playlistCode, requestDTO);
    }

    /**
     * 사용자 데이터가 존재할 때에는 로그인을 시도한다.
     */
    private CreateUserResponseDTO signIn(User user, String password) {
        // password는 인코딩 전, user.getPassword는 인코딩 후
        if (passwordEncoder.matches(password, user.getPassword())) {
            return CreateUserResponseDTO.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getCode()))
                .isAdmin(user.isAdmin())
                .build();
        }
        throw new GeneralException(ErrorStatus._USER_ALREADY_EXISTS);
    }

    /**
     * 사용자 데이터가 존재하지 않을 때에는 회원가입을 시도한다.
     */
    private CreateUserResponseDTO signUp(String playlistCode, CreateUserRequestDTO requestDTO) {
        Playlist playlist = playlistQueryService.getPlaylistByCodeOrThrow(playlistCode);
        String code = IdGenerator.generateShortId(USER_CODE_LENGTH);
        User user = userRepository.save(
            User.toEntity(playlist, code, requestDTO.name(),
                passwordEncoder.encode(requestDTO.password()), false)
        );
        return CreateUserResponseDTO.builder()
            .accessToken(jwtUtil.generateAccessToken(user.getCode()))
            .isAdmin(user.isAdmin())
            .build();
    }


}
