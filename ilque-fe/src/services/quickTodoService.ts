import { get, patch } from '../api/client';
import type { QuickTodoResponse } from '../api/types';

export const quickTodoQueryKey = (date: string) => ['quick-todos', date] as const;

export function fetchQuickTodos(date: string, signal?: AbortSignal): Promise<QuickTodoResponse[]> {
  return get<QuickTodoResponse[]>(`/quick-todos?date=${encodeURIComponent(date)}`, { signal });
}

export function completeQuickTodo(quickTodoId: number): Promise<QuickTodoResponse> {
  return patch<QuickTodoResponse>(`/quick-todos/${quickTodoId}/complete`);
}
