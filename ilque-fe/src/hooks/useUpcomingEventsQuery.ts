import { useQuery } from '@tanstack/react-query';
import { fetchUpcomingEvents, upcomingEventsQueryKey } from '../services/calendarService';

export function useUpcomingEventsQuery(from: string, to: string) {
  return useQuery({
    queryKey: upcomingEventsQueryKey(from, to),
    queryFn: ({ signal }) => fetchUpcomingEvents(from, to, signal),
  });
}
