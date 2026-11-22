import { Flame, Gauge, RotateCcw } from 'lucide-react'

import './Header.css'

export function Header() {
  return (
    <header className="app-header">
      <div className="app-header__title-group">
        <span className="app-header__eyebrow">ADVENTURER STATUS</span>
        <h1>Today Dashboard</h1>
      </div>

      <div className="app-header__stats" aria-label="모험가 상태 요약">
        <div className="app-header__stat">
          <Gauge aria-hidden="true" size={18} />
          <span className="app-header__stat-label">Level</span>
          <strong>--</strong>
        </div>
        <div className="app-header__stat">
          <span className="app-header__stat-icon app-header__stat-icon--accent" aria-hidden="true">
            ✦
          </span>
          <span className="app-header__stat-label">EXP</span>
          <strong>-- / --</strong>
        </div>
        <div className="app-header__stat">
          <Flame aria-hidden="true" size={18} />
          <span className="app-header__stat-label">Streak</span>
          <strong>--</strong>
        </div>
        <div className="app-header__stat">
          <RotateCcw aria-hidden="true" size={17} />
          <span className="app-header__stat-label">Next Reset</span>
          <strong>연동 예정</strong>
        </div>
      </div>
    </header>
  )
}
