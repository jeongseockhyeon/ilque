import { get } from '../api/client';
import type { DashboardResponse } from '../api/types';

export const dashboardQueryKey = ['dashboard'] as const;

export function fetchDashboard(signal?: AbortSignal): Promise<DashboardResponse> {
  return get<DashboardResponse>('/dashboard', { signal });
}
