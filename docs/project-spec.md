# ilque Project Specification

> 게이미피케이션 기반 개인용 데스크탑 생산성 & 게임 숙제 통합 관리 시스템

## 1. 문서 목적

이 문서는 ilque의 기획, 도메인, 핵심 비즈니스 규칙, 기능 범위 및 기술 방향을 개발 과정에서 참고하기 위한 프로젝트 기준 문서입니다. 본 문서는 설계의 Source of Truth이며, 구현 상세는 본 문서의 설계 결정을 변경하지 않습니다.

세부 구현 규칙은 각 `agent.md`를 따릅니다.

```text
project-spec.md
= 무엇을 만들고 어떤 규칙으로 동작해야 하는가

agent.md
= AI가 위 요구사항을 어떤 개발 규칙으로 구현해야 하는가
```

## 2. 프로젝트 정의

ilque는 단순 Todo 앱이 아니라, 일상 루틴과 게임 숙제를 Quest로 정의하고 실제 수행 이력을 축적하여 EXP, Level, Streak, Achievement, History로 연결하는 개인용 데스크탑 생산성 앱입니다.

### 핵심 가치

- 성취감: EXP / Level / Streak
- 유연한 주기: 생활 및 게임별 Custom Reset
- 몰입도: Focus Timer
- 지속성: 실제 수행 이력과 History
- 확장성: 게임별 전용 테이블 없이 Category + ResetPolicy + Quest로 Game Quest 지원

### 타겟 사용자

일상 루틴, 학습 및 여러 게임의 일일 숙제를 한곳에서 관리하려는 게이머, 학생, 개발자 등 개인 사용자입니다.

## 3. 핵심 Loop

```text
해야 할 일 발견
→ Quest 생성
→ 주기별 QuestExecution 생성
→ 실제 수행
→ 완료
→ EXP / Level / Streak
→ Achievement
→ History / Statistics
→ 다음 주기
```

CalendarEvent는 이 Loop를 대체하지 않고 예정된 일정과 게임 이벤트를 확인하여 Quest 또는 Quick Todo로 연결하는 보조 기능입니다.

## 4. 도메인 구조

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

### 4.1 User와 Identity

ilque MVP는 개인용 데스크탑 앱이며, 회원가입과 로그인 기능을 제공하지 않습니다. 다만 `User`는 사용자 소유 데이터의 기준 Entity로 유지합니다.

- 앱 최초 실행 시 Local User가 없으면 기본 Local User를 생성합니다.
- 이후 생성된 Local User를 현재 사용자로 사용합니다.
- `Quest`, `QuickTodo`, `CalendarEvent`, `UserAchievement`는 모두 User가 소유하는 데이터입니다.

MVP에서는 다음을 구현하지 않습니다.

- 회원가입, 로그인, 비밀번호
- OAuth, JWT
- 멀티 사용자, 서버 계정

이 관계는 향후 서버 기반 계정 및 멀티 디바이스 동기화로 확장할 때에도 유지합니다.

## 5. Quest

Quest는 반복적으로 관리할 가치가 있는 행동의 영구적인 정의입니다.

### 필드

| 필드 | 타입/값 | 설명 |
|---|---|---|
| id | Long | PK |
| title | String | 퀘스트명 |
| description | String | 상세 설명 |
| questType | CHECK / TIME_BASED | 체크형 / 시간형 |
| recurrenceType | ONCE / DAILY / WEEKLY / CUSTOM | 수행 주기 |
| categoryId | Long | Category FK |
| resetPolicyId | Long | ResetPolicy FK |
| difficulty | EASY / NORMAL / HARD / EPIC | 난이도 |
| targetCount | Integer | CHECK Quest의 횟수 목표 |
| targetTimeMinutes | Integer | TIME_BASED Quest의 시간 목표 |
| baseExp | Integer | 기본 EXP |
| expPerMinute | Integer | 분당 추가 EXP |
| active | Boolean | 활성 여부; 삭제 시 Soft Delete |

### 핵심 규칙

