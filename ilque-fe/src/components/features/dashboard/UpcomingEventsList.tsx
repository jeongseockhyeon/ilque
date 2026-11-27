import { useState } from 'react';
import { CalendarDays } from 'lucide-react';
import type { CalendarEventResponse, CalendarEventType } from '../../../api/types';
import { useUpcomingEventsQuery } from '../../../hooks/useUpcomingEventsQuery';
import { EmptyState, ErrorState, LoadingState } from '../../common/DataState';

import './UpcomingEventsList.css';

const timeZone = 'Asia/Seoul';
const eventTypeLabels: Record<CalendarEventType, string> = {
  GENERAL: '일반 일정',
  GAME_EVENT: '게임 이벤트',
  GAME_PICKUP: '게임 픽업',
  GAME_UPDATE: '게임 업데이트',
};
const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone,
  month: 'long',
  day: 'numeric',
  weekday: 'short',
});
const timeFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone,
  hour: '2-digit',
  minute: '2-digit',
});
const dateKeyFormatter = new Intl.DateTimeFormat('en-CA', {
  timeZone,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});

function seoulDateString(date: Date): string {
  return dateKeyFormatter.format(date);
}

function addDays(date: string, days: number): string {
  const [year, month, day] = date.split('-').map(Number);
  return new Date(Date.UTC(year, month - 1, day + days)).toISOString().slice(0, 10);
}

function formatEventTime(event: CalendarEventResponse): string {
  if (event.allDay) return '종일';
  const start = timeFormatter.format(new Date(event.startAt));
  return event.endAt === null ? start : `${start} – ${timeFormatter.format(new Date(event.endAt))}`;
}

function groupEvents(events: CalendarEventResponse[]) {
  const groups = new Map<string, CalendarEventResponse[]>();
  for (const event of events) {
    const key = dateKeyFormatter.format(new Date(event.startAt));
    groups.set(key, [...(groups.get(key) ?? []), event]);
  }
  return [...groups.entries()];
}

function EventItem({ event }: { event: CalendarEventResponse }) {
  const [expanded, setExpanded] = useState(false);
  return (
    <li className="upcoming-events__item">
      <button
        className="upcoming-events__event"
        type="button"
        aria-expanded={expanded}
        onClick={() => setExpanded((value) => !value)}
      >
        <span className="upcoming-events__event-main">
          <strong>{event.title}</strong>
          <span>{eventTypeLabels[event.eventType]}</span>
        </span>
        <span className="upcoming-events__event-time">{formatEventTime(event)}</span>
      </button>
      {expanded && event.description && <p className="upcoming-events__description">{event.description}</p>}
    </li>
  );
}

export function UpcomingEventsList() {
  const [from] = useState(() => seoulDateString(new Date()));
  const [to] = useState(() => addDays(seoulDateString(new Date()), 7));
  const { data, error, isError, isLoading, isFetching, refetch } = useUpcomingEventsQuery(from, to);
  const events = data ?? [];
  const groups = groupEvents(events);

  return (
    <section className="upcoming-events" aria-labelledby="upcoming-events-title" aria-busy={isLoading}>
      <header className="upcoming-events__header">
        <div>
          <span className="page-placeholder__eyebrow">UPCOMING EVENTS</span>
          <h2 id="upcoming-events-title">예정된 일정</h2>
          <p>오늘부터 7일 이내의 이벤트입니다.</p>
        </div>
        <span className="dashboard-placeholder__badge">{events.length}개</span>
      </header>

      {isLoading ? (
        <LoadingState className="upcoming-events__state" message="예정된 일정을 불러오는 중입니다." />
      ) : isError && data === undefined ? (
        <ErrorState className="upcoming-events__state" message={error instanceof Error ? error.message : '예정된 일정을 불러오지 못했습니다.'} onRetry={() => void refetch()} retrying={isFetching} />
      ) : (
        <>
        {isError && <ErrorState className="upcoming-events__inline-state" compact message={error instanceof Error ? error.message : '예정된 일정을 갱신하지 못했습니다.'} onRetry={() => void refetch()} retrying={isFetching} />}
        {isFetching && <LoadingState className="upcoming-events__inline-state" compact message="일정 갱신 중" />}
        {events.length === 0 ? <EmptyState className="upcoming-events__state" icon={<CalendarDays aria-hidden="true" size={24} />} message="앞으로 7일 동안 예정된 일정이 없습니다." /> : (
        <div className="upcoming-events__groups">
          {groups.map(([date, groupedEvents]) => (
            <section className="upcoming-events__day" key={date} aria-label={dateFormatter.format(new Date(`${date}T12:00:00+09:00`))}>
              <h3>{dateFormatter.format(new Date(`${date}T12:00:00+09:00`))}</h3>
              <ul className="upcoming-events__items">
                {groupedEvents.map((event) => <EventItem key={event.calendarEventId} event={event} />)}
              </ul>
            </section>
          ))}
        </div>
        )}
        </>
      )}
    </section>
  );
}
