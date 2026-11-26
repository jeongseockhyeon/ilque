import { useState } from 'react';
import { CalendarDays, ChevronLeft, ChevronRight, CircleAlert, LoaderCircle, Pencil, Plus, RotateCw } from 'lucide-react';
import type { CalendarEventResponse, CalendarEventType } from '../../../api/types';
import { useCalendarEventMutation } from '../../../hooks/useCalendarEventMutation';
import { useCalendarEventsQuery, useCategoriesQuery } from '../../../hooks/useCalendarManagementQuery';
import { CalendarEventFormModal } from './CalendarEventFormModal';
import type { CalendarEventRequest } from '../../../services/calendarService';

import './CalendarEventsPage.css';

const timeZone = 'Asia/Seoul';
const typeLabels: Record<CalendarEventType, string> = {
  GENERAL: '일반 일정',
  GAME_EVENT: '게임 이벤트',
  GAME_PICKUP: '게임 픽업',
  GAME_UPDATE: '게임 업데이트',
};
const dateKeyFormatter = new Intl.DateTimeFormat('en-CA', {
  timeZone, year: 'numeric', month: '2-digit', day: '2-digit',
});
const monthLabelFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone, year: 'numeric', month: 'long',
});
const dayLabelFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone, month: 'long', day: 'numeric', weekday: 'short',
});
const timeFormatter = new Intl.DateTimeFormat('ko-KR', {
  timeZone, hour: '2-digit', minute: '2-digit',
});
const weekdays = ['일', '월', '화', '수', '목', '금', '토'];

function dateKey(date: Date): string {
  return dateKeyFormatter.format(date);
}

function addDays(date: string, amount: number): string {
  const [year, month, day] = date.split('-').map(Number);
  return new Date(Date.UTC(year, month - 1, day + amount)).toISOString().slice(0, 10);
}

function monthKey(date: string): string {
  return date.slice(0, 7);
}

function dateFromKey(key: string): Date {
  return new Date(`${key}T12:00:00+09:00`);
}

function monthDate(month: string, day: number): string {
  return `${month}-${String(day).padStart(2, '0')}`;
}

function shiftMonth(month: string, amount: number, selectedDate: string): { month: string; selectedDate: string } {
  const [year, monthNumber] = month.split('-').map(Number);
  const nextFirst = new Date(Date.UTC(year, monthNumber - 1 + amount, 1));
  const nextMonth = nextFirst.toISOString().slice(0, 7);
  const selectedDay = Number(selectedDate.slice(-2));
  const daysInMonth = new Date(Date.UTC(nextFirst.getUTCFullYear(), nextFirst.getUTCMonth() + 1, 0)).getUTCDate();
  const nextDate = monthDate(nextMonth, Math.min(selectedDay, daysInMonth));
  return { month: nextMonth, selectedDate: nextDate };
}

function getVisibleRange(month: string): { from: string; to: string } {
  const [year, monthNumber] = month.split('-').map(Number);
  const first = new Date(Date.UTC(year, monthNumber - 1, 1));
  const last = new Date(Date.UTC(year, monthNumber, 0));
  const from = addDays(monthDate(month, 1), -first.getUTCDay());
  const to = addDays(monthDate(month, last.getUTCDate()), 6 - last.getUTCDay());
  return { from, to };
}

function getGridDates(from: string, to: string): string[] {
  const dates: string[] = [];
  for (let date = from; date <= to; date = addDays(date, 1)) dates.push(date);
  return dates;
}

function eventDateRange(event: CalendarEventResponse): { start: string; end: string } {
  const start = dateKey(new Date(event.startAt));
  return { start, end: event.endAt ? dateKey(new Date(event.endAt)) : start };
}

function eventOccursOn(event: CalendarEventResponse, date: string): boolean {
  const range = eventDateRange(event);
  return range.start <= date && date <= range.end;
}

function eventTime(event: CalendarEventResponse): string {
  if (event.allDay) return '종일';
  const start = timeFormatter.format(new Date(event.startAt));
  return event.endAt ? `${start} – ${timeFormatter.format(new Date(event.endAt))}` : start;
}

function EventDetails({ event, onEdit }: { event: CalendarEventResponse; onEdit: () => void }) {
  return (
    <article className="calendar-events-page__detail">
      <div className="calendar-events-page__detail-heading">
        <div><h4>{event.title}</h4><span>{typeLabels[event.eventType]}</span></div>
        <button type="button" onClick={onEdit}><Pencil size={15} /> 수정</button>
      </div>
      <p>{dayLabelFormatter.format(new Date(event.startAt))} · {eventTime(event)}</p>
      {event.endAt && <p>종료: {dayLabelFormatter.format(new Date(event.endAt))} · {timeFormatter.format(new Date(event.endAt))}</p>}
      {event.description && <p className="calendar-events-page__description">{event.description}</p>}
    </article>
  );
}

