import { Sidebar } from './components/layout/Sidebar'
import './App.css'

function App() {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="app-shell__preview" aria-label="콘텐츠 영역">
        Sidebar 미리보기 영역
      </main>
    </div>
  )
}

export default App
