import { post } from '../api/client';
import type {
  CompleteExecutionRequest,
  QuestExecutionResponse,
} from '../api/types';

export function completeExecution(
  questExecutionId: number,
  request: CompleteExecutionRequest,
): Promise<QuestExecutionResponse> {
  return post<QuestExecutionResponse>(
    `/executions/${questExecutionId}/complete`,
    request,
  );
}

export function skipExecution(
  questExecutionId: number,
): Promise<QuestExecutionResponse> {
  return post<QuestExecutionResponse>(
    `/executions/${questExecutionId}/skip`,
  );
}
