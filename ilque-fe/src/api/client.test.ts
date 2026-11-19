import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  ApiHttpError,
  ApiNetworkError,
  DEFAULT_API_BASE_URL,
  UNKNOWN_ERROR_CODE,
  get,
  setApiBaseUrl,
} from './client';

const TIMESTAMP = '2026-10-06T14:00:00Z';

function successEnvelope<T>(data: T): unknown {
  return { success: true, data, error: null, timestamp: TIMESTAMP };
}

function errorEnvelope(code: string, message: string, details: unknown): unknown {
  return {
    success: false,
    data: null,
    error: { code, message, details },
    timestamp: TIMESTAMP,
  };
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

function stubFetch(response: Response | (() => Promise<Response> | Response)): ReturnType<typeof vi.fn> {
  const fetchMock = vi.fn(
    typeof response === 'function' ? response : async () => response,
  );
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function lastRequestUrl(fetchMock: ReturnType<typeof vi.fn>): string {
  const [url] = fetchMock.mock.calls[0] as [unknown];
  return String(url);
}

async function awaitHttpError(path: string): Promise<ApiHttpError> {
  try {
    await get(path);
  } catch (error) {
    expect(error).toBeInstanceOf(ApiHttpError);
    if (error instanceof ApiHttpError) {
      return error;
    }
  }
  throw new Error('ApiHttpError가 발생하지 않았습니다.');
}

afterEach(() => {
  vi.unstubAllGlobals();
  setApiBaseUrl(DEFAULT_API_BASE_URL);
});

describe('성공 응답', () => {
  it('성공 Envelope의 data를 반환한다', async () => {
    stubFetch(jsonResponse(successEnvelope({ status: 'UP' })));

    await expect(get<{ status: string }>('/health')).resolves.toEqual({ status: 'UP' });
  });

  it('data가 null인 성공 응답은 null을 반환한다', async () => {
    stubFetch(jsonResponse(successEnvelope(null)));

    await expect(get<null>('/empty')).resolves.toBeNull();
  });

  it('본문이 비어 있는 200 응답은 null을 반환한다', async () => {
    stubFetch(new Response('', { status: 200 }));

    await expect(get<null>('/empty')).resolves.toBeNull();
  });
});

describe('오류 응답', () => {
  it('400 검증 오류의 코드·메시지·details를 전달한다', async () => {
    stubFetch(
      jsonResponse(
        errorEnvelope('VALIDATION_ERROR', '입력 값이 유효하지 않습니다.', [
          { field: 'title', message: '제목은 필수입니다.' },
        ]),
        400,
      ),
    );

    const error = await awaitHttpError('/quests');

    expect(error.status).toBe(400);
    expect(error.code).toBe('VALIDATION_ERROR');
    expect(error.message).toBe('입력 값이 유효하지 않습니다.');
    expect(error.details?.[0]).toEqual({ field: 'title', message: '제목은 필수입니다.' });
  });

  it('404 리소스 없음 응답을 ApiHttpError로 처리한다', async () => {
    stubFetch(jsonResponse(errorEnvelope('RESOURCE_NOT_FOUND', '해당 리소스를 찾을 수 없습니다.', null), 404));

    const error = await awaitHttpError('/quests/999');

    expect(error.status).toBe(404);
    expect(error.code).toBe('RESOURCE_NOT_FOUND');
  });

  it('일반 HTTP 오류(response.ok === false)를 ApiHttpError로 처리한다', async () => {
    stubFetch(new Response('Forbidden', { status: 403 }));

    const error = await awaitHttpError('/quests');

    expect(error.status).toBe(403);
    expect(error.code).toBe(UNKNOWN_ERROR_CODE);
  });

  it('500 비-JSON 응답도 파싱 예외 없이 ApiHttpError로 처리한다', async () => {
    stubFetch(
      new Response('<html>Internal Server Error</html>', {
        status: 500,
        headers: { 'Content-Type': 'text/html' },
      }),
    );

    const error = await awaitHttpError('/quests');

    expect(error.status).toBe(500);
    expect(error.code).toBe(UNKNOWN_ERROR_CODE);
    expect(error.details).toBeNull();
  });

  it('잘못된 JSON 본문은 ApiHttpError로 처리한다', async () => {
    stubFetch(new Response('{invalid-json', { status: 200 }));

    const error = await awaitHttpError('/quests');

    expect(error.code).toBe(UNKNOWN_ERROR_CODE);
  });

  it('본문이 비어 있는 오류 응답을 ApiHttpError로 처리한다', async () => {
    stubFetch(new Response('', { status: 503 }));

    const error = await awaitHttpError('/quests');

    expect(error.status).toBe(503);
    expect(error.code).toBe(UNKNOWN_ERROR_CODE);
  });

  it('fetch 실패는 ApiNetworkError로 구분한다', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => {
        throw new TypeError('fetch failed');
      }),
    );

    const error = await get('/health').then(
      () => {
        throw new Error('ApiNetworkError가 발생하지 않았습니다.');
      },
      (caught: unknown) => caught,
    );

    expect(error).toBeInstanceOf(ApiNetworkError);
    expect(error).not.toBeInstanceOf(ApiHttpError);
  });
});

describe('Base URL과 경로 결합', () => {
  it('setApiBaseUrl로 설정한 Base URL이 요청에 반영된다', async () => {
    setApiBaseUrl('http://example.com:9000/api/');
    const fetchMock = stubFetch(jsonResponse(successEnvelope({ status: 'UP' })));

    await get('/health');

    expect(lastRequestUrl(fetchMock)).toBe('http://example.com:9000/api/health');
  });

  it.each(['/quests', 'quests', '//quests'])('경로 %s를 동일한 URL로 결합한다', async (path) => {
    const fetchMock = stubFetch(jsonResponse(successEnvelope([])));

    await get(path);

    expect(lastRequestUrl(fetchMock)).toBe(`${DEFAULT_API_BASE_URL}/quests`);
  });

  it('끝에 슬래시가 있는 Base URL에서도 //가 생기지 않는다', async () => {
    setApiBaseUrl('http://localhost:8080/api/');
    const fetchMock = stubFetch(jsonResponse(successEnvelope({ status: 'UP' })));

    await get('/health');

    expect(lastRequestUrl(fetchMock)).toBe('http://localhost:8080/api/health');
  });

  it('빈 경로는 Base URL 그대로 요청한다', async () => {
    const fetchMock = stubFetch(jsonResponse(successEnvelope({ status: 'UP' })));

    await get('');

    expect(lastRequestUrl(fetchMock)).toBe(DEFAULT_API_BASE_URL);
  });
});
