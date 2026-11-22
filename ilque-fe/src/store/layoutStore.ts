import { create } from 'zustand'
import type { NavigationKey } from '../types/navigation'

interface LayoutState {
  activeNavigation: NavigationKey
  isSidebarCollapsed: boolean
  selectNavigation: (navigation: NavigationKey) => void
  toggleSidebar: () => void
}

export const useLayoutStore = create<LayoutState>((set) => ({
  activeNavigation: 'today',
  isSidebarCollapsed: false,
  selectNavigation: (activeNavigation) => set({ activeNavigation }),
  toggleSidebar: () =>
    set((state) => ({ isSidebarCollapsed: !state.isSidebarCollapsed })),
}))
