import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { completeQuickTodo, quickTodoQueryKey } from '../services/quickTodoService';

interface QuickTodoMutationState {
  isPending: (quickTodoId: number) => boolean;
  getError: (quickTodoId: number) => string | null;
  complete: (quickTodoId: number, date: string) => Promise<void>;
}

export function useQuickTodoMutation(): QuickTodoMutationState {
  const queryClient = useQueryClient();
  const [pendingIds, setPendingIds] = useState<Set<number>>(() => new Set());
  const [errors, setErrors] = useState<Map<number, string>>(() => new Map());

  const complete = useCallback(async (quickTodoId: number, date: string) => {
    if (pendingIds.has(quickTodoId)) return;
    setPendingIds((current) => new Set(current).add(quickTodoId));
    setErrors((current) => {
      const next = new Map(current);
      next.delete(quickTodoId);
      return next;
    });
    try {
      await completeQuickTodo(quickTodoId);
      await queryClient.invalidateQueries({ queryKey: quickTodoQueryKey(date) });
    } catch (error) {
      setErrors((current) => new Map(current).set(
        quickTodoId,
        error instanceof Error ? error.message : 'Quick Todo를 완료하지 못했습니다.',
      ));
      throw error;
    } finally {
      setPendingIds((current) => {
        const next = new Set(current);
        next.delete(quickTodoId);
        return next;
      });
    }
  }, [pendingIds, queryClient]);

  return {
    isPending: (quickTodoId) => pendingIds.has(quickTodoId),
    getError: (quickTodoId) => errors.get(quickTodoId) ?? null,
    complete,
  };
}
