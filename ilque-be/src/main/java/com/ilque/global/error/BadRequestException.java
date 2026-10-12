package com.ilque.global.error;

/**
 * HTTP 400 {@link ErrorCode#BAD_REQUEST}에 대응하는 예외.
 * 잘못된 요청 값, 잘못된 상태 전이, 완료 조건 미충족에 사용한다.
 */
public class BadRequestException extends BusinessException {

    public BadRequestException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
