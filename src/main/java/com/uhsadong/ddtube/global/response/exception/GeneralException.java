package com.uhsadong.ddtube.global.response.exception;

import com.uhsadong.ddtube.global.response.code.BaseErrorCode;
import com.uhsadong.ddtube.global.response.code.ErrorReasonDto;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;

    public GeneralException(String message, BaseErrorCode code) {
        super(message);
        this.code = code;
    }

    public GeneralException(BaseErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }


    public ErrorReasonDto getErrorReasonHttpStatus() {
        return this.code.getReasonHttpStatus();
    }

}