import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  completeQuickTodo,
  createQuickTodo,
  quickTodoQueryKey,
  updateQuickTodoTitle,
} from '../services/quickTodoService';

interface QuickTodoMutationState {
  isPending: (quickTodoId: number) => boolean;
  getError: (quickTodoId: number) => string | null;
  complete: (quickTodoId: number, date: string) => Promise<void>;
  create: (title: string, date: string) => Promise<void>;
  update: (quickTodoId: number, title: string, date: string) => Promise<void>;
  isCreating: boolean;
  createError: string | null;
}

export function useQuickTodoMutation(): QuickTodoMutationState {
  const queryClient = useQueryClient();
  const [pendingIds, setPendingIds] = useState<Set<number>>(() => new Set());
  const [errors, setErrors] = useState<Map<number, string>>(() => new Map());
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const create = useCallback(async (title: string, date: string) => {
    if (isCreating) return;
    setIsCreating(true);
    setCreateError(null);
    try {
      await createQuickTodo(title);
      await queryClient.invalidateQueries({ queryKey: quickTodoQueryKey(date) });
    } catch (error) {
      setCreateError(error instanceof Error ? error.message : 'Quick Todo를 추가하지 못했습니다.');
      throw error;
    } finally {
      setIsCreating(false);
    }
  }, [isCreating, queryClient]);

  const update = useCallback(async (quickTodoId: number, title: string, date: string) => {
    if (pendingIds.has(quickTodoId)) return;
    setPendingIds((current) => new Set(current).add(quickTodoId));
    setErrors((current) => {
      const next = new Map(current);
      next.delete(quickTodoId);
      return next;
    });
    try {
      await updateQuickTodoTitle(quickTodoId, title);
      await queryClient.invalidateQueries({ queryKey: quickTodoQueryKey(date) });
    } catch (error) {
      setErrors((current) => new Map(current).set(
        quickTodoId,
        error instanceof Error ? error.message : 'Quick Todo를 수정하지 못했습니다.',
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
    create,
    update,
    isCreating,
    createError,
  };
}
