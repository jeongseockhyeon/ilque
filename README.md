# ilque

> 게이미피케이션 기반 개인용 데스크탑 생산성 & 게임 숙제 통합 관리 시스템

**ilque(일퀘)**는 일상 루틴과 게임 숙제를 RPG의 Quest처럼 관리하고, 실제 수행 이력을 `QuestExecution`으로 축적하여 EXP, Level, Streak, Achievement, History로 연결하는 개인용 데스크탑 생산성 앱입니다.

## 1. 프로젝트 소개

일반적인 Todo 앱은 할 일을 완료하는 것에서 끝나기 쉽습니다. ilque는 완료 결과를 지속적인 성장 피드백으로 연결하여 사용자가 반복적인 일상 루틴과 게임 숙제를 꾸준히 수행할 수 있도록 설계합니다.

핵심은 단순한 RPG형 Todo가 아니라 다음 요소의 결합입니다.

- Quest와 QuestExecution의 분리
- 게임 및 생활에 맞춘 Custom Reset
- 수행 시간 측정을 위한 Focus Session
- EXP / Level / Streak 기반 성장 피드백
- Achievement / Badge
- 게임 숙제를 위한 Category + ResetPolicy 구조
- 예정된 게임 이벤트와 개인 일정을 관리하는 CalendarEvent
- Today 중심의 데스크탑 Dashboard

## 2. 핵심 Loop

```text
해야 할 일 발견
    ↓
Quest 생성
    ↓
주기별 QuestExecution 생성
    ↓
실제 수행
    ↓
완료
    ↓
EXP / Level / Streak
    ↓
Achievement
    ↓
History / Statistics
    ↓
다음 주기
```

CalendarEvent는 위 핵심 Loop를 대체하지 않고, 예정된 일정과 게임 이벤트를 미리 확인하여 Quest 또는 Quick Todo로 연결할 수 있도록 보조합니다.

## 3. 주요 기능

### Quest

반복적으로 관리할 가치가 있는 행동의 영구적인 정의입니다.

- CHECK / TIME_BASED
- ONCE / DAILY / WEEKLY / CUSTOM
- Category
- ResetPolicy
- EASY / NORMAL / HARD / EPIC
- 횟수 또는 시간 목표
- 난이도 기반 EXP
- Soft Delete

### QuestExecution

Quest의 특정 주기에서 실제 수행한 기록입니다.

- PENDING
- IN_PROGRESS
- COMPLETED
- MISSED
- SKIPPED
- REST_DAY

Quest는 수행 상태를 직접 저장하지 않으며, 실제 수행 이력은 QuestExecution에 보존합니다.

### Quick Todo

이번에만 처리할 단발성 할 일입니다.

- EXP 없음
- Streak 영향 없음
- QuestExecution 생성 없음
- 완료 이력은 최근 30일 범위에서 관리

### Game Quest

게임별 전용 테이블을 만들지 않고 다음 조합으로 구현합니다.

```text
Category + ResetPolicy + Quest
```

예를 들어 게임별 일일 리셋 시각이 다른 경우 각 게임에 적절한 ResetPolicy를 연결합니다.

### CalendarEvent

게임 이벤트, 픽업, 업데이트, 개인 일정 등 미래의 예정 정보를 관리합니다.

- GENERAL
- GAME_EVENT
- GAME_PICKUP
- GAME_UPDATE
- 시작/종료 일시
- 종일 일정
- Upcoming Events
- reminderAt 기반 알림

CalendarEvent 자체는 EXP, Level, Streak에 영향을 주지 않으며 QuestExecution을 생성하지 않습니다.

## 4. 기술 스택

### Frontend / Desktop

- React
- TypeScript
- Tailwind CSS
- Electron
- Vite
- TanStack Query
- Zustand
- Lucide React
- Framer Motion

### Backend

- Java 21
- Spring Boot 3.x
- Spring Data JPA / Hibernate
- Gradle

### Database

- SQLite
- WAL

## 5. 시스템 구조

```text
┌───────────────────────────────┐
│          Electron             │
│                               │
│  Main Process    Renderer     │
│  - Spring Boot  - React       │
│  - Tray         - Dashboard   │
│  - Notification  - Quest UI   │
└──────────────┬────────────────┘
               │ HTTP
               ▼
┌───────────────────────────────┐
│        Spring Boot API        │
│                               │
│ Quest / Execution / Focus     │
│ EXP / Level / Streak          │
│ Achievement / Statistics      │
│ QuickTodo / CalendarEvent     │
└──────────────┬────────────────┘
               │ JPA
               ▼
┌───────────────────────────────┐
│            SQLite             │
└───────────────────────────────┘
```

Electron Main Process는 Spring Boot JAR의 실행 및 데스크탑 기능을 담당하고, React Renderer는 UI와 API 통신을 담당합니다. Spring Boot는 핵심 도메인 로직과 트랜잭션을 담당합니다.

## 6. 핵심 도메인 관계

```text
User
├── Quest
│   └── QuestExecution
│       └── FocusSession
├── QuickTodo
├── CalendarEvent
└── UserAchievement
    └── Achievement

Category ──< Quest
ResetPolicy ──< Quest
Category ──< CalendarEvent (선택)
```


## 7. 프로젝트 문서

| 문서 | 설명 |
|---|---|
| [`agent.md`](./agent.md) | 전체 프로젝트 AI 개발 규칙 |
| [`ilque-fe/agent.md`](./ilque-fe/agent.md) | Frontend AI 개발 규칙 |
| [`ilque-be/agent.md`](./ilque-be/agent.md) | Backend AI 개발 규칙 |
| [`docs/project-spec.md`](./docs/project-spec.md) | 프로젝트 기획 및 개발 기준 |


