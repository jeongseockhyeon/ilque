import { Check, CircleAlert, ClipboardCheck, Clock3, LoaderCircle, RotateCw, SkipForward, X } from 'lucide-react'
import { useState } from 'react'
import type { CompleteExecutionRequest, QuestExecutionResponse, QuestExecutionStatus, QuestType } from '../../../api/types'
import { useExecutionMutation } from '../../../hooks/useExecutionMutation'
import { useTodayQuery } from '../../../hooks/useTodayQuery'

import './TodayQuestList.css'

const statusLabels: Record<QuestExecutionStatus, string> = {
  PENDING: '대기 중',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
  MISSED: '놓침',
  SKIPPED: '건너뜀',
  REST_DAY: '휴식일',
}

const questTypeLabels: Record<QuestType, string> = {
  CHECK: '횟수형',
  TIME_BASED: '시간형',
}

const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
})

function formatDateTime(value: string | null): string | null {
  return value === null ? null : dateTimeFormatter.format(new Date(value))
}

function formatPeriod(execution: QuestExecutionResponse): string {
  const start = formatDateTime(execution.periodStart)
  const end = formatDateTime(execution.periodEnd)
  return end === null ? `${start}부터` : `${start} ~ ${end}`
}

function formatProgress(execution: QuestExecutionResponse): string | null {
  if (execution.questTypeSnapshot === 'CHECK') {
    if (execution.targetCount === null) return null
    return `${execution.actualCount ?? 0} / ${execution.targetCount}회`
  }

  if (execution.targetDuration === null) return null
  return `${execution.actualDuration ?? 0} / ${execution.targetDuration}분`
}

interface QuestExecutionCardProps {
  execution: QuestExecutionResponse
  isPending: boolean
  error: string | null
  onComplete: (request: CompleteExecutionRequest) => Promise<QuestExecutionResponse>
  onSkip: () => Promise<QuestExecutionResponse>
  onClearError: () => void
}