- Quest 자체에 완료 상태를 저장하지 않습니다.
- 실제 특정 주기의 수행 상태는 QuestExecution이 담당합니다.
- Quest가 삭제되어도 기존 QuestExecution History는 보존합니다.

### RecurrenceType과 CUSTOM 범위

`RecurrenceType`은 Quest가 어떤 날 또는 주기에 수행 대상이 되는지를 결정합니다. MVP의 `CUSTOM`은 사용자가 선택한 요일에 반복되는 Quest만 의미합니다.

예: 월/수/금, 화/목

MVP의 `CUSTOM`은 요일 선택형 반복으로 제한하며, 다음은 지원하지 않습니다.

- N일마다 반복
- 2주마다 반복
- 매월 특정 날짜 또는 매월 마지막 날
- 복잡한 간격 반복
- 자연어 반복 규칙

## 6. QuestExecution

QuestExecution은 Quest의 특정 수행 주기에 대한 실제 수행 기록입니다.

### 필드

| 필드 | 설명 |
|---|---|
| id | Execution PK |
| questId | 원본 Quest FK |
| periodStart / periodEnd | 수행 주기의 시작/종료 경계 |
| status | PENDING / IN_PROGRESS / COMPLETED / MISSED / SKIPPED / REST_DAY |
| startedAt / completedAt | 실제 시작/완료 시각 |
| targetCount / actualCount | 횟수 목표와 실제 수행 횟수 |
| targetDuration / actualDuration | 시간 목표와 실제 누적 시간(분) |
| earnedExp | 완료 시 확정된 EXP 스냅샷 |

FocusSession은 하나의 Execution에 여러 개 연결될 수 있으며, 분할된 실제 집중 수행 시간을 보존합니다.

## 7. ResetPolicy와 Execution 생성

`RecurrenceType`은 Quest가 언제 수행 대상이 되는지를 결정하고, `ResetPolicy`는 해당 수행 주기의 시작/종료 경계를 결정합니다. 두 책임은 서로 대체하지 않습니다.

- `RecurrenceType`: `ONCE`, `DAILY`, `WEEKLY`, `CUSTOM` 및 `CUSTOM`의 선택 요일에 따라 수행 대상을 판단합니다.
- `ResetPolicy`: 설정된 `resetTime`, `timezone`, `weekStartDay`를 기준으로 Execution의 `periodStart`와 `periodEnd`를 계산합니다.

### ResetPolicy 필드

| 필드 | 설명 |
|---|---|
| id | PK |
| name | 정책 이름 |
| resetType | DAILY / WEEKLY / CUSTOM |
| resetTime | 리셋 기준 시각 |
| timezone | 기준 타임존 |
| weekStartDay | 주간 주기 시작 요일 |
| active | 활성 여부 |

예시:

- 내 생활: 08:00
- 림버스 컴퍼니: 06:00
- 명조: 05:00
- 트릭컬: 04:00

Reset은 Quest 상태를 초기화하는 작업이 아닙니다. 앱 실행 또는 Today 조회 시점에 Next Reset을 계산하고 필요한 새로운 QuestExecution을 생성합니다. 기존 Execution은 History로 보존합니다.

### 장기 미접속

앱이 장기간 종료되어 있었던 경우 개별 MISSED Execution은 최대 7개 기간까지만 생성합니다. 그보다 오래된 미수행분은 `UserStatisticsSummary.longTermMissedCount`에 요약하여 대량의 레코드 생성과 SQLite 쓰기 폭증을 방지합니다.

## 8. EXP 및 Level

### 난이도별 기본값

| 난이도 | 기본 EXP | 시간 비례 EXP | 완수 보너스 |
|---|---:|---:|---:|
| EASY | 10 | +1 / 분 | +20 |
| NORMAL | 20 | +1 / 분 | +30 |
| HARD | 40 | +2 / 분 | +50 |
| EPIC | 80 | +3 / 분 | +100 |

### EXP 공식

```text
earnedExp
= baseExp
+ min(actualMinutes, targetMinutes) × expPerMinute
+ completionBonus
```

목표 시간을 초과한 실제 시간은 History와 Statistics에는 보존하지만 EXP에는 추가 반영하지 않습니다.