export function CalendarEventsPage() {
  const [today] = useState(() => dateKey(new Date()));
  const [month, setMonth] = useState(() => monthKey(dateKey(new Date())));
  const [selectedDate, setSelectedDate] = useState(() => dateKey(new Date()));
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [editingEvent, setEditingEvent] = useState<CalendarEventResponse | null>(null);
  const range = getVisibleRange(month);
  const eventsQuery = useCalendarEventsQuery(range.from, range.to);
  const categoriesQuery = useCategoriesQuery();
  const mutations = useCalendarEventMutation();
  const events = eventsQuery.data ?? [];
  const selectedEvents = events.filter((event) => eventOccursOn(event, selectedDate));
  const gridDates = getGridDates(range.from, range.to);
  const modalOpen = isCreating || editingEvent !== null;
  const isSaving = mutations.create.isPending || mutations.update.isPending;
  const saveError = mutations.create.error?.message ?? mutations.update.error?.message ?? null;

  const changeMonth = (offset: number) => {
    const next = shiftMonth(month, offset, selectedDate);
    setMonth(next.month);
    setSelectedDate(next.selectedDate);
    setExpandedId(null);
  };

  const selectDate = (date: string) => {
    setSelectedDate(date);
    setMonth(monthKey(date));
    setExpandedId(null);
  };

  const closeModal = () => {
    if (isSaving) return;
    setIsCreating(false);
    setEditingEvent(null);
    mutations.create.reset();
    mutations.update.reset();
  };

  const saveEvent = async (request: CalendarEventRequest) => {
    if (editingEvent) await mutations.update.mutateAsync({ id: editingEvent.calendarEventId, request });
    else await mutations.create.mutateAsync(request);
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

      <section className="calendar-events-page__calendar" aria-label="월간 일정 달력">
        <div className="calendar-events-page__month-toolbar">
          <h2>{monthLabelFormatter.format(dateFromKey(`${month}-01`))}</h2>
          <div>
            <button type="button" aria-label="이전 달" onClick={() => changeMonth(-1)}><ChevronLeft size={20} /></button>
            <button type="button" aria-label="다음 달" onClick={() => changeMonth(1)}><ChevronRight size={20} /></button>
          </div>
        </div>

        {eventsQuery.isLoading ? (
          <div className="calendar-events-page__state" role="status"><LoaderCircle className="calendar-events-page__spinner" size={24} /><p>일정을 불러오는 중입니다.</p></div>
        ) : eventsQuery.isError ? (
          <div className="calendar-events-page__state calendar-events-page__state--error" role="alert">
            <CircleAlert size={24} /><p>{eventsQuery.error.message}</p>
            <button type="button" onClick={() => void eventsQuery.refetch()}><RotateCw size={16} /> 다시 시도</button>
          </div>
        ) : (
          <div className="calendar-events-page__grid">
            {weekdays.map((day) => <div className="calendar-events-page__weekday" key={day}>{day}</div>)}
            {gridDates.map((date) => {
              const dayEvents = events.filter((event) => eventOccursOn(event, date));
              const outsideMonth = monthKey(date) !== month;
              const visibleEvents = dayEvents.slice(0, 2);
              return (
                <div className={`calendar-events-page__cell${outsideMonth ? ' calendar-events-page__cell--outside' : ''}${date === selectedDate ? ' calendar-events-page__cell--selected' : ''}${date === today ? ' calendar-events-page__cell--today' : ''}`} key={date}>
                  <button className="calendar-events-page__date-button" type="button" aria-pressed={date === selectedDate}
                    aria-label={dayLabelFormatter.format(dateFromKey(date))} onClick={() => selectDate(date)}>
                    {Number(date.slice(-2))}
                  </button>
                  <div className="calendar-events-page__cell-events">
                    {visibleEvents.map((event) => (
                      <button className="calendar-events-page__event-chip" type="button" key={event.calendarEventId}
                        title={event.title} onClick={() => { selectDate(date); setExpandedId(event.calendarEventId); }}>
                        <span>{event.title}</span>
                      </button>
                    ))}
                    {dayEvents.length > 2 && <button className="calendar-events-page__more" type="button" onClick={() => selectDate(date)}>+{dayEvents.length - 2}</button>}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {!eventsQuery.isLoading && !eventsQuery.isError && (
        <section className="calendar-events-page__selected-day" aria-labelledby="calendar-selected-day-title">
          <header><div><span className="page-placeholder__eyebrow">SELECTED DATE</span><h3 id="calendar-selected-day-title">{dayLabelFormatter.format(dateFromKey(selectedDate))}</h3></div>
            <span className="dashboard-placeholder__badge">{selectedEvents.length}개</span>
          </header>
          {selectedEvents.length === 0 ? (
            <div className="calendar-events-page__state" role="status"><CalendarDays size={22} /><p>이 날짜에 예정된 일정이 없습니다.</p></div>
          ) : (
            <ul className="calendar-events-page__items">
              {selectedEvents.map((event) => (
                <li key={event.calendarEventId} className="calendar-events-page__item">
                  <button className="calendar-events-page__event" type="button" aria-expanded={expandedId === event.calendarEventId}
                    onClick={() => setExpandedId((current) => current === event.calendarEventId ? null : event.calendarEventId)}>
                    <span className="calendar-events-page__event-copy"><strong>{event.title}</strong><span>{typeLabels[event.eventType]}</span></span>
                    <span className="calendar-events-page__event-time">{eventTime(event)}</span>
                  </button>
                  {expandedId === event.calendarEventId && <EventDetails event={event} onEdit={() => { mutations.update.reset(); setEditingEvent(event); setIsCreating(false); }} />}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}

      {modalOpen && categoriesQuery.isError && (
        <div className="calendar-events-page__category-error" role="alert">Category 목록을 불러오지 못했습니다. {categoriesQuery.error.message}
          <button type="button" onClick={() => void categoriesQuery.refetch()}>다시 시도</button>
        </div>
      )}
      {modalOpen && !categoriesQuery.isLoading && !categoriesQuery.isError && (
        <CalendarEventFormModal key={editingEvent?.calendarEventId ?? 'new-event'} event={editingEvent}
          categories={categoriesQuery.data ?? []} isSaving={isSaving} error={saveError} onClose={closeModal} onSave={saveEvent} />
      )}
    </main>
  );
}
