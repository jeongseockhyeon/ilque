import { useState } from 'react';
import { CalendarDays, CircleAlert, LoaderCircle, RotateCw } from 'lucide-react';
import type { CalendarEventResponse, CalendarEventType } from '../../../api/types';
import { useUpcomingEventsQuery } from '../../../hooks/useUpcomingEventsQuery';

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

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
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
  const [to] = useState(() => seoulDateString(addDays(new Date(), 7)));
  const { data, error, isError, isLoading, refetch } = useUpcomingEventsQuery(from, to);
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
        <div className="upcoming-events__state" role="status">
          <LoaderCircle className="upcoming-events__spinner" aria-hidden="true" size={24} />
          <p>예정된 일정을 불러오는 중입니다.</p>
        </div>
      ) : isError ? (
        <div className="upcoming-events__state upcoming-events__state--error" role="alert">
          <CircleAlert aria-hidden="true" size={24} />
          <p>{error instanceof Error ? error.message : '예정된 일정을 불러오지 못했습니다.'}</p>
          <button type="button" onClick={() => void refetch()}><RotateCw aria-hidden="true" size={16} /> 다시 시도</button>
        </div>
      ) : events.length === 0 ? (
        <div className="upcoming-events__state" role="status">
          <CalendarDays aria-hidden="true" size={24} />
          <p>앞으로 7일 동안 예정된 일정이 없습니다.</p>
        </div>
      ) : (
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
    </section>
  );
}
