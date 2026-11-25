import { CircleAlert, Flame, Gauge, LoaderCircle, RotateCcw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useDashboardQuery } from '../../hooks/useDashboardQuery'

import './Header.css'

export function Header() {
  const { data, error, isError, isLoading, refetch } = useDashboardQuery()
  const [currentTime, setCurrentTime] = useState(() => Date.now())
  const expProgress = data === undefined || data.nextLevelRequiredExp === 0
    ? 0
    : Math.min(100, Math.max(0, (data.currentLevelExp / data.nextLevelRequiredExp) * 100))
  const remainingResetTime = data?.nextResetAt === null || data?.nextResetAt === undefined
    ? null
    : Math.max(0, new Date(data.nextResetAt).getTime() - currentTime)

  const nextResetLabel = data?.nextResetAt === null
    ? '초기화 대상 없음'
    : data?.nextResetAt === undefined
      ? '--'
      : formatDateTime(data.nextResetAt)

  useEffect(() => {
    if (data?.nextResetAt === null || data?.nextResetAt === undefined) return

    const timer = window.setInterval(() => setCurrentTime(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [data?.nextResetAt])

  useEffect(() => {
    if (data?.nextResetAt === null || data?.nextResetAt === undefined) return

    const resetAt = new Date(data.nextResetAt).getTime()
    const timer = window.setTimeout(() => void refetch(), Math.max(0, resetAt - Date.now()) + 1000)
    return () => window.clearTimeout(timer)
  }, [data?.nextResetAt, refetch])

  return (
    <header className="app-header">
      <div className="app-header__title-group">
        <span className="app-header__eyebrow">ADVENTURER STATUS</span>
        <h1>Today Dashboard</h1>
      </div>

      <div className="app-header__stats" aria-label="모험가 상태 요약" aria-busy={isLoading}>
        <div className="app-header__stat">
          <Gauge aria-hidden="true" size={18} />
          <span className="app-header__stat-label">Level</span>
          <strong>{data === undefined ? '--' : `Lv. ${data.level}`}</strong>
        </div>
        <div className="app-header__stat app-header__stat--exp">
          <span className="app-header__stat-icon app-header__stat-icon--accent" aria-hidden="true">
            ✦
          </span>
          <span className="app-header__stat-label">EXP</span>
          <strong>{data === undefined ? '-- / --' : `${data.currentLevelExp} / ${data.nextLevelRequiredExp}`}</strong>
          <span className="app-header__progress" aria-label={data === undefined ? 'EXP 진행률 확인 중' : `EXP 진행률 ${Math.round(expProgress)}퍼센트`}>
            <span className="app-header__progress-bar" style={{ width: `${expProgress}%` }} />
          </span>
        </div>
        <div className="app-header__stat">
          <Flame aria-hidden="true" size={18} />
          <span className="app-header__stat-label">Streak</span>
          <strong>{data === undefined ? '--' : `${data.currentStreak}일`}</strong>
        </div>
        <div className="app-header__stat">
          <RotateCcw aria-hidden="true" size={17} />
          <span className="app-header__stat-label">Next Reset</span>
          <strong>{nextResetLabel}</strong>
          {remainingResetTime !== null && (
            <span className="app-header__countdown" aria-label={`Next Reset까지 ${formatRemainingTime(remainingResetTime)}`}>
              {formatRemainingTime(remainingResetTime)} 후
            </span>
          )}
        </div>
      </div>

      {isLoading && (
        <div className="app-header__state" role="status">
          <LoaderCircle className="app-header__spinner" aria-hidden="true" size={15} />
          Dashboard 상태를 불러오는 중입니다.
        </div>
      )}
      {isError && (
        <div className="app-header__state app-header__state--error" role="alert">
          <CircleAlert aria-hidden="true" size={15} />
          <span>{error instanceof Error ? error.message : 'Dashboard 상태를 불러오지 못했습니다.'}</span>
          <button type="button" onClick={() => void refetch()}>다시 시도</button>
        </div>
      )}
    </header>
  )
}

const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
})

function formatDateTime(value: string): string {
  return dateTimeFormatter.format(new Date(value))
}

function formatRemainingTime(milliseconds: number): string {
  const totalSeconds = Math.floor(milliseconds / 1000)
  const days = Math.floor(totalSeconds / 86_400)
  const hours = Math.floor((totalSeconds % 86_400) / 3_600)
  const minutes = Math.floor((totalSeconds % 3_600) / 60)
  const seconds = totalSeconds % 60

  if (days > 0) return `${days}일 ${hours}시간`
  if (hours > 0) return `${hours}시간 ${minutes}분`
  if (minutes > 0) return `${minutes}분 ${seconds}초`
  return `${seconds}초`
}
