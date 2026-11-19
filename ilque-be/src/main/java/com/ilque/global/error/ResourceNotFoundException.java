package com.ilque.global.error;

/**
 * HTTP 404 {@link ErrorCode#RESOURCE_NOT_FOUND}에 대응하는 예외.
 * 존재하지 않는 리소스 조회 시 사용한다.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
