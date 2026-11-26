import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { CalendarEventResponse } from '../api/types';
import {
  calendarEventsQueryKey,
  createCalendarEvent,
  type CalendarEventRequest,
  updateCalendarEvent,
} from '../services/calendarService';

export function useCalendarEventMutation() {
  const queryClient = useQueryClient();
  const invalidateCalendarQueries = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: calendarEventsQueryKey }),
      queryClient.invalidateQueries({ queryKey: ['calendar', 'upcoming'] }),
    ]);
  };

  const create = useMutation<CalendarEventResponse, Error, CalendarEventRequest>({
    mutationFn: createCalendarEvent,
    onSuccess: invalidateCalendarQueries,
  });
  const update = useMutation<CalendarEventResponse, Error, { id: number; request: CalendarEventRequest }>({
    mutationFn: ({ id, request }) => updateCalendarEvent(id, request),
    onSuccess: invalidateCalendarQueries,
  });

  return { create, update };
}
