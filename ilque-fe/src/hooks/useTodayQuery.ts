import { useQuery } from '@tanstack/react-query';
import { fetchToday, todayQueryKey } from '../services/todayService';

export function useTodayQuery() {
  return useQuery({
    queryKey: todayQueryKey,
    queryFn: ({ signal }) => fetchToday(signal),
  });
}
