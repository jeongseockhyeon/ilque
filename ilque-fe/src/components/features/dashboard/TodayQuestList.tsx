import { CircleAlert, ClipboardCheck, Clock3, LoaderCircle, RotateCw } from 'lucide-react'
import type { QuestExecutionResponse, QuestExecutionStatus, QuestType } from '../../../api/types'
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

function QuestExecutionCard({ execution }: { execution: QuestExecutionResponse }) {
  const progress = formatProgress(execution)

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
        </div>
      </article>
    </li>
  )
}

export function TodayQuestList() {
  const { data, error, isError, isLoading, refetch } = useTodayQuery()

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
            <QuestExecutionCard key={execution.questExecutionId} execution={execution} />
          ))}
        </ul>
      )}
    </section>
  )
}
