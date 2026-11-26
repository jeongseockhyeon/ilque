import { useQuery } from '@tanstack/react-query';
import { fetchCalendarEvents, calendarEventsQueryKey } from '../services/calendarService';
import { fetchCategories, categoriesQueryKey } from '../services/categoryService';

export function useCalendarEventsQuery(from: string, to: string) {
  return useQuery({
    queryKey: [...calendarEventsQueryKey, from, to],
    queryFn: ({ signal }) => fetchCalendarEvents(from, to, signal),
  });
}

export function useCategoriesQuery() {
  return useQuery({
    queryKey: categoriesQueryKey,
    queryFn: ({ signal }) => fetchCategories(signal),
  });
}