### earnedExp Snapshot

완료 시 계산된 `earnedExp`를 QuestExecution에 확정 저장합니다. 이후 Quest의 보상 설정이 변경되어도 과거 Execution의 EXP는 변경하지 않습니다.

### 시간형 Quest 80% 규칙

목표 시간의 80% 미만으로 수행한 경우 Partial EXP는 적립할 수 있지만 기본 상태는 IN_PROGRESS로 유지합니다. 사용자가 명시적으로 부분 완료를 선택할 때만 COMPLETED 분기로 처리합니다.

목표 시간의 80% 이상을 수행하면 실질적인 완료 자격을 부여합니다.

### Level

```text
requiredExp(level) = 100 + ((level - 1) × 50)
```

Level은 생산성의 우열이 아니라 누적 활동량을 시각화하는 지표입니다.

## 9. Streak와 Rest Day

| 상태 | Streak 처리 |
|---|---|
| COMPLETED | Current Streak 증가 |
| MISSED | Current Streak 0으로 단절 |
| SKIPPED | 유지, 증가하지 않음 |
| REST_DAY | 유지, 증가하지 않음 |
| PENDING | 판정 전 |
| IN_PROGRESS | 판정 전 |

`Current Streak`은 최근 연속 완료 구간의 길이이며, `Longest Streak`은 전체 Execution 이력에서의 최대 연속 완료 길이입니다.

Rest Day는 해당 날짜의 Quest를 실패 처리하지 않고 Streak을 보존합니다. EXP는 지급하지 않습니다.

## 10. Achievement

초기 Achievement는 다음을 사용합니다.

| Code | 이름 | 조건 | 보상 / Tier |
|---|---|---|---|
| FIRST_CLEAR | 모험의 시작 | 첫 Quest 완료 | 20 EXP / BRONZE |
| STREAK_3 | 작심삼일 극복 | 3일 연속 | 50 EXP / SILVER |
| STREAK_7 | 습관의 형성 | 7일 연속 | 100 EXP / GOLD |
| FOCUS_10H | 극강의 몰입자 | 누적 Focus 10시간 | 100 EXP / PLATINUM |
| QUEST_100 | 퀘스트 마스터 | 누적 100회 완료 | 150 EXP / DIAMOND |

Achievement는 `Achievement` 정의와 `UserAchievement` 해금 이력을 분리합니다.

완료 흐름은 다음과 같습니다.

```text
QuestExecution COMPLETED
→ EXP / Level
→ Streak
→ Achievement 조건 검사
→ UserAchievement 생성
→ Achievement 보상
```

동일 Achievement의 보상은 중복 지급하지 않습니다.

## 11. Quick Todo

Quick Todo는 이번에만 처리할 단발 작업입니다.

```text
Quick Todo
├── EXP 없음
├── Level 영향 없음
├── Streak 영향 없음
└── QuestExecution 생성 없음
```

완료 시간과 제목을 간단히 보존하며, 당일 화면에서는 완료 상태를 표시하고 최근 30일 범위에서 완료 이력을 조회합니다. Quest History와 혼합하지 않습니다.

## 12. Game Quest

게임 숙제는 특정 게임 전용 테이블을 만들지 않고 다음 구조로 일반화합니다.

```text
Category + ResetPolicy + Quest
```

예를 들어 `원신` Category에 일일 의뢰 Quest를 배치하고 05:00 ResetPolicy를 연결할 수 있습니다.

게임별 일일 리셋 시각이 달라도 동일한 Quest 모델을 사용합니다.

## 13. CalendarEvent

CalendarEvent는 Quest 수행을 보조하기 위한 일정/이벤트 관리 기능입니다. 일반 캘린더 제품으로 확장하는 것이 목적이 아닙니다.

### 책임

- 개인 일정 기록
- 게임 이벤트 기록
- 게임 픽업 일정 기록
- 게임 업데이트/점검 일정 기록
- Upcoming Events 조회
- reminderAt 기반 사전 알림

### 유형

