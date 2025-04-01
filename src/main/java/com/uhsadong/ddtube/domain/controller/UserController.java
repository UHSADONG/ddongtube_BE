package com.uhsadong.ddtube.domain.controller;

import com.uhsadong.ddtube.domain.dto.request.CreateUserRequestDTO;
import com.uhsadong.ddtube.domain.dto.response.CreateUserResponseDTO;
import com.uhsadong.ddtube.domain.service.UserCommandService;
import com.uhsadong.ddtube.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserCommandService userCommandService;

    @PostMapping("/{playlistCode}")
    @Operation(summary = "사용자 로그인/회원가입", description = "사용자 정보를 추가합니다. 이미 있는 계정이라면 비밀번호를 확인한 뒤 데이터를 추가합니다. JWT를 반환합니다.")
    public ResponseEntity<ApiResponse<CreateUserResponseDTO>> signIn(
        @PathVariable String playlistCode,
        @RequestBody @Valid CreateUserRequestDTO createUserRequestDTO
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(
            userCommandService.getJwtTokenBySignInUp(playlistCode, createUserRequestDTO)
        ));
    }


}
