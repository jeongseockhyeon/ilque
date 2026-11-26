import { useQuery } from '@tanstack/react-query';
import { fetchCalendarEvents, calendarEventsQueryKey } from '../services/calendarService';
import { fetchCategories, categoriesQueryKey } from '../services/categoryService';

export function useCalendarEventsQuery() {
  return useQuery({
    queryKey: calendarEventsQueryKey,
    queryFn: ({ signal }) => fetchCalendarEvents(signal),
  });
}

export function useCategoriesQuery() {
  return useQuery({
    queryKey: categoriesQueryKey,
    queryFn: ({ signal }) => fetchCategories(signal),
  });
}