- GENERAL
- GAME_EVENT
- GAME_PICKUP
- GAME_UPDATE

### 필드

| 필드 | 설명 |
|---|---|
| id | PK |
| userId | User FK |
| title | 제목 |
| description | 상세 메모 |
| eventType | 일정 유형 |
| startAt | 시작 일시 |
| endAt | 종료 일시; 선택 |
| allDay | 종일 일정 여부 |
| categoryId | Category FK; 선택 |
| reminderAt | 알림 시각; 선택 |
| active | Soft Delete 상태 |
| createdAt / updatedAt | 생성/수정 시각 |

### 경계 규칙

CalendarEvent는 QuestExecution을 생성하지 않습니다.

CalendarEvent 등록/수정/삭제 자체는 EXP, Level, Streak 계산을 호출하지 않습니다.

예를 들어:

```text
CalendarEvent
원신 신규 이벤트
10/08 ~ 10/29
        │
        ├── 반복 수행 필요 → Game Quest
        └── 일회성 확인 필요 → Quick Todo
```

즉, `Game Event`는 예정 정보를 저장하는 CalendarEvent이고, 실제 반복 행동은 Game Quest로 관리합니다.

## 14. Today Dashboard

앱 실행 시 사용자가 오늘의 상태를 빠르게 파악할 수 있는 Today 중심 RPG Dashboard를 제공합니다.

### Header

- Level
- 현재 / 필요 EXP
- Current Streak
- Next Reset

### Sidebar

- Today
- Calendar
- Weekly
- Game
- Achievement
- History
- Settings

### Today 영역

- 오늘의 Quest Execution
- 완료 / Skip
- Focus 시작
- Quick Todo
- Upcoming Events

### Focus Overlay

하나의 Quest와 타이머에 집중할 수 있도록 불필요한 정보를 최소화합니다.

### History

QuestExecution의 실제 수행 시간, 완료 시각, 확정 earnedExp 및 일/주/월 통계를 표시합니다. Quick Todo의 단순 완료 이력은 별도로 관리합니다.

## 15. 시스템 아키텍처

### Backend 기술 기준

- Java 21
- Spring Boot 4.1.1
- Gradle (Groovy DSL)
- Spring Boot 기반 REST API (API Prefix: `/api`)
- Spring Data JPA / Hibernate
- SQLite

```text
Electron
├── Main Process
│   ├── Spring Boot JAR 실행/관리
│   ├── Window
│   ├── Tray
│   └── Desktop Notification
│
└── Renderer
    └── React + TypeScript
             │
             │ HTTP
             ▼
       Spring Boot 4.1.1 (Java 21)
             │
             ├── Domain Services
             └── JPA / Hibernate
                     │
                     ▼
                   SQLite
```

### Backend Service 책임

- `QuestService`: Quest / Category / ResetPolicy 관리
- `ExecutionService`: Execution 생성, Reset, Lookback, 상태 전이
- `FocusService`: FocusSession 시작/종료 및 시간 집계
- `ExpLevelService`: EXP 계산 및 Level 갱신
- `StreakService`: Current / Longest Streak 계산
- `AchievementService`: Achievement 조건 검사 및 보상
- `StatisticsService`: Today / Week / Month 통계 및 History
- `QuickTodoService`: Quick Todo 및 30일 이력
- `CalendarEventService`: CalendarEvent CRUD 및 조회
- `CalendarReminderService`: reminderAt 기반 알림 대상 계산

### MVP 로컬 실행과 향후 서버 확장

MVP는 Electron 기반 로컬 데스크탑 앱과 SQLite를 기준으로 동작합니다. 인증, 서버 DB, 데이터 동기화 및 충돌 해결은 MVP에 포함하지 않습니다.

향후 Desktop, Mobile, Web Client가 Backend Server를 통해 동일한 User 데이터를 사용할 수 있도록 확장할 가능성은 열어 둡니다. 이때에도 기존 `User → 사용자 소유 데이터` 관계를 최대한 유지하며, 서버 계정, 인증, 동기화, 충돌 해결 및 서버 DB는 별도의 설계 결정으로 다룹니다.

