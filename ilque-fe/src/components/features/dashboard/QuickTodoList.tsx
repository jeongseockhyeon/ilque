import { useState, type FormEvent } from 'react';
import { Check, ClipboardList, LoaderCircle, Pencil, Plus, X } from 'lucide-react';
import { useQuickTodosQuery } from '../../../hooks/useQuickTodosQuery';
import { useQuickTodoMutation } from '../../../hooks/useQuickTodoMutation';
import type { QuickTodoResponse } from '../../../api/types';
import { EmptyState, ErrorState, LoadingState } from '../../common/DataState';

import './QuickTodoList.css';

const timeFormatter = new Intl.DateTimeFormat('ko-KR', { hour: '2-digit', minute: '2-digit' });

function localDateString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function QuickTodoItem({ todo, date, complete, update, pending, error }: {
  todo: QuickTodoResponse;
  date: string;
  complete: (id: number, date: string) => Promise<void>;
  update: (id: number, title: string, date: string) => Promise<void>;
  pending: boolean;
  error: string | null;
}) {
  const [isFailed, setIsFailed] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(todo.title);
  const [editError, setEditError] = useState<string | null>(null);
  const completed = todo.completedAt !== null;

  const saveTitle = async () => {
    const normalizedTitle = title.trim();
    if (!normalizedTitle || normalizedTitle.length > 100) {
      setEditError('제목은 1자 이상 100자 이하로 입력해 주세요.');
      return;
    }
    setEditError(null);
    try {
      await update(todo.quickTodoId, normalizedTitle, date);
      setIsEditing(false);
    } catch (saveError) {
      setEditError(saveError instanceof Error ? saveError.message : 'Quick Todo를 수정하지 못했습니다.');
    }
  };

  return (
    <li className="quick-todo-list__item">
      <div className={`quick-todo-list__row${completed ? ' quick-todo-list__row--completed' : ''}`}>
        <input
          type="checkbox"
          checked={completed}
          disabled={completed || pending}
          onChange={() => {
            setIsFailed(false);
            void complete(todo.quickTodoId, date).catch(() => setIsFailed(true));
          }}
          aria-label={`${todo.title} 완료`}
        />
        {isEditing ? (
          <input
            className="quick-todo-list__edit-input"
            aria-label="Quick Todo 제목"
            maxLength={100}
            value={title}
            disabled={pending}
            onChange={(event) => setTitle(event.target.value)}
          />
        ) : <span className="quick-todo-list__title">{todo.title}</span>}
        <time dateTime={completed ? todo.completedAt ?? undefined : todo.createdAt}>
          {timeFormatter.format(new Date(completed ? todo.completedAt! : todo.createdAt))}
        </time>
        {pending && <LoaderCircle className="quick-todo-list__spinner" aria-label="처리 중" size={16} />}
        {!completed && !isEditing && (
          <button className="quick-todo-list__icon-button" type="button" aria-label="제목 수정"
            onClick={() => { setTitle(todo.title); setIsEditing(true); setEditError(null); }}>
            <Pencil size={16} />
          </button>
        )}
        {!completed && isEditing && (
          <span className="quick-todo-list__edit-actions">
            <button className="quick-todo-list__icon-button" type="button" aria-label="수정 저장"
              disabled={pending} onClick={() => void saveTitle()}><Check size={16} /></button>
            <button className="quick-todo-list__icon-button" type="button" aria-label="수정 취소"
              disabled={pending} onClick={() => { setIsEditing(false); setTitle(todo.title); setEditError(null); }}><X size={16} /></button>
          </span>
        )}
      </div>
      {isFailed && error !== null && <p className="quick-todo-list__item-error" role="alert">{error}</p>}
      {editError !== null && <p className="quick-todo-list__item-error" role="alert">{editError}</p>}
    </li>
  );
}

export function QuickTodoList() {
  const [date] = useState(() => localDateString(new Date()));
  const { data, error, isError, isLoading, isFetching, refetch } = useQuickTodosQuery(date);
  const mutation = useQuickTodoMutation();
  const [newTitle, setNewTitle] = useState('');
  const [createError, setCreateError] = useState<string | null>(null);
  const todos = data ?? [];

  const submitNewTodo = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const title = newTitle.trim();
    if (!title || title.length > 100) {
      setCreateError('제목은 1자 이상 100자 이하로 입력해 주세요.');
      return;
    }
    setCreateError(null);
    try {
      await mutation.create(title, date);
      setNewTitle('');
    } catch (creationError) {
      setCreateError(creationError instanceof Error ? creationError.message : 'Quick Todo를 추가하지 못했습니다.');
    }
  };

  return (
    <section className="quick-todo-list" aria-labelledby="quick-todo-list-title" aria-busy={isLoading}>
      <header className="quick-todo-list__header">
        <div>
          <span className="page-placeholder__eyebrow">QUICK TODO</span>
          <h2 id="quick-todo-list-title">빠른 할 일</h2>
          <p>오늘 처리할 항목을 확인하세요.</p>
        </div>
        <span className="dashboard-placeholder__badge">{todos.length}개</span>
      </header>

      <form className="quick-todo-list__create-form" onSubmit={(event) => void submitNewTodo(event)}>
        <label className="quick-todo-list__sr-only" htmlFor="quick-todo-new-title">새 Quick Todo</label>
        <input id="quick-todo-new-title" type="text" maxLength={100} value={newTitle}
          onChange={(event) => { setNewTitle(event.target.value); setCreateError(null); }}
          placeholder="새 할 일 입력" disabled={mutation.isCreating} />
        <button type="submit" disabled={mutation.isCreating}>
          {mutation.isCreating ? <LoaderCircle className="quick-todo-list__spinner" size={16} /> : <Plus size={16} />}
          추가
        </button>
      </form>
      {createError !== null && <p className="quick-todo-list__item-error" role="alert">{createError}</p>}

      {isLoading ? (
        <LoadingState className="quick-todo-list__state" message="Quick Todo를 불러오는 중입니다." />
      ) : isError && data === undefined ? (
        <ErrorState className="quick-todo-list__state" message={error instanceof Error ? error.message : 'Quick Todo를 불러오지 못했습니다.'} onRetry={() => void refetch()} retrying={isFetching} />
      ) : (
        <>
        {isError && <ErrorState className="quick-todo-list__inline-state" compact message={error instanceof Error ? error.message : 'Quick Todo를 갱신하지 못했습니다.'} onRetry={() => void refetch()} retrying={isFetching} />}
        {isFetching && <LoadingState className="quick-todo-list__inline-state" compact message="Quick Todo 갱신 중" />}
        {todos.length === 0 ? <EmptyState className="quick-todo-list__state" icon={<ClipboardList aria-hidden="true" size={24} />} message="오늘의 Quick Todo가 없습니다." /> : (
        <ul className="quick-todo-list__items">
          {todos.map((todo) => (
            <QuickTodoItem
              key={todo.quickTodoId}
              todo={todo}
              date={date}
              complete={mutation.complete}
              update={mutation.update}
              pending={mutation.isPending(todo.quickTodoId)}
              error={mutation.getError(todo.quickTodoId)}
            />
          ))}
        </ul>
        )}
        </>
      )}
    </section>
  );
}
