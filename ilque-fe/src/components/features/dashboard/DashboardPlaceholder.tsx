import type { NavigationItem } from '../../../types/navigation'
import { TodayQuestList } from './TodayQuestList'
import { QuickTodoList } from './QuickTodoList'
import { UpcomingEventsList } from './UpcomingEventsList'

import './DashboardPlaceholder.css'

interface DashboardPlaceholderProps {
  activePage: NavigationItem
}

export function DashboardPlaceholder({ activePage }: DashboardPlaceholderProps) {
  const PageIcon = activePage.icon

  if (activePage.key !== 'today') {
    return (
      <section className="page-placeholder" aria-labelledby="page-placeholder-title">
        <div className="page-placeholder__icon" aria-hidden="true">
          <PageIcon size={28} strokeWidth={1.6} />
        </div>
        <span className="page-placeholder__eyebrow">COMING NEXT</span>
        <h2 id="page-placeholder-title">{activePage.label}</h2>
        <p>{activePage.description}</p>
        <span className="page-placeholder__note">이 기능은 다음 단계에서 구현됩니다.</span>
      </section>
    )
  }

  return (
    <div className="dashboard-placeholder">
      <TodayQuestList />
      <QuickTodoList />
      <UpcomingEventsList />
    </div>
  )
}
