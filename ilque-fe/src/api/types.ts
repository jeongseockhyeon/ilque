/**
 * 공통 API 응답 타입.
 *
 * 백엔드의 공통 Response Envelope와 동일한 JSON 계약을 사용한다.
 * `ApiError`는 백엔드의 `ErrorDetail`에 대응하는 프론트엔드 측 타입 이름이다.
 */
export interface FieldViolation {
  field: string;
  message: string;
}

export interface ApiError {
  code: string;
  message: string;
  details: FieldViolation[] | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  timestamp: string;
}

export type QuestType = 'CHECK' | 'TIME_BASED';

export type QuestExecutionStatus =
  | 'PENDING'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'MISSED'
  | 'SKIPPED'
  | 'REST_DAY';

export interface QuestExecutionResponse {
  questExecutionId: number;
  questId: number | null;
  originalQuestId: number;
  questTitleSnapshot: string;
  questTypeSnapshot: QuestType;
  periodStart: string;
  periodEnd: string | null;
  status: QuestExecutionStatus;
  startedAt: string | null;
  completedAt: string | null;
  targetCount: number | null;
  actualCount: number | null;
  targetDuration: number | null;
  actualDuration: number | null;
  baseExp: number;
  expPerMinute: number;
  completionBonus: number;
  earnedExp: number | null;
}

export interface CompleteExecutionRequest {
  actualCount?: number;
  actualDuration?: number;
  partialCompletion?: boolean;
}

export interface TodayResponse {
  executions: QuestExecutionResponse[];
}

export interface DashboardResponse {
  totalExp: number;
  level: number;
  currentLevelExp: number;
  nextLevelRequiredExp: number;
  currentStreak: number;
  longestStreak: number;
  nextResetAt: string | null;
}
