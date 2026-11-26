import { useState } from 'react';
import { CalendarDays, CircleAlert, LoaderCircle, Pencil, Plus, RotateCw } from 'lucide-react';
import type { CalendarEventResponse, CalendarEventType } from '../../../api/types';
import { useCalendarEventMutation } from '../../../hooks/useCalendarEventMutation';
import { useCalendarEventsQuery, useCategoriesQuery } from '../../../hooks/useCalendarManagementQuery';
import { CalendarEventFormModal } from './CalendarEventFormModal';
import type { CalendarEventRequest } from '../../../services/calendarService';

import './CalendarEventsPage.css';

const typeLabels: Record<CalendarEventType, string> = {
  GENERAL: '일반 일정',
  GAME_EVENT: '게임 이벤트',
  GAME_PICKUP: '게임 픽업',
  GAME_UPDATE: '게임 업데이트',
};
const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone: 'Asia/Seoul', year: 'numeric', month: 'long', day: 'numeric', weekday: 'short',
});
const timeFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone: 'Asia/Seoul', hour: '2-digit', minute: '2-digit',
});

function CalendarEventRow({ event, expanded, onToggle, onEdit }: {
  event: CalendarEventResponse;
  expanded: boolean;
  onToggle: () => void;
  onEdit: () => void;
}) {
  const startDate = dateFormatter.format(new Date(event.startAt));
  const time = event.allDay ? '종일' : timeFormatter.format(new Date(event.startAt));
  return (
    <li className="calendar-events-page__item">
      <button className="calendar-events-page__event" type="button" aria-expanded={expanded} onClick={onToggle}>
        <span className="calendar-events-page__event-copy">
          <strong>{event.title}</strong>
          <span>{startDate} · {typeLabels[event.eventType]}</span>
        </span>
        <span className="calendar-events-page__event-time">{time}</span>
      </button>
      {expanded && (
        <div className="calendar-events-page__details">
          {event.description && <p>{event.description}</p>}
          {event.endAt && <p>종료: {dateFormatter.format(new Date(event.endAt))} · {timeFormatter.format(new Date(event.endAt))}</p>}
          <button type="button" onClick={onEdit}><Pencil size={15} /> 수정</button>
        </div>
      )}
    </li>
  );
}

export function CalendarEventsPage() {
  const eventsQuery = useCalendarEventsQuery();
  const categoriesQuery = useCategoriesQuery();
  const mutations = useCalendarEventMutation();
  const [isCreating, setIsCreating] = useState(false);
  const [editingEvent, setEditingEvent] = useState<CalendarEventResponse | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const events = eventsQuery.data ?? [];
  const modalOpen = isCreating || editingEvent !== null;
  const isSaving = mutations.create.isPending || mutations.update.isPending;
  const saveError = mutations.create.error?.message ?? mutations.update.error?.message ?? null;

  const closeModal = () => {
    if (isSaving) return;
    setIsCreating(false);
    setEditingEvent(null);
    mutations.create.reset();
    mutations.update.reset();
  };

  const saveEvent = async (request: CalendarEventRequest) => {
    if (editingEvent) {
      await mutations.update.mutateAsync({ id: editingEvent.calendarEventId, request });
    } else {
      await mutations.create.mutateAsync(request);
    }
    closeModal();
  };

  return (
    <main className="calendar-events-page" aria-labelledby="calendar-events-page-title">
      <header className="calendar-events-page__header">
        <div>
          <span className="page-placeholder__eyebrow">CALENDAR</span>
          <h1 id="calendar-events-page-title">Calendar</h1>
          <p>개인 일정과 게임 이벤트를 관리하세요.</p>
        </div>
        <button type="button" onClick={() => { mutations.create.reset(); setIsCreating(true); }}>
          <Plus size={17} /> 일정 추가
        </button>
      </header>

      {eventsQuery.isLoading ? (
        <div className="calendar-events-page__state" role="status"><LoaderCircle className="calendar-events-page__spinner" size={24} /><p>일정을 불러오는 중입니다.</p></div>
      ) : eventsQuery.isError ? (
        <div className="calendar-events-page__state calendar-events-page__state--error" role="alert">
          <CircleAlert size={24} /><p>{eventsQuery.error.message}</p>
          <button type="button" onClick={() => void eventsQuery.refetch()}><RotateCw size={16} /> 다시 시도</button>
        </div>
      ) : events.length === 0 ? (
        <div className="calendar-events-page__state" role="status"><CalendarDays size={24} /><p>등록된 일정이 없습니다.</p></div>
      ) : (
        <ul className="calendar-events-page__items">
          {events.map((event) => (
            <CalendarEventRow
              key={event.calendarEventId}
              event={event}
              expanded={expandedId === event.calendarEventId}
              onToggle={() => setExpandedId((current) => current === event.calendarEventId ? null : event.calendarEventId)}
              onEdit={() => { mutations.update.reset(); setEditingEvent(event); setIsCreating(false); }}
            />
          ))}
        </ul>
      )}

      {modalOpen && categoriesQuery.isError && (
        <div className="calendar-events-page__category-error" role="alert">
          Category 목록을 불러오지 못했습니다. {categoriesQuery.error.message}
          <button type="button" onClick={() => void categoriesQuery.refetch()}>다시 시도</button>
        </div>
      )}

      {modalOpen && !categoriesQuery.isLoading && !categoriesQuery.isError && (
        <CalendarEventFormModal
          key={editingEvent?.calendarEventId ?? 'new-event'}
          event={editingEvent}
          categories={categoriesQuery.data ?? []}
          isSaving={isSaving}
          error={saveError}
          onClose={closeModal}
          onSave={saveEvent}
        />
      )}
    </main>
  );
}
