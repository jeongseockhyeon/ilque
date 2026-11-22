import {
  Award,
  CalendarDays,
  Gamepad2,
  History,
  Settings,
  Sparkles,
  Target,
  type LucideIcon,
} from 'lucide-react'

export type NavigationKey =
  | 'today'
  | 'calendar'
  | 'weekly'
  | 'game'
  | 'achievement'
  | 'history'
  | 'settings'

export interface NavigationItem {
  key: NavigationKey
  label: string
  description: string
  icon: LucideIcon
}

export const navigationItems: NavigationItem[] = [
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
