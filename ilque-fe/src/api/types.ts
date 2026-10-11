/**
 * 공통 API 응답 타입.
 *
 * 백엔드의 공통 Response Envelope와 동일한 JSON 계약을 사용한다.
 * `ApiError`는 백엔드의 `ErrorDetail`에 대응하는 프론트엔드 측 타입 이름이다.
 */
export interface FieldViolation {
  field: string;
  message: string;
}

export interface ApiError {
  code: string;
  message: string;
  details: FieldViolation[] | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  timestamp: string;
}
