import { get } from '../api/client';
import type { CalendarEventResponse } from '../api/types';

export const upcomingEventsQueryKey = (from: string, to: string) => ['calendar', 'upcoming', from, to] as const;

export function fetchUpcomingEvents(from: string, to: string, signal?: AbortSignal): Promise<CalendarEventResponse[]> {
  const query = new URLSearchParams({ from, to });
  return get<CalendarEventResponse[]>(`/calendar/upcoming?${query.toString()}`, { signal });
}
