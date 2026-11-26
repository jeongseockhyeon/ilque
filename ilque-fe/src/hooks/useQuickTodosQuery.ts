import { useQuery } from '@tanstack/react-query';
import { fetchQuickTodos, quickTodoQueryKey } from '../services/quickTodoService';

export function useQuickTodosQuery(date: string) {
  return useQuery({
    queryKey: quickTodoQueryKey(date),
    queryFn: ({ signal }) => fetchQuickTodos(date, signal),
  });
}
