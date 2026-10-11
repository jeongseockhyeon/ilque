package com.ilque.global.support;

import jakarta.validation.constraints.NotBlank;

/**
 * 예외 처리 검증용 요청 DTO. 테스트 소스에서만 사용한다.
 */
public record TestSupportRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title
) {
}
