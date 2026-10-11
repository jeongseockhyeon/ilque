package com.ilque.global.response;

/**
 * 필드별 유효성 검사 오류 항목.
 */
public record FieldViolation(
        String field,
        String message
) {
}
