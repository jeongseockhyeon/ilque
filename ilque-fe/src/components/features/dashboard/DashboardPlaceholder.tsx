import {
  CalendarClock,
  CheckSquare2,
  Focus,
  ListTodo,
  type LucideIcon,
} from 'lucide-react'
import type { NavigationItem } from '../../../types/navigation'

import './DashboardPlaceholder.css'

interface PlaceholderCardProps {
  icon: LucideIcon
  title: string
  description: string
}

function PlaceholderCard({ icon: Icon, title, description }: PlaceholderCardProps) {
  return (
    <article className="dashboard-placeholder__card">
      <div className="dashboard-placeholder__card-icon" aria-hidden="true">
        <Icon size={20} strokeWidth={1.8} />
      </div>
      <div>
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
      <span className="dashboard-placeholder__status">연동 예정</span>
    </article>
  )
}

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
    <section className="dashboard-placeholder" aria-labelledby="today-placeholder-title">
      <div className="dashboard-placeholder__intro">
        <div>
          <span className="page-placeholder__eyebrow">YOUR NEXT ADVENTURE</span>
          <h2 id="today-placeholder-title">오늘의 여정을 준비하세요</h2>
          <p>오늘의 Quest와 집중 활동을 한 곳에서 확인할 수 있습니다.</p>
        </div>
        <span className="dashboard-placeholder__badge">Today</span>
      </div>

      <div className="dashboard-placeholder__grid">
        <PlaceholderCard
          icon={CheckSquare2}
          title="Today Quest"
          description="오늘 수행할 Quest Execution"
        />
        <PlaceholderCard
          icon={ListTodo}
          title="Quick Todo"
          description="가볍게 기록하는 일회성 할 일"
        />
        <PlaceholderCard
          icon={Focus}
          title="Focus"
          description="Quest에 집중하는 타이머"
        />
        <PlaceholderCard
          icon={CalendarClock}
          title="Upcoming Events"
          description="다가오는 일정과 이벤트"
        />
      </div>
    </section>
  )
}
