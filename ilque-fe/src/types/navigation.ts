import type { LucideIcon } from 'lucide-react'

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
