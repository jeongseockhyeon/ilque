package com.ilque.global.response;

import java.time.Instant;
import java.util.List;

import com.ilque.global.error.ErrorCode;

/**
 * 일반 API의 공통 응답 Envelope.
 *
 * <p>성공 시 {@code success=true}, {@code error=null}이며 데이터가 없는 성공 응답은
 * {@code data=null}로 표현한다. 오류 시 {@code success=false}, {@code data=null}이다.
 * {@code timestamp}는 ISO 8601 형식의 UTC 시각 문자열이다.
 */
public record ApiResponseEnvelope<T>(
        boolean success,
        T data,
        ErrorDetail error,
        Instant timestamp
) {
    public static <T> ApiResponseEnvelope<T> success(T data) {
        return new ApiResponseEnvelope<>(
                true,
                data,
                null,
                Instant.now()
        );
    }

    public static <T> ApiResponseEnvelope<T> error(ErrorCode errorCode, String message) {
        return new ApiResponseEnvelope<>(
                false,
                null,
                new ErrorDetail(errorCode.getCode(), message, null),
                Instant.now()
        );
    }

    public static <T> ApiResponseEnvelope<T> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDefaultMessage());
    }

    public static <T> ApiResponseEnvelope<T> validationError(
            String message,
            List<FieldViolation> details
    ) {
        return new ApiResponseEnvelope<>(
                false,
                null,
                new ErrorDetail(ErrorCode.VALIDATION_ERROR.getCode(), message, details),
                Instant.now()
        );
    }
}
