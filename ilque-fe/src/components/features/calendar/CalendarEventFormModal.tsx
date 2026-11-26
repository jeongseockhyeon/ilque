import { useState, type FormEvent } from 'react';
import { X } from 'lucide-react';
import type { CalendarEventResponse, CalendarEventType, CategoryResponse } from '../../../api/types';
import type { CalendarEventRequest } from '../../../services/calendarService';

interface CalendarEventFormModalProps {
  event: CalendarEventResponse | null;
  categories: CategoryResponse[];
  isSaving: boolean;
  error: string | null;
  onClose: () => void;
  onSave: (request: CalendarEventRequest) => Promise<void>;
}

const eventTypes: { value: CalendarEventType; label: string }[] = [
  { value: 'GENERAL', label: '일반 일정' },
  { value: 'GAME_EVENT', label: '게임 이벤트' },
  { value: 'GAME_PICKUP', label: '게임 픽업' },
  { value: 'GAME_UPDATE', label: '게임 업데이트' },
];

function dateTimeInSeoul(value: string): string {
  const parts = new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
  }).formatToParts(new Date(value));
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((item) => item.type === type)?.value ?? '';
  return `${part('year')}-${part('month')}-${part('day')}T${part('hour')}:${part('minute')}`;
}

function nowInSeoul(): string {
  const parts = new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
  }).formatToParts(new Date());
  const part = (type: Intl.DateTimeFormatPartTypes) => parts.find((item) => item.type === type)?.value ?? '';
  return `${part('year')}-${part('month')}-${part('day')}T${part('hour')}:${part('minute')}`;
}

function toUtcInstant(value: string): string {
  return new Date(`${value}:00+09:00`).toISOString();
}

export function CalendarEventFormModal({ event, categories, isSaving, error, onClose, onSave }: CalendarEventFormModalProps) {
  const [title, setTitle] = useState(event?.title ?? '');
  const [description, setDescription] = useState(event?.description ?? '');
  const [eventType, setEventType] = useState<CalendarEventType>(event?.eventType ?? 'GENERAL');
  const [startAt, setStartAt] = useState(event ? dateTimeInSeoul(event.startAt) : nowInSeoul());
  const [endAt, setEndAt] = useState(event?.endAt ? dateTimeInSeoul(event.endAt) : '');
  const [allDay, setAllDay] = useState(event?.allDay ?? false);
  const [reminderAt, setReminderAt] = useState(event?.reminderAt ? dateTimeInSeoul(event.reminderAt) : '');
  const [categoryId, setCategoryId] = useState(event?.categoryId?.toString() ?? '');
  const [formError, setFormError] = useState<string | null>(null);

  const submit = async (formEvent: FormEvent<HTMLFormElement>) => {
    formEvent.preventDefault();
    if (!title.trim() || !startAt) {
      setFormError('제목과 시작 시각을 입력해 주세요.');
      return;
    }
    if (endAt && new Date(`${endAt}:00+09:00`) < new Date(`${startAt}:00+09:00`)) {
      setFormError('종료 시각은 시작 시각보다 빠를 수 없습니다.');
      return;
    }
    setFormError(null);
    try {
      await onSave({
        title: title.trim(),
        description: description.trim() || null,
        eventType,
        startAt: toUtcInstant(startAt),
        endAt: endAt ? toUtcInstant(endAt) : null,
        allDay,
        reminderAt: reminderAt ? toUtcInstant(reminderAt) : null,
        categoryId: categoryId ? Number(categoryId) : null,
      });
    } catch {
      // Keep the form open; the server error is rendered below.
    }
  };

  return (
    <div className="calendar-event-modal__backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget && !isSaving) onClose(); }}>
      <section className="calendar-event-modal" role="dialog" aria-modal="true" aria-labelledby="calendar-event-modal-title">
        <header className="calendar-event-modal__header">
          <h2 id="calendar-event-modal-title">{event ? '일정 수정' : '일정 추가'}</h2>
          <button type="button" aria-label="닫기" onClick={onClose} disabled={isSaving}><X size={20} /></button>
        </header>
        <form className="calendar-event-modal__form" onSubmit={(e) => void submit(e)}>
          <label>제목<input autoFocus maxLength={100} value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
          <label>유형<select value={eventType} onChange={(e) => setEventType(e.target.value as CalendarEventType)}>
            {eventTypes.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}
          </select></label>
          <label>시작 시각<input type="datetime-local" value={startAt} onChange={(e) => setStartAt(e.target.value)} required /></label>
          <label>종료 시각 <span>(선택)</span><input type="datetime-local" value={endAt} onChange={(e) => setEndAt(e.target.value)} /></label>
          <label className="calendar-event-modal__checkbox"><input type="checkbox" checked={allDay} onChange={(e) => setAllDay(e.target.checked)} /> 종일 일정</label>
          <label>설명 <span>(선택)</span><textarea maxLength={1000} rows={3} value={description} onChange={(e) => setDescription(e.target.value)} /></label>
          <label>알림 시각 <span>(선택)</span><input type="datetime-local" value={reminderAt} onChange={(e) => setReminderAt(e.target.value)} /></label>
          <label>Category <span>(선택)</span><select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
            <option value="">지정 안 함</option>
            {categories.filter((category) => category.active).map((category) =>
              <option key={category.categoryId} value={category.categoryId}>{category.name}</option>)}
          </select></label>
          {(formError || error) && <p className="calendar-event-modal__error" role="alert">{formError ?? error}</p>}
          <footer className="calendar-event-modal__actions">
            <button type="button" className="calendar-event-modal__cancel" onClick={onClose} disabled={isSaving}>취소</button>
            <button type="submit" disabled={isSaving}>{isSaving ? '저장 중…' : '저장'}</button>
          </footer>
        </form>
      </section>
    </div>
  );
}
