import { get } from '../api/client';
import type { TodayResponse } from '../api/types';

export const todayQueryKey = ['today'] as const;

export function fetchToday(signal?: AbortSignal): Promise<TodayResponse> {
  return get<TodayResponse>('/today', { signal });
}
