import {
  Award,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Gamepad2,
  History,
  Settings,
  Sparkles,
  Target,
} from 'lucide-react'
import type { NavigationItem } from '../../types/navigation'
import { useLayoutStore } from '../../store/layoutStore'

import './Sidebar.css'

const navigationItems: NavigationItem[] = [
  {
    key: 'today',
    label: 'Today',
    description: '오늘의 Quest와 진행 상태',
    icon: Target,
  },
  {
    key: 'calendar',
    label: 'Calendar',
    description: '일정과 예정된 이벤트',
    icon: CalendarDays,
  },
  {
    key: 'weekly',
    label: 'Weekly',
    description: '주간 활동과 통계',
    icon: Sparkles,
  },
  {
    key: 'game',
    label: 'Game',
    description: '게임 Quest 관리',
    icon: Gamepad2,
  },
  {
    key: 'achievement',
    label: 'Achievement',
    description: '획득한 Achievement',
    icon: Award,
  },
  {
    key: 'history',
    label: 'History',
    description: '수행 기록과 이력',
    icon: History,
  },
  {
    key: 'settings',
    label: 'Settings',
    description: '앱 설정',
    icon: Settings,
  },
]

export function Sidebar() {
  const activeNavigation = useLayoutStore((state) => state.activeNavigation)
  const isSidebarCollapsed = useLayoutStore((state) => state.isSidebarCollapsed)
  const selectNavigation = useLayoutStore((state) => state.selectNavigation)
  const toggleSidebar = useLayoutStore((state) => state.toggleSidebar)

  return (
    <aside
      className={`sidebar${isSidebarCollapsed ? ' sidebar--collapsed' : ''}`}
      aria-label="Ilque 주 메뉴"
    >
      <div className="sidebar__brand">
        <div className="sidebar__brand-mark" aria-hidden="true">
          I
        </div>
        <div className="sidebar__brand-copy">
          <strong>ilque</strong>
          <span>Quest companion</span>
        </div>
      </div>

      <nav className="sidebar__nav" aria-label="주 메뉴">
        <span className="sidebar__section-label">Navigate</span>
        <ul>
          {navigationItems.map(({ key, label, description, icon: Icon }) => {
            const isActive = activeNavigation === key

            return (
              <li key={key}>
                <button
                  className={`sidebar__item${isActive ? ' sidebar__item--active' : ''}`}
                  type="button"
                  aria-current={isActive ? 'page' : undefined}
                  aria-label={isSidebarCollapsed ? `${label}: ${description}` : undefined}
                  title={isSidebarCollapsed ? description : undefined}
                  onClick={() => selectNavigation(key)}
                >
                  <Icon aria-hidden="true" size={20} strokeWidth={1.8} />
                  <span className="sidebar__item-label">{label}</span>
                </button>
              </li>
            )
          })}
        </ul>
      </nav>

      <button
        className="sidebar__collapse-button"
        type="button"
        aria-label={isSidebarCollapsed ? 'Sidebar 펼치기' : 'Sidebar 접기'}
        title={isSidebarCollapsed ? 'Sidebar 펼치기' : 'Sidebar 접기'}
        onClick={toggleSidebar}
      >
        {isSidebarCollapsed ? <ChevronRight aria-hidden="true" size={18} /> : <ChevronLeft aria-hidden="true" size={18} />}
        <span className="sidebar__collapse-label">
          {isSidebarCollapsed ? '펼치기' : '접기'}
        </span>
      </button>
    </aside>
  )
}
