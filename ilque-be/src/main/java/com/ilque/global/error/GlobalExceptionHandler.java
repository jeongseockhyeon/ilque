package com.ilque.global.error;

import java.util.Comparator;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ilque.global.response.ApiResponseEnvelope;
import com.ilque.global.response.FieldViolation;

/**
 * 공통 Envelope 기준 전역 예외 처리기.
 *
 * <p>예상하지 못한 서버 오류의 Stack Trace, SQL, 내부 경로는 응답에 포함하지 않고
 * 일반 메시지로 변환하며, 상세 내용은 서버 로그에만 남긴다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleBusinessException(BusinessException ex) {
        log.warn("비즈니스 예외: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiResponseEnvelope.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        List<FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new FieldViolation(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();
        log.warn("요청 값 검증 실패: {}", details);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponseEnvelope.validationError(
                        ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                        details
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        List<FieldViolation> details = ex.getConstraintViolations().stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();
        log.warn("요청 값 검증 실패: {}", details);
        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponseEnvelope.validationError(
                        ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                        details
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        log.warn("요청 본문 읽기 실패: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponseEnvelope.error(ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("존재하지 않는 경로: {}", ex.getResourcePath());
        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResponseEnvelope.error(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEnvelope<?>> handleUnexpectedException(Exception ex) {
        log.error("예상하지 못한 서버 오류", ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponseEnvelope.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