## 16. SQLite 및 트랜잭션 원칙

SQLite는 다음 설정을 사용합니다.

- Journal Mode: WAL
- Synchronous: NORMAL
- Busy Timeout: 5000ms

Execution 생성, Complete, Level 갱신, Achievement 해금 등 핵심 DB 쓰기는 짧은 Transaction으로 처리합니다.

특히 Complete 흐름은 가능한 한 하나의 원자적인 Transaction으로 처리하여 일부만 반영되는 상태를 방지합니다.

### Schema Management

MVP는 SQLite와 JPA/Hibernate를 사용합니다. 초기 개발 단계에서는 JPA/Hibernate Schema Generation을 사용하며 `ddl-auto=update`를 적용합니다.

스키마가 안정화된 뒤에는 `ddl-auto=validate` 전환을 검토합니다. `ddl-auto=update`는 초기 개발 편의를 위한 방식일 뿐 장기적인 migration 전략이 아닙니다.

MVP 초기에는 Flyway 또는 Liquibase를 도입하지 않습니다. 향후 서버 DB를 사용하거나 명시적인 Schema Migration이 필요해지면 Flyway/Liquibase 도입을 별도의 설계 결정으로 검토합니다.

## 17. Complete 멱등성

```text
POST /api/executions/{id}/complete
```

이미 `COMPLETED`인 Execution을 다시 완료 요청하면 기존 `earnedExp`와 상태를 반환하고 다음 작업을 다시 수행하지 않습니다.

- EXP 중복 지급 금지
- Streak 중복 증가 금지
- Achievement 보상 중복 지급 금지

## 18. 주요 API 기준

현재 REST API Prefix는 `/api`를 사용합니다.

### Quest / Reset

```text
GET    /api/quests
POST   /api/quests
GET    /api/quests/{id}
PATCH  /api/quests/{id}
DELETE /api/quests/{id}

GET    /api/reset-policies
POST   /api/reset-policies
PATCH  /api/reset-policies/{id}
```

### Today / Execution / Focus

```text
GET  /api/today
POST /api/quests/{questId}/start
POST /api/executions/{id}/complete
POST /api/executions/{id}/skip
POST /api/quests/{questId}/focus/start
POST /api/focus/{sessionId}/stop
```

### Achievement / Statistics

```text
GET   /api/achievements
GET   /api/achievements/unlocked
PATCH /api/achievements/{id}/equip

GET /api/statistics/today
GET /api/statistics/week
GET /api/statistics/month
```

### CalendarEvent

```text
GET    /api/calendar/events
GET    /api/calendar/events/{id}
POST   /api/calendar/events
PATCH  /api/calendar/events/{id}
DELETE /api/calendar/events/{id}
GET    /api/calendar/upcoming
GET    /api/calendar/date/{date}
```

### 공통 응답 규약

일반 API의 성공/오류 응답은 공통 Envelope를 사용한다. 필드는 `success`, `data`, `error`, `timestamp`로 구성한다.

- 성공 시 `success=true`, `error=null`이다. 데이터가 없는 성공 응답은 `data=null`로 표현한다.
- 오류 시 `success=false`, `data=null`이다. 오류 객체는 `code`, `message`, `details`로 구성한다.
- `details`는 선택적 필드이며, 필드별 유효성 검사 오류가 있을 때만 `field`, `message` 항목 배열을 담는다. 일반 오류의 `details`는 `null`로 설정한다.
- `timestamp`는 ISO 8601 형식의 UTC 시각 문자열이다 (예: `2026-10-06T14:00:00Z`). 숫자형 Unix timestamp로 반환하지 않는다.

성공·오류 여부와 별개로 HTTP 상태 코드는 의미에 맞게 사용한다. 기본 매핑은 다음과 같다.

