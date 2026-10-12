import type { ApiError, FieldViolation } from './types';

/**
 * 공통 `fetch` 기반 API Client.
 *
 * 처리 규칙(`ilque-fe/agent.md` §7.2):
 * - 성공 응답(`success === true`)의 `data`를 반환한다. `data`가 없을 수 있으므로
 *   `null`인 경우를 호출자가 처리한다.
 * - 오류 응답(`success === false`)은 `error` 기준으로 `ApiHttpError`를 던진다.
 *   `VALIDATION_ERROR`의 `details`는 폼의 필드별 오류 표시에 사용한다.
 * - HTTP 상태와 응답 본문을 함께 처리한다. 본문이 없거나 JSON이 아니어도
 *   불필요하게 중단되지 않고 `ApiHttpError`로 변환한다.
 * - 서버에 연결할 수 없는 경우는 `ApiNetworkError`로 구분한다.
 * - 인증·재시도·인터셉터는 MVP 범위가 아니므로 포함하지 않는다.
 */

export const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';

/** 서버가 Envelope 대신 빈 본문·비 JSON을 반환할 때 사용하는 클라이언트 측 대체 코드. */
export const UNKNOWN_ERROR_CODE = 'UNKNOWN_ERROR';

function resolveBaseUrl(): string {
  const fromEnv = (
    import.meta as unknown as { env?: Record<string, string | undefined> }
  ).env?.VITE_API_BASE_URL;
  return (fromEnv ?? DEFAULT_API_BASE_URL).replace(/\/+$/, '');
}

let baseUrl = resolveBaseUrl();

/** 현재 API Base URL을 반환한다. */
export function getApiBaseUrl(): string {
  return baseUrl;
}

/** API Base URL을 변경한다. (기본값: `VITE_API_BASE_URL` 또는 로컬 백엔드) */
export function setApiBaseUrl(url: string): void {
  baseUrl = url.replace(/\/+$/, '');
}

/**
 * Base URL과 요청 경로를 결합한다. 요청 경로의 선행 슬래시 유무·개수와
 * 관계없이 동일한 URL을 만들며, Base URL의 `/api` 같은 경로 접두사를 보존한다.
 */
function buildUrl(path: string): string {
  const normalized = path.replace(/^\/+/, '');
  return normalized === '' ? baseUrl : `${baseUrl}/${normalized}`;
}

/** HTTP 오류 응답. 서버 Envelope 오류와 비 Envelope 오류를 모두 담는다. */
export class ApiHttpError extends Error {
  readonly name = 'ApiHttpError';
  readonly status: number;
  readonly code: string;
  readonly details: FieldViolation[] | null;

  constructor(status: number, error: ApiError) {
    super(error.message);
    this.status = status;
    this.code = error.code;
    this.details = error.details;
  }
}

/** 네트워크 오류. 서버가 응답하지 않는 경우에 사용한다. */
export class ApiNetworkError extends Error {
  readonly name = 'ApiNetworkError';

  constructor(message = '서버에 연결할 수 없습니다.', cause?: unknown) {
    super(message);
    this.cause = cause;
  }
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
  headers?: Record<string, string>;
  signal?: AbortSignal;
}

function asEnvelope(value: unknown): {
  success: boolean;
  data: unknown;
  error: ApiError | null;
} | null {
  if (typeof value !== 'object' || value === null) {
    return null;
  }
  const record = value as Record<string, unknown>;
  if (typeof record['success'] !== 'boolean' || typeof record['timestamp'] !== 'string') {
    return null;
  }
  if (!('data' in record) || !('error' in record)) {
    return null;
  }
  return record as unknown as {
    success: boolean;
    data: unknown;
    error: ApiError | null;
  };
}

type ParsedBody = { kind: 'empty' } | { kind: 'json'; value: unknown } | { kind: 'invalid' };

async function parseBody(response: Response): Promise<ParsedBody> {
  const text = await response.text();
  if (text === '') {
    return { kind: 'empty' };
  }
  try {
    return { kind: 'json', value: JSON.parse(text) as unknown };
  } catch {
    return { kind: 'invalid' };
  }
}

function invalidResponseError(): ApiError {
  return {
    code: UNKNOWN_ERROR_CODE,
    message: '서버 응답 형식이 올바르지 않습니다.',
    details: null,
  };
}

function unknownError(status: number): ApiError {
  return {
    code: UNKNOWN_ERROR_CODE,
    message: `요청에 실패했습니다. (HTTP ${status})`,
    details: null,
  };
}

/**
 * 공통 요청 함수. 성공 시 `data`를 반환하고, 실패 시 `ApiHttpError` 또는
 * `ApiNetworkError`를 던진다. `path`의 선행 슬래시 유무와 관계없이
 * Base URL과 동일하게 결합된다. (예: `/quests`, `quests`)
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, headers = {}, signal } = options;
  const mergedHeaders: Record<string, string> = {
    Accept: 'application/json',
    ...headers,
  };
  let rawBody: string | undefined;
  if (body !== undefined) {
    rawBody = JSON.stringify(body);
    mergedHeaders['Content-Type'] ??= 'application/json';
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(path), {
      method,
      headers: mergedHeaders,
      body: rawBody,
      signal,
    });
  } catch (error) {
    throw new ApiNetworkError(undefined, error);
  }

  const parsed = await parseBody(response);
  const envelope = parsed.kind === 'json' ? asEnvelope(parsed.value) : null;

  if (response.ok) {
    if (envelope !== null) {
      if (!envelope.success && envelope.error !== null) {
        throw new ApiHttpError(response.status, envelope.error);
      }
      return envelope.data as T;
    }
    if (parsed.kind === 'empty') {
      return null as T;
    }
    throw new ApiHttpError(response.status, invalidResponseError());
  }

  if (envelope !== null && !envelope.success && envelope.error !== null) {
    throw new ApiHttpError(response.status, envelope.error);
  }
  throw new ApiHttpError(response.status, unknownError(response.status));
}

export interface BodylessRequestOptions {
  headers?: Record<string, string>;
  signal?: AbortSignal;
}

export interface BodyRequestOptions extends BodylessRequestOptions {
  method?: 'POST' | 'PUT' | 'PATCH' | 'DELETE';
}

/** GET 요청. */
export function get<T>(path: string, options: BodylessRequestOptions = {}): Promise<T> {
  return request<T>(path, options);
}

/** POST 요청. */
export function post<T>(
  path: string,
  body?: unknown,
  options: BodyRequestOptions = {},
): Promise<T> {
  return request<T>(path, { ...options, method: options.method ?? 'POST', body });
}

/** PUT 요청. */
export function put<T>(
  path: string,
  body?: unknown,
  options: BodyRequestOptions = {},
): Promise<T> {
  return request<T>(path, { ...options, method: options.method ?? 'PUT', body });
}

/** PATCH 요청. */
export function patch<T>(
  path: string,
  body?: unknown,
  options: BodyRequestOptions = {},
): Promise<T> {
  return request<T>(path, { ...options, method: options.method ?? 'PATCH', body });
}

/** DELETE 요청. */
export function del<T>(
  path: string,
  body?: unknown,
  options: BodyRequestOptions = {},
): Promise<T> {
  return request<T>(path, { ...options, method: options.method ?? 'DELETE', body });
}
