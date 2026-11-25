import { useQuery } from '@tanstack/react-query';
import { dashboardQueryKey, fetchDashboard } from '../services/dashboardService';

export function useDashboardQuery() {
  return useQuery({
    queryKey: dashboardQueryKey,
    queryFn: ({ signal }) => fetchDashboard(signal),
  });
}