| HTTP 상태 | 오류 코드 | 용도 |
|---|---|---|
| `400 Bad Request` | `BAD_REQUEST` | 잘못된 요청 값, 잘못된 상태 전이, 완료 조건 미충족 |
| `400 Bad Request` | `VALIDATION_ERROR` | 요청 값 검증 실패 (`details`에 필드별 오류 포함) |
| `404 Not Found` | `RESOURCE_NOT_FOUND` | 존재하지 않는 리소스 |
| `409 Conflict` | `RESOURCE_CONFLICT` | 중복/충돌 |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | 예상하지 못한 서버 내부 오류 |

필요한 추가 오류 코드는 실제 도메인 요구사항이 생길 때 확장한다.

추가 규칙:

- Envelope를 반환하는 빈 작업 응답은 기본적으로 `200 OK`를 사용한다. `204 No Content`는 응답 본문이 없어야 하므로 기본 응답 규약에서는 사용하지 않는다.
- 예상하지 못한 서버 오류의 Stack Trace, SQL, 내부 경로는 클라이언트에 노출하지 않는다.
- 인증·인가는 MVP 범위가 아니므로 인증 관련 설계를 새로 추가하지 않는다.
- 페이지네이션은 실제 API 요구사항이 정해지기 전까지 공통 응답 구조에 포함하지 않는다.

구체적인 Request / Response DTO 계약은 실제 API 구현 단계에서 별도의 API 문서로 분리할 수 있다.

## 19. MVP 범위

### MVP 포함

- Quest CRUD
- ONCE / DAILY / WEEKLY / CUSTOM
- Category
- Custom Reset
- QuestExecution
- CHECK / TIME_BASED Quest
- EXP
- Level
- Current / Longest Streak
- Rest Day
- Focus Timer
- Today Dashboard
- History
- Quick Todo 생성/체크 및 30일 이력
- CalendarEvent CRUD
- Calendar 기간 조회
- Upcoming Events
- 기본 reminderAt
- Dashboard Upcoming Events

### Post-MVP

핵심 Loop 안정화 이후 다음 기능을 확장합니다.

- Achievement / Badge 고도화
- 게임 Quest 프리셋
- 고급 통계 차트
- Tray 알림 고도화
- 사운드 / 애니메이션
- 캐릭터 / 장비 커스터마이징
- 외부 캘린더 연동
- 게임 API 기반 이벤트 자동 수집

### 제외 범위

핵심 생산성 Loop를 대체하거나 복잡한 게임 경제를 요구하는 기능은 MVP에서 제외합니다.

- Gold
- Shop
- Battle
- Map
- 캐릭터 성장 트리
- 장비 파밍
- 전투/탐험 중심 시스템
- Google Calendar 등 외부 캘린더 동기화
- 일정 공유 / 참석자
- 복잡한 반복 일정
- 회의/예약/초대 중심 캘린더

## 20. 핵심 설계 원칙

### 20.1 Quest와 수행 기록의 분리

`Quest`는 정의이고 `QuestExecution`은 수행 기록입니다.

### 20.2 Reset과 상태 초기화의 분리

Reset은 데이터를 초기화하지 않고 새로운 Execution을 생성합니다.

### 20.3 정보와 행동의 분리

CalendarEvent는 예정 정보를, Quest/Quick Todo는 실제 행동을 담당합니다.

### 20.4 보상과 실제 시간의 분리

실제 수행 시간은 통계적으로 보존하지만 EXP는 정의된 산정 공식과 목표 시간에 따라 계산합니다.

### 20.5 실패보다 지속성 우선

MISSED, SKIPPED, REST_DAY를 구분하여 실패를 단순 처벌로 연결하지 않습니다.

### 20.6 RPG는 생산성 기능의 피드백 계층

RPG 요소는 사용자의 행동을 대체하는 것이 아니라 완료 결과에 성취감을 제공하는 역할을 합니다.

## 21. 개발 순서

```text
Foundation
→ Quest Core
→ QuestExecution / Reset
→ EXP / Level
→ Streak / Rest Day
→ Focus Session
→ Achievement
→ Today Dashboard
→ History / Statistics
→ Game Quest
→ CalendarEvent / Upcoming / Reminder
→ Electron Tray / Notification
```

각 단계의 구현 규칙과 작업 방식은 Root / Frontend / Backend `agent.md`에서 정의합니다.
