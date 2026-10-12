package com.ilque.global.error;

/**
 * 오류 코드를 담는 공통 비즈니스 예외. {@link GlobalExceptionHandler}에서
 * 오류 코드의 HTTP 상태와 메시지로 변환한다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
