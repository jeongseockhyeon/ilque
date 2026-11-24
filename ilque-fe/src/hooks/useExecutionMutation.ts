import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { CompleteExecutionRequest, QuestExecutionResponse } from '../api/types';
import {
  completeExecution,
  skipExecution,
} from '../services/executionService';
import { todayQueryKey } from '../services/todayService';

type ExecutionAction = 'complete' | 'skip';

interface ExecutionMutationState {
  isPending: (questExecutionId: number) => boolean;
  getError: (questExecutionId: number) => string | null;
  clearError: (questExecutionId: number) => void;
  complete: (
    questExecutionId: number,
    request: CompleteExecutionRequest,
  ) => Promise<QuestExecutionResponse>;
  skip: (questExecutionId: number) => Promise<QuestExecutionResponse>;
}

function actionError(error: unknown): string {
  return error instanceof Error
    ? error.message
    : '요청을 처리하지 못했습니다.';
}

export function useExecutionMutation(): ExecutionMutationState {
  const queryClient = useQueryClient();
  const [pendingExecutionIds, setPendingExecutionIds] = useState<Set<number>>(
    () => new Set(),
  );
  const [errors, setErrors] = useState<Map<number, string>>(() => new Map());

  const run = useCallback(
    async (
      questExecutionId: number,
      action: ExecutionAction,
      request?: CompleteExecutionRequest,
    ): Promise<QuestExecutionResponse> => {
      if (pendingExecutionIds.has(questExecutionId)) {
        throw new Error('이미 요청을 처리하고 있습니다.');
      }

      setPendingExecutionIds((current) => {
        const next = new Set(current);
        next.add(questExecutionId);
        return next;
      });
      setErrors((current) => {
        const next = new Map(current);
        next.delete(questExecutionId);
        return next;
      });

      try {
        const response = action === 'complete'
          ? await completeExecution(questExecutionId, request ?? {})
          : await skipExecution(questExecutionId);
        await queryClient.invalidateQueries({ queryKey: todayQueryKey });
        return response;
      } catch (error) {
        setErrors((current) => {
          const next = new Map(current);
          next.set(questExecutionId, actionError(error));
          return next;
        });
        throw error;
      } finally {
        setPendingExecutionIds((current) => {
          const next = new Set(current);
          next.delete(questExecutionId);
          return next;
        });
      }
    },
    [pendingExecutionIds, queryClient],
  );

  const isPending = useCallback(
    (questExecutionId: number) => pendingExecutionIds.has(questExecutionId),
    [pendingExecutionIds],
  );

  const getError = useCallback(
    (questExecutionId: number) => errors.get(questExecutionId) ?? null,
    [errors],
  );

  const clearError = useCallback((questExecutionId: number) => {
    setErrors((current) => {
      if (!current.has(questExecutionId)) return current;
      const next = new Map(current);
      next.delete(questExecutionId);
      return next;
    });
  }, []);

  const complete = useCallback(
    (questExecutionId: number, request: CompleteExecutionRequest) =>
      run(questExecutionId, 'complete', request),
    [run],
  );

  const skip = useCallback(
    (questExecutionId: number) => run(questExecutionId, 'skip'),
    [run],
  );

  return { isPending, getError, clearError, complete, skip };
}
