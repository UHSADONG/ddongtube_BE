package com.uhsadong.ddtube.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uhsadong.ddtube.domain.dto.request.CreateUserRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreateUserResponseDTO;
import com.uhsadong.ddtube.domain.entity.Playlist;
import com.uhsadong.ddtube.domain.entity.User;
import com.uhsadong.ddtube.domain.repository.UserRepository;
import com.uhsadong.ddtube.domain.repositoryservice.PlaylistRepositoryService;
import com.uhsadong.ddtube.domain.service.UserCommandService;
import com.uhsadong.ddtube.global.response.code.status.ErrorStatus;
import com.uhsadong.ddtube.global.response.exception.GeneralException;
import com.uhsadong.ddtube.global.util.IdGenerator;
import com.uhsadong.ddtube.global.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private final Integer userCodeLength = 8;
    @InjectMocks
    private UserCommandService userCommandService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlaylistRepositoryService playlistRepositoryService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userCommandService, "USER_CODE_LENGTH", userCodeLength);
    }

    @Test
    @DisplayName("플레이리스트 생성자(관리자) 생성 - 생성자 정보를 저장하고 액세스 토큰을 반환한다")
    void createPlaylistCreator_success() {
        // given
        String generatedCode = "userCode";
        String playlistCode = "playlistCode";
        String userName = "admin";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String accessToken = "access-token";

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User savedUser = User.toEntity(playlist, generatedCode, userName, encodedPassword, true);

        try (MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class)) {
            idGeneratorMock.when(() -> IdGenerator.generateShortId(userCodeLength))
                .thenReturn(generatedCode);

            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtUtil.generateAccessToken(generatedCode, playlistCode, userName)).thenReturn(
                accessToken);

            // when
            String result = userCommandService.createPlaylistCreator(playlist, userName, password);

            // then
            assertThat(result).isEqualTo(accessToken);

            verify(passwordEncoder).encode(password);
            verify(userRepository).save(any(User.class));
            verify(jwtUtil).generateAccessToken(generatedCode, playlistCode, userName);
        }
    }

    @Test
    @DisplayName("사용자 로그인/회원가입 - 존재하는 사용자는 로그인 처리된다")
    void getJwtTokenBySignInUp_existingUser_logIn() {
        // given
        String playlistCode = "playlistCode";
        String userName = "testUser";
        String password = "password123";
        String userCode = "user123";
        boolean isAdmin = false;
        String accessToken = "access-token";

        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO(userName, password);

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User existingUser = User.toEntity(playlist, userCode, userName, "encodedPassword", isAdmin);

        when(userRepository.findFirstByPlaylistCodeAndName(playlistCode, userName))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(userCode, playlistCode, userName)).thenReturn(accessToken);

        // when
        CreateUserResponseDTO responseDTO = userCommandService.getJwtTokenBySignInUp(playlistCode,
            requestDTO);

        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.accessToken()).isEqualTo(accessToken);
        assertThat(responseDTO.isAdmin()).isEqualTo(isAdmin);

        verify(userRepository).findFirstByPlaylistCodeAndName(playlistCode, userName);
        verify(passwordEncoder).matches(password, existingUser.getPassword());
        verify(jwtUtil).generateAccessToken(userCode, playlistCode, userName);
    }

    @Test
    @DisplayName("사용자 로그인/회원가입 - 존재하는 사용자가 잘못된 비밀번호로 로그인 시도 시 예외가 발생한다")
    void getJwtTokenBySignInUp_existingUserWrongPassword_throwsException() {
        // given
        String playlistCode = "playlistCode";
        String userName = "testUser";
        String password = "wrongPassword";
        String userCode = "user123";

        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO(userName, password);

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User existingUser = User.toEntity(playlist, userCode, userName, "encodedPassword", false);

        when(userRepository.findFirstByPlaylistCodeAndName(playlistCode, userName))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(password, existingUser.getPassword())).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> userCommandService.getJwtTokenBySignInUp(playlistCode, requestDTO))
            .isInstanceOf(GeneralException.class)
            .hasMessage(ErrorStatus._USER_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("사용자 로그인/회원가입 - 존재하지 않는 사용자는 회원가입 처리된다")
    void getJwtTokenBySignInUp_newUser_signUp() {
        // given
        String playlistCode = "playlistCode";
        String userName = "newUser";
        String password = "password123";
        String generatedCode = "user123";
        String encodedPassword = "encodedPassword";
        String accessToken = "access-token";
        boolean isAdmin = false;

        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO(userName, password);

        Playlist playlist = Playlist.toEntity(
            playlistCode,
            "Test Playlist",
            "Test Description",
            "https://example.com/thumbnail.jpg",
            LocalDateTime.now()
        );

        User newUser = User.toEntity(playlist, generatedCode, userName, encodedPassword, isAdmin);

        try (MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class)) {
            idGeneratorMock.when(() -> IdGenerator.generateShortId(userCodeLength))
                .thenReturn(generatedCode);

            when(userRepository.findFirstByPlaylistCodeAndName(playlistCode, userName))
                .thenReturn(Optional.empty());
            when(playlistRepositoryService.findByCodeOrThrow(playlistCode)).thenReturn(playlist);
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(jwtUtil.generateAccessToken(generatedCode, playlistCode, userName)).thenReturn(
                accessToken);

            // when
            CreateUserResponseDTO responseDTO = userCommandService.getJwtTokenBySignInUp(
                playlistCode, requestDTO);

            // then
            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.accessToken()).isEqualTo(accessToken);
            assertThat(responseDTO.isAdmin()).isEqualTo(isAdmin);

            verify(userRepository).findFirstByPlaylistCodeAndName(playlistCode, userName);
            verify(playlistRepositoryService).findByCodeOrThrow(playlistCode);
            verify(passwordEncoder).encode(password);
            verify(userRepository).save(any(User.class));
            verify(jwtUtil).generateAccessToken(generatedCode, playlistCode, userName);
        }
    }
}
