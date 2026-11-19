package com.ilque.global.support;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilque.global.error.BadRequestException;
import com.ilque.global.error.ConflictException;
import com.ilque.global.error.ResourceNotFoundException;
import com.ilque.global.response.ApiResponseEnvelope;

/**
 * 전역 예외 처리 검증용 컨트롤러. 테스트 소스에서만 사용하며 실제 API가 아니다.
 */
@RestController
@RequestMapping("/api/test-support")
public class TestSupportController {

    @PostMapping("/validation")
    public ApiResponseEnvelope<TestSupportRequest> validation(
            @Valid @RequestBody TestSupportRequest request
    ) {
        return ApiResponseEnvelope.success(request);
    }

    @GetMapping("/bad-request")
    public ApiResponseEnvelope<Void> badRequest() {
        throw new BadRequestException("잘못된 상태 전이입니다.");
    }

    @GetMapping("/not-found")
    public ApiResponseEnvelope<Void> notFound() {
        throw new ResourceNotFoundException();
    }

    @GetMapping("/conflict")
    public ApiResponseEnvelope<Void> conflict() {
        throw new ConflictException();
    }

    @GetMapping("/failure")
    public ApiResponseEnvelope<Void> failure() {
        throw new IllegalStateException("DB 연결 실패: jdbc:sqlite:./data/ilque.db\n\tat com.ilque.Fake.stack(Fake.java:1)");
    }
}
