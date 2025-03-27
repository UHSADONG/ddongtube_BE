package com.uhsadong.ddtube.global.response.code.status;


import com.uhsadong.ddtube.global.response.code.BaseErrorCode;
import com.uhsadong.ddtube.global.response.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 일반 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _EMPTY_JWT(HttpStatus.UNAUTHORIZED, "COMMON404", "토큰이 비어있습니다."),
    _INVALID_JWT(HttpStatus.UNAUTHORIZED, "COMMON405", "유효하지 않은 토큰입니다."),

    // PLAYLIST
    _PLAYLIST_NOT_FOUND(HttpStatus.BAD_REQUEST, "PLAYLIST001", "해당 플레이리스트를 찾을 수 없습니다."),

    // USER
    _USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER001", "이미 존재하는 사용자이며, 비밀번호가 틀렸습니다."),
    _USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER002", "해당 사용자를 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
            .message(message)
            .code(code)
            .isSuccess(false)
            .httpStatus(httpStatus)
            .build();
    }
}