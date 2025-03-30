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

    // JWT
    _EMPTY_JWT(HttpStatus.UNAUTHORIZED, "COMMON404", "토큰이 비어있습니다."),
    _INVALID_JWT(HttpStatus.UNAUTHORIZED, "COMMON405", "유효하지 않은 토큰입니다."),

    // S3
    _FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "S3001", "파일 업로드에 실패했습니다."),
    _INVALID_THUMBNAIL_URL(HttpStatus.BAD_REQUEST, "S3002", "유효하지 않은 썸네일 URL입니다."),
    _S3_IO_EXCEPTION(HttpStatus.BAD_REQUEST, "S3003", "S3 IO 예외가 발생했습니다."),
    _S3_COMMUNICATION_EXCEPTION(HttpStatus.BAD_REQUEST, "S3004", "S3 통신 중 예외가 발생했습니다."),


    // PLAYLIST
    _PLAYLIST_NOT_FOUND(HttpStatus.BAD_REQUEST, "PLAYLIST001", "해당 플레이리스트를 찾을 수 없습니다."),
    _PLAYLIST_DELETE_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "PLAYLIST002", "해당 플레이리스트를 삭제할 권한이 없습니다."),

    // USER
    _USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER001", "이미 존재하는 사용자이며, 비밀번호가 틀렸습니다."),
    _USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER002", "해당 사용자를 찾을 수 없습니다."),
    _CREATOR_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER003", "해당 플레이리스트의 생성자를 찾을 수 없습니다."),
    _USER_NOT_IN_PLAYLIST(HttpStatus.BAD_REQUEST, "USER004", "해당 사용자는 해당 플레이리스트에 속해있지 않습니다."),

    // VIDEO
    _VIDEO_NOT_FOUND(HttpStatus.BAD_REQUEST, "VIDEO001", "해당 비디오를 찾을 수 없습니다."),
    _VIDEO_DELETE_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "VIDEO002", "해당 비디오를 삭제할 권한이 없습니다."),
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