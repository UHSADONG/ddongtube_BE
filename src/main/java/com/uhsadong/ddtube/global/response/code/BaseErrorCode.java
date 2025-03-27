package com.uhsadong.ddtube.global.response.code;

public interface BaseErrorCode {
    ErrorReasonDto getReasonHttpStatus();
    String getMessage();
}