function QuestExecutionCard({
  execution,
  isPending,
  error,
  onComplete,
  onSkip,
  onClearError,
}: QuestExecutionCardProps) {
  const progress = formatProgress(execution)
  const [isCompleting, setIsCompleting] = useState(false)
  const [isConfirmingSkip, setIsConfirmingSkip] = useState(false)
  const [actualCount, setActualCount] = useState(String(execution.actualCount ?? ''))
  const [actualDuration, setActualDuration] = useState(String(execution.actualDuration ?? ''))
  const [partialCompletion, setPartialCompletion] = useState(false)
  const [inputError, setInputError] = useState<string | null>(null)

  const canAct = execution.status === 'PENDING' || execution.status === 'IN_PROGRESS'

  const startComplete = () => {
    onClearError()
    setInputError(null)
    setIsConfirmingSkip(false)
    setIsCompleting(true)
  }

  const cancelComplete = () => {
    if (isPending) return
    setInputError(null)
    setIsCompleting(false)
  }

  const submitComplete = async () => {
    const value = execution.questTypeSnapshot === 'CHECK' ? actualCount : actualDuration
    const parsedValue = Number(value)

    if (value.trim() === '' || !Number.isFinite(parsedValue) || parsedValue < 0) {
      setInputError('0 이상의 값을 입력해 주세요.')
      return
    }

    if (execution.questTypeSnapshot === 'CHECK' && !Number.isInteger(parsedValue)) {
      setInputError('수행 횟수는 정수로 입력해 주세요.')
      return
    }

    const request: CompleteExecutionRequest = execution.questTypeSnapshot === 'CHECK'
      ? { actualCount: parsedValue }
      : { actualDuration: parsedValue, partialCompletion }

    setInputError(null)
    try {
      await onComplete(request)
      setIsCompleting(false)
    } catch {
      // 오류 문구는 부모 훅에서 전달된 서버 오류를 사용합니다.
    }
  }

  const confirmSkip = async () => {
    try {
      await onSkip()
      setIsConfirmingSkip(false)
    } catch {
      // 오류 문구는 부모 훅에서 전달된 서버 오류를 사용합니다.
    }
  }

  return (
    <li className="today-quest-list__item">
      <article className="today-quest-card">
        <div className="today-quest-card__icon" aria-hidden="true">
          {execution.questTypeSnapshot === 'CHECK' ? <ClipboardCheck size={20} /> : <Clock3 size={20} />}
        </div>
        <div className="today-quest-card__content">
          <div className="today-quest-card__heading">
            <h3>{execution.questTitleSnapshot}</h3>
            <span className={`today-quest-card__status today-quest-card__status--${execution.status.toLowerCase()}`}>
              {statusLabels[execution.status]}
            </span>
          </div>
          <div className="today-quest-card__meta">
            <span>{questTypeLabels[execution.questTypeSnapshot]}</span>
            <span>{formatPeriod(execution)}</span>
            {progress !== null && <span>{progress}</span>}
          </div>
          {canAct && (
            <div className="today-quest-card__actions">
              {!isCompleting && !isConfirmingSkip && (
                <>
                  <button
                    className="today-quest-card__action today-quest-card__action--complete"
                    type="button"
                    onClick={startComplete}
                    disabled={isPending}
                  >
                    <Check aria-hidden="true" size={16} />
                    완료
                  </button>
                  <button
                    className="today-quest-card__action today-quest-card__action--skip"
                    type="button"
                    onClick={() => {
                      onClearError()
                      setIsCompleting(false)
                      setIsConfirmingSkip(true)
                    }}
                    disabled={isPending}
                  >
                    <SkipForward aria-hidden="true" size={16} />
                    건너뛰기
                  </button>
                </>
              )}

              {isCompleting && (
                <div className="today-quest-card__form">
                  <label htmlFor={`execution-${execution.questExecutionId}-value`}>
                    {execution.questTypeSnapshot === 'CHECK' ? '실제 수행 횟수' : '실제 수행 시간(분)'}
                  </label>
                  <input
                    id={`execution-${execution.questExecutionId}-value`}
                    type="number"
                    min="0"
                    step={execution.questTypeSnapshot === 'CHECK' ? '1' : '1'}
                    value={execution.questTypeSnapshot === 'CHECK' ? actualCount : actualDuration}
                    onChange={(event) => {
                      if (execution.questTypeSnapshot === 'CHECK') {
                        setActualCount(event.target.value)
                      } else {
                        setActualDuration(event.target.value)
                      }
                      setInputError(null)
                    }}
                    disabled={isPending}
                  />
                  {execution.questTypeSnapshot === 'TIME_BASED' && (
                    <label className="today-quest-card__checkbox">
                      <input
                        type="checkbox"
                        checked={partialCompletion}
                        onChange={(event) => setPartialCompletion(event.target.checked)}
                        disabled={isPending}
                      />
                      부분 완료로 처리
                    </label>
                  )}
                  {inputError !== null && <p className="today-quest-card__inline-error">{inputError}</p>}
                  <div className="today-quest-card__form-actions">
                    <button
                      className="today-quest-card__action today-quest-card__action--complete"
                      type="button"
                      onClick={() => void submitComplete()}
                      disabled={isPending}
                    >
                      {isPending ? <LoaderCircle className="today-quest-card__spinner" aria-hidden="true" size={16} /> : <Check aria-hidden="true" size={16} />}
                      {isPending ? '처리 중' : '완료 요청'}
                    </button>
                    <button
                      className="today-quest-card__action today-quest-card__action--cancel"
                      type="button"
                      onClick={cancelComplete}
                      disabled={isPending}
                    >
                      <X aria-hidden="true" size={16} />
                      취소
                    </button>
                  </div>
                </div>
              )}

              {isConfirmingSkip && (
                <div className="today-quest-card__confirmation" role="group" aria-label="건너뛰기 확인">
                  <p>이 퀘스트를 건너뛰시겠습니까?</p>
                  <div className="today-quest-card__form-actions">
                    <button
                      className="today-quest-card__action today-quest-card__action--skip"
                      type="button"
                      onClick={() => void confirmSkip()}
                      disabled={isPending}
                    >
                      {isPending ? <LoaderCircle className="today-quest-card__spinner" aria-hidden="true" size={16} /> : <SkipForward aria-hidden="true" size={16} />}
                      {isPending ? '처리 중' : '건너뛰기'}
                    </button>
                    <button
                      className="today-quest-card__action today-quest-card__action--cancel"
                      type="button"
                      onClick={() => setIsConfirmingSkip(false)}
                      disabled={isPending}
                    >
                      <X aria-hidden="true" size={16} />
                      취소
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
          {isPending && <p className="today-quest-card__pending" role="status">요청을 처리하고 있습니다.</p>}
          {error !== null && <p className="today-quest-card__error" role="alert">{error}</p>}
        </div>
      </article>
    </li>
  )
}

export function TodayQuestList() {
  const { data, error, isError, isLoading, refetch } = useTodayQuery()
  const { isPending, getError, clearError, complete, skip } = useExecutionMutation()

  if (isLoading) {
    return (
      <section className="today-quest-list" aria-labelledby="today-quest-list-title" aria-busy="true">
        <div className="today-quest-list__header">
          <div>
            <span className="page-placeholder__eyebrow">TODAY QUEST</span>
            <h2 id="today-quest-list-title">오늘의 Quest</h2>
          </div>
        </div>
        <div className="today-quest-list__state" role="status">
          <LoaderCircle className="today-quest-list__spinner" aria-hidden="true" size={24} />
          <p>오늘의 Quest를 불러오는 중입니다.</p>
        </div>
      </section>
    )
  }

  if (isError) {
    return (
      <section className="today-quest-list" aria-labelledby="today-quest-list-title">
        <div className="today-quest-list__header">
          <div>
            <span className="page-placeholder__eyebrow">TODAY QUEST</span>
            <h2 id="today-quest-list-title">오늘의 Quest</h2>
          </div>
        </div>
        <div className="today-quest-list__state today-quest-list__state--error" role="alert">
          <CircleAlert aria-hidden="true" size={24} />
          <p>{error instanceof Error ? error.message : 'Today 데이터를 불러오지 못했습니다.'}</p>
          <button type="button" onClick={() => refetch()}>
            <RotateCw aria-hidden="true" size={16} />
            다시 시도
          </button>
        </div>
      </section>
    )
  }

  const executions = data?.executions ?? []

  return (
    <section className="today-quest-list" aria-labelledby="today-quest-list-title">
      <div className="today-quest-list__header">
        <div>
          <span className="page-placeholder__eyebrow">TODAY QUEST</span>
          <h2 id="today-quest-list-title">오늘의 Quest</h2>
          <p>오늘 수행할 QuestExecution을 확인하세요.</p>
        </div>
        <span className="dashboard-placeholder__badge">{executions.length}개</span>
      </div>

      {executions.length === 0 ? (
        <div className="today-quest-list__state" role="status">
          <ClipboardCheck aria-hidden="true" size={24} />
          <p>오늘 수행할 Quest가 없습니다.</p>
        </div>
      ) : (
        <ul className="today-quest-list__items">
          {executions.map((execution) => (
            <QuestExecutionCard
              key={execution.questExecutionId}
              execution={execution}
              isPending={isPending(execution.questExecutionId)}
              error={getError(execution.questExecutionId)}
              onComplete={(request) => complete(execution.questExecutionId, request)}
              onSkip={() => skip(execution.questExecutionId)}
              onClearError={() => clearError(execution.questExecutionId)}
            />
          ))}
        </ul>
      )}
    </section>
  )
}
