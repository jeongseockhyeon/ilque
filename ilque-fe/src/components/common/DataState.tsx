import type { ReactNode } from 'react';
import { CircleAlert, LoaderCircle, RotateCw } from 'lucide-react';

import './DataState.css';

interface StateContentProps {
  message: string;
  icon?: ReactNode;
  compact?: boolean;
  className?: string;
}

interface LoadingStateProps extends StateContentProps {
  label?: string;
}

interface EmptyStateProps extends StateContentProps {}

interface ErrorStateProps extends StateContentProps {
  onRetry?: () => void;
  retrying?: boolean;
}

export function LoadingState({ message, label = message, icon, compact = false, className = '' }: LoadingStateProps) {
  return (
    <div className={`data-state data-state--loading${compact ? ' data-state--compact' : ''} ${className}`} role="status" aria-live="polite">
      {icon ?? <LoaderCircle className="data-state__spinner" aria-hidden="true" size={compact ? 16 : 24} />}
      <p>{message}</p>
      <span className="data-state__sr-only">{label}</span>
    </div>
  );
}

export function EmptyState({ message, icon, compact = false, className = '' }: EmptyStateProps) {
  return (
    <div className={`data-state data-state--empty${compact ? ' data-state--compact' : ''} ${className}`} role="status">
      {icon}
      <p>{message}</p>
    </div>
  );
}

export function ErrorState({ message, icon, onRetry, retrying = false, compact = false, className = '' }: ErrorStateProps) {
  return (
    <div className={`data-state data-state--error${compact ? ' data-state--compact' : ''} ${className}`} role="alert">
      {icon ?? <CircleAlert aria-hidden="true" size={compact ? 16 : 24} />}
      <p>{message}</p>
      {onRetry && (
        <button type="button" onClick={onRetry} disabled={retrying}>
          <RotateCw aria-hidden="true" size={16} /> {retrying ? '다시 불러오는 중' : '다시 시도'}
        </button>
      )}
    </div>
  );
}
