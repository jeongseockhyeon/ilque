package com.ilque.global.response;

import java.util.List;

/**
 * 공통 Envelope의 오류 객체. {@code details}는 필드별 유효성 검사 오류가 있을 때만
 * {@link FieldViolation} 배열을 담고, 일반 오류에서는 {@code null}이다.
 */
public record ErrorDetail(
        String code,
        String message,
        List<FieldViolation> details
) {
}
