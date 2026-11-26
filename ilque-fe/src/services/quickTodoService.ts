import { get, patch, post } from '../api/client';
import type { QuickTodoResponse } from '../api/types';

export const quickTodoQueryKey = (date: string) => ['quick-todos', date] as const;

export function fetchQuickTodos(date: string, signal?: AbortSignal): Promise<QuickTodoResponse[]> {
  return get<QuickTodoResponse[]>(`/quick-todos?date=${encodeURIComponent(date)}`, { signal });
}

export function completeQuickTodo(quickTodoId: number): Promise<QuickTodoResponse> {
  return patch<QuickTodoResponse>(`/quick-todos/${quickTodoId}/complete`);
}

export function createQuickTodo(title: string): Promise<QuickTodoResponse> {
  return post<QuickTodoResponse>('/quick-todos', { title });
}

export function updateQuickTodoTitle(quickTodoId: number, title: string): Promise<QuickTodoResponse> {
  return patch<QuickTodoResponse>(`/quick-todos/${quickTodoId}`, { title });
}
