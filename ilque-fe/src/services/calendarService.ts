import { get, patch, post } from '../api/client';
import type { CalendarEventResponse, CalendarEventType } from '../api/types';

export interface CalendarEventRequest {
  title: string;
  description: string | null;
  eventType: CalendarEventType;
  startAt: string;
  endAt: string | null;
  allDay: boolean;
  reminderAt: string | null;
  categoryId: number | null;
}

export const calendarEventsQueryKey = ['calendar', 'events'] as const;

export const upcomingEventsQueryKey = (from: string, to: string) => ['calendar', 'upcoming', from, to] as const;

export function fetchUpcomingEvents(from: string, to: string, signal?: AbortSignal): Promise<CalendarEventResponse[]> {
  const query = new URLSearchParams({ from, to });
  return get<CalendarEventResponse[]>(`/calendar/upcoming?${query.toString()}`, { signal });
}

export function fetchCalendarEvents(from: string, to: string, signal?: AbortSignal): Promise<CalendarEventResponse[]> {
  const query = new URLSearchParams({ from, to });
  return get<CalendarEventResponse[]>(`/calendar/events?${query.toString()}`, { signal });
}

export function createCalendarEvent(request: CalendarEventRequest): Promise<CalendarEventResponse> {
  return post<CalendarEventResponse>('/calendar/events', request);
}

export function updateCalendarEvent(id: number, request: CalendarEventRequest): Promise<CalendarEventResponse> {
  return patch<CalendarEventResponse>(`/calendar/events/${id}`, request);
}
