package com.ilque.global.error;

/**
 * HTTP 409 {@link ErrorCode#RESOURCE_CONFLICT}에 대응하는 예외.
 * 중복 생성, 이미 처리된 요청과의 충돌에 사용한다.
 */
public class ConflictException extends BusinessException {

    public ConflictException() {
        super(ErrorCode.RESOURCE_CONFLICT);
    }

    public ConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, message);
    }
}
