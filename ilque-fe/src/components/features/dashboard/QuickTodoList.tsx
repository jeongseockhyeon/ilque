import { useState } from 'react';
import { CircleAlert, ClipboardList, LoaderCircle, RotateCw } from 'lucide-react';
import { useQuickTodosQuery } from '../../../hooks/useQuickTodosQuery';
import { useQuickTodoMutation } from '../../../hooks/useQuickTodoMutation';
import type { QuickTodoResponse } from '../../../api/types';

import './QuickTodoList.css';

const timeFormatter = new Intl.DateTimeFormat('ko-KR', { hour: '2-digit', minute: '2-digit' });

function localDateString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function QuickTodoItem({ todo, date, complete, pending, error }: {
  todo: QuickTodoResponse;
  date: string;
  complete: (id: number, date: string) => Promise<void>;
  pending: boolean;
  error: string | null;
}) {
  const [isFailed, setIsFailed] = useState(false);
  const completed = todo.completedAt !== null;

  return (
    <li className="quick-todo-list__item">
      <label className={`quick-todo-list__row${completed ? ' quick-todo-list__row--completed' : ''}`}>
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
        <span className="quick-todo-list__title">{todo.title}</span>
        <time dateTime={completed ? todo.completedAt ?? undefined : todo.createdAt}>
          {timeFormatter.format(new Date(completed ? todo.completedAt! : todo.createdAt))}
        </time>
        {pending && <LoaderCircle className="quick-todo-list__spinner" aria-label="처리 중" size={16} />}
      </label>
      {isFailed && error !== null && <p className="quick-todo-list__item-error" role="alert">{error}</p>}
    </li>
  );
}

export function QuickTodoList() {
  const [date] = useState(() => localDateString(new Date()));
  const { data, error, isError, isLoading, refetch } = useQuickTodosQuery(date);
  const mutation = useQuickTodoMutation();
  const todos = data ?? [];

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

      {isLoading ? (
        <div className="quick-todo-list__state" role="status">
          <LoaderCircle className="quick-todo-list__spinner" aria-hidden="true" size={24} />
          <p>Quick Todo를 불러오는 중입니다.</p>
        </div>
      ) : isError ? (
        <div className="quick-todo-list__state quick-todo-list__state--error" role="alert">
          <CircleAlert aria-hidden="true" size={24} />
          <p>{error instanceof Error ? error.message : 'Quick Todo를 불러오지 못했습니다.'}</p>
          <button type="button" onClick={() => void refetch()}>
            <RotateCw aria-hidden="true" size={16} /> 다시 시도
          </button>
        </div>
      ) : todos.length === 0 ? (
        <div className="quick-todo-list__state" role="status">
          <ClipboardList aria-hidden="true" size={24} />
          <p>오늘의 Quick Todo가 없습니다.</p>
        </div>
      ) : (
        <ul className="quick-todo-list__items">
          {todos.map((todo) => (
            <QuickTodoItem
              key={todo.quickTodoId}
              todo={todo}
              date={date}
              complete={mutation.complete}
              pending={mutation.isPending(todo.quickTodoId)}
              error={mutation.getError(todo.quickTodoId)}
            />
          ))}
        </ul>
      )}
    </section>
  );
}
