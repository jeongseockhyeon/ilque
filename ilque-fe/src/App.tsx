import { Sidebar } from './components/layout/Sidebar'
import { DashboardPlaceholder } from './components/features/dashboard/DashboardPlaceholder'
import { Header } from './components/layout/Header'
import { navigationItems } from './types/navigation'
import { useLayoutStore } from './store/layoutStore'
import './App.css'

function App() {
  const activeNavigation = useLayoutStore((state) => state.activeNavigation)
  const activePage = navigationItems.find(({ key }) => key === activeNavigation) ?? navigationItems[0]

  return (
    <div className="app-shell">
      <Sidebar />
      <main className="app-shell__content" aria-label="Ilque 콘텐츠 영역">
        <Header />
        <div className="app-shell__body">
          <DashboardPlaceholder activePage={activePage} />
        </div>
      </main>
    </div>
  )
}

export default App
