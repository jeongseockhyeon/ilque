# 프로젝트 규칙 및 AI 에이전트 지침 (Root Agent Rules)

## 1. 프로젝트 개요 (Project Overview)

-   **프로젝트명**: 일퀘 (ilque)
-   **개요**: 게이머와 개발자를 위한 게이미피케이션(Gamification) 기반
    개인용 데스크톱 생산성 & 일일 숙제 통합 관리 시스템
-   **기술 스택**: React + TypeScript + Tailwind CSS + Electron / Java
    21 + Spring Boot 3.x / SQLite
-   **MVP 실행 구조**: `Electron Desktop → React Renderer → HTTP → Spring
    Boot 3.x (Java 21) → JPA/Hibernate → SQLite`
-   **MVP 운영 범위**: 로컬 단일 사용자 데스크톱 환경을 기준으로 합니다.
    서버 배포, 모바일 앱, 다중 디바이스 동기화는 MVP 범위가 아닙니다.
-   **핵심 가치**:
    -   EXP, Level, Streak 기반 동기부여
    -   게임별/그룹별 유연한 Reset Time
    -   반복 숙제와 실제 수행 기록의 분리
    -   Focus Session 및 오늘 중심 대시보드
    -   게임 일정 및 일반 일정을 보조하는 Calendar Event

------------------------------------------------------------------------

## 2. 전체 프로젝트 디렉터리 구조

``` text
ilque/
├── agent.md
├── ilque-fe/
│   └── agent.md
└── ilque-be/
    └── agent.md
```

-   `ilque-fe/` 작업은 `ilque-fe/agent.md`를 우선 준수합니다.
-   `ilque-be/` 작업은 `ilque-be/agent.md`를 우선 준수합니다.
-   전체 시스템 구조, 도메인 용어, 비즈니스 규칙, API 계약의 Source of
    Truth는 `docs/project-spec.md`입니다. 본 문서는 그 설계 결정을 AI 작업
    규칙에 반영합니다.
-   하위 Agent가 `project-spec.md` 또는 Root의 공통 규칙과 충돌하는 구현을
    제안하면 임의로 구현하지 않고 사용자에게 알립니다.
-   Root Agent는 프로젝트 전반에 공통으로 적용할 구현 규칙을, FE/BE
    Agent는 각 영역의 구체적인 구현 방식을 주로 정의합니다.

------------------------------------------------------------------------

## 3. 핵심 도메인 및 비즈니스 규칙

### User / Identity

ilque MVP는 개인용 데스크톱 애플리케이션이며 회원가입 또는 로그인
시스템을 구현하지 않습니다. `User` 도메인은 데이터 소유권의 기준으로
유지합니다.

-   앱 최초 실행 시 Local User가 없으면 기본 Local User를 생성하고, 이후
    해당 Local User를 현재 사용자로 사용합니다.
-   `Quest`, `QuickTodo`, `CalendarEvent`, `UserAchievement`는 모두 User가
    소유하며, `QuestExecution`과 `FocusSession`은 Quest 계층을 통해 User의
    수행 기록에 속합니다.
-   MVP에서는 회원가입, 로그인, 비밀번호, OAuth, JWT, 다중 사용자 계정,
    서버 계정 및 사용자 인증/인가는 구현하지 않습니다.
-   User 소유 관계는 향후 서버 기반 계정, 서버 DB, 모바일/데스크톱
    동기화로 확장할 때에도 재사용할 수 있도록 유지합니다. 이러한 확장
    기능 자체는 현재 MVP 범위가 아닙니다.

### 3.1 Quest

Quest는 사용자가 수행해야 할 **퀘스트 정의(Definition)**입니다. 특정
기간의 수행 상태를 저장하는 객체가 아닙니다.

`questType`과 `recurrenceType`은 서로 다른 개념입니다.

-   `questType`: `CHECK`, `TIME_BASED`
-   `recurrenceType`: `ONCE`(1회성), `DAILY`(매일), `WEEKLY`(매주),
    `CUSTOM`(사용자가 지정한 요일)

`RecurrenceType`은 Quest가 **언제 수행 대상이 되는지**를 결정합니다.
MVP의 `CUSTOM`은 월/수/금처럼 사용자가 선택한 요일에 반복하는 경우로
한정합니다. N일마다, 2주마다, 월간 반복, 특정 월의 마지막 날 및 그 밖의
복잡한 캘린더 기반 반복 규칙은 MVP에서 구현하지 않습니다.

주요 필드: - `categoryId`, `resetPolicyId` - `difficulty`: `EASY`,
`NORMAL`, `HARD`, `EPIC` - `targetCount`, `targetTimeMinutes` -
`baseExp`, `expPerMinute` - `active`

**Quest에 특정 기간의 `completed`, `missed` 등의 수행 상태를 직접
저장하지 않습니다.**

### 3.2 QuestExecution

Quest의 **특정 기간에 대한 실제 수행 기록**입니다.

``` text
Quest
  └── QuestExecution
        └── FocusSession
```

상태: - `PENDING` - `IN_PROGRESS` - `COMPLETED` - `MISSED` - `SKIPPED` -
`REST_DAY`

주요 필드: - `questId`, `periodStart`, `periodEnd`, `status` -
`startedAt`, `completedAt` - `targetCount`, `actualCount` -
`targetDuration`, `actualDuration` - `earnedExp`

불변조건: 1. Quest와 QuestExecution을 혼합하지 않습니다. 2. 과거
QuestExecution은 임의로 덮어쓰거나 삭제하지 않습니다. 3. 새 기간이
시작되면 기존 Execution을 초기화하지 않고 새로운 Execution을 생성합니다.
4. 특정 기간의 완료 여부는 QuestExecution을 기준으로 판단합니다.

### 3.3 FocusSession

특정 QuestExecution의 집중 수행 기록입니다.

``` text
QuestExecution 1 ─── N FocusSession
```

FocusSession의 시간 기록과 QuestExecution의 실제 수행 시간은 구분할 수
있어야 합니다.

### 3.4 Quick Todo

오늘 하루 간단히 처리하는 **일회성 할 일**입니다.

Quick Todo는 Quest와 별도이며 다음에 영향을 주지 않습니다.

-   EXP
-   Level
-   Streak
-   QuestExecution 생성

완료 이력은 최근 30일 범위에서 관리합니다.

### 3.5 ResetPolicy

`ResetPolicy`는 QuestExecution의 **기간 경계가 언제 시작되고 종료되는지**를
결정하는 리셋 정책입니다. `RecurrenceType`이 수행 대상 시점을 정하고,
`ResetPolicy`가 기간 경계를 정하므로 두 책임을 혼동하거나 서로 대체하지
않습니다.

**Reset은 기존 상태를 초기화하는 동작이 아니라 새로운 기간의
QuestExecution을 생성하는 기준입니다.**

주요 개념: - `resetType` - `resetTime` - `timezone` - `weekStartDay` -
`active`

예: 게임 A 05:00 / 게임 B 04:00 / 일반 일일 퀘스트 00:00

리셋 시각은 UTC 절대 시각이 아니라 해당 ResetPolicy의 설정을 기준으로
계산합니다.

예를 들어 `RecurrenceType = DAILY`, `ResetPolicy = 매일 05:00`이면 매일
05:00을 기준으로 새 Execution 기간이 생성됩니다. ResetPolicy를 변경해도
이미 생성된 과거 QuestExecution의 기간 경계나 기록은 변경하지 않습니다.

### 3.6 Streak / Rest Day

Streak은 QuestExecution 결과를 기준으로 판단합니다.

  상태            처리
  --------------- --------------
  `COMPLETED`     증가
  `MISSED`        0으로 초기화
  `SKIPPED`       유지
  `REST_DAY`      유지
  `PENDING`       미결정
  `IN_PROGRESS`   미결정

Rest Day는 Streak을 유지하지만 EXP를 지급하거나 Streak을 증가시키지
않습니다.

### 3.7 Achievement

초기 업적: - `FIRST_CLEAR` - `STREAK_3` - `STREAK_7` - `FOCUS_10H` -
`QUEST_100`

### 3.8 CalendarEvent

CalendarEvent는 **Quest 수행을 보조하는 일정/이벤트 정보**이며 일반
캘린더 제품을 만드는 목적이 아닙니다.

이벤트 타입: - `GENERAL` - `GAME_EVENT` - `GAME_PICKUP` - `GAME_UPDATE`

CalendarEvent는 다음에 영향을 주지 않습니다. - EXP - Level - Streak -
QuestExecution 생성

사용자가 이벤트를 참고해 별도의 Quest 또는 Quick Todo를 생성하는 것은
가능합니다.

------------------------------------------------------------------------

## 4. 핵심 도메인 관계

``` text
User
├── Quest
│   └── QuestExecution
│       └── FocusSession
├── QuickTodo
├── CalendarEvent
└── UserAchievement
    └── Achievement

Category
├── Quest
└── CalendarEvent (optional)

ResetPolicy
└── Quest
```

게임 Quest도 별도 `GameQuest` 테이블을 만들지 않고 기본적으로 다음
조합으로 표현합니다.

``` text
Category + ResetPolicy + Quest
```

------------------------------------------------------------------------

## 5. Reset 및 이력 보존 규칙

리셋은 상태 초기화가 아닙니다.

``` text
기존 Quest
    ↓
새로운 기간 도래
    ↓
새 QuestExecution 생성
    ↓
새 기간 수행
```

예:

``` text
Quest: 운동하기

2026-10-05
└── Execution #101 → COMPLETED

2026-10-06
└── Execution #102 → PENDING
```

기존 Execution #101의 이력은 보존합니다.

-   Lookback은 최근 최대 7개 기간을 사용합니다.
-   오래된 MISSED 기록은 개별 Execution을 무한정 조회하지 않습니다.
-   장기 통계는 `UserStatisticsSummary.longTermMissedCount` 등의 집계
    데이터를 활용합니다.

------------------------------------------------------------------------

## 6. EXP / Level / Streak 규칙

### 6.1 난이도별 EXP

  Difficulty     Base EXP   EXP / min   Completion Bonus
  ------------ ---------- ----------- ------------------
  EASY                 10           1                 20
  NORMAL               20           1                 30
  HARD                 40           2                 50
  EPIC                 80           3                100

### 6.2 EXP 계산

``` text
earnedExp
= baseExp
+ min(actualMinutes, targetMinutes) × expPerMinute
+ completionBonus
```

-   목표 시간을 초과한 실제 수행 시간은 이력/통계에는 보존할 수
    있습니다.
-   초과 시간은 EXP 계산에 추가 반영하지 않습니다.
-   `earnedExp`는 완료 시점에 계산하고 **스냅샷으로 고정**합니다.
-   이후 설정 변경으로 과거 완료 Execution의 `earnedExp`를 변경하지
    않습니다.

### 6.3 Time-based Quest 80% 규칙

-   목표 시간 80% 미만: 부분 EXP 지급 가능, 기본적으로 `IN_PROGRESS`
    유지
-   목표 시간 80% 이상: `COMPLETED` 조건 충족
-   사용자가 명시적으로 부분 완료를 선택한 경우 별도 정책에 따라 처리

### 6.4 Level

``` text
requiredExp(level)
= 100 + ((level - 1) × 50)
```

EXP 및 Level 계산은 백엔드에서 검증합니다.

------------------------------------------------------------------------

## 7. Complete 처리의 원자성 및 멱등성

완료 처리는 다음 성장 루프를 일관되게 처리해야 합니다.

``` text
QuestExecution → COMPLETED
        ↓
earnedExp 확정
        ↓
EXP 반영
        ↓
Level 계산
        ↓
Streak 반영
        ↓
Achievement 조건 확인
        ↓
UserAchievement / 보상 반영
```

### 원자성

EXP/Level/Streak/Achievement에 영향을 주는 완료 처리는 하나의 트랜잭션
단위에서 일관되게 처리합니다.

### 멱등성

이미 `COMPLETED`인 Execution에 Complete 요청이 다시 들어오면: - EXP
재지급 금지 - Level 재반영 금지 - Streak 재증가 금지 - Achievement 보상
중복 지급 금지 - 기존 `earnedExp`와 완료 상태 반환

------------------------------------------------------------------------

## 8. Quick Todo / CalendarEvent 경계

### Quick Todo

``` text
QuickTodo 완료
    └── 완료 이력 저장
```

금지:

``` text
QuickTodo → QuestExecution 생성
QuickTodo → EXP 증가
QuickTodo → Streak 변경
```

### CalendarEvent

``` text
CalendarEvent
    ├── Upcoming / Calendar 조회
    └── Reminder 대상 판단
```

금지:

``` text
CalendarEvent → QuestExecution 생성
CalendarEvent → EXP 증가
CalendarEvent → Streak 변경
```

------------------------------------------------------------------------

## 9. API 및 데이터 통신 규격

### 9.1 API Prefix

현재 REST API Prefix는 **`/api`**입니다. `/api/v1`은 사용하지 않으며,
MVP 범위에서는 API 버전 관리 방식을 도입하지 않습니다.

### 9.2 REST 규칙

-   소문자 + kebab-case
-   복수형 명사
-   예: `/api/quests`, `/api/calendar/events`, `/api/achievements`

### 9.3 공통 Response Envelope

아래 형식은 구현 단계의 공통 API 규격입니다.

``` json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-10-06T14:00:00Z"
}
```

오류:

``` json
{
  "success": false,
  "data": null,
  "error": {
    "code": "QUEST_NOT_FOUND",
    "message": "해당 퀘스트를 찾을 수 없습니다."
  },
  "timestamp": "2026-10-06T14:00:00Z"
}
```

### 9.4 API 변경 원칙

-   FE/BE가 공유하는 API 계약을 임의로 변경하지 않습니다.
-   DTO/응답 변경 시 영향받는 FE/BE 파일과 변경 이유를 먼저 확인합니다.
-   `project-spec.md`와 Agent 규칙이 충돌하면 `project-spec.md`를 설계의
    Source of Truth로 따르며, Agent 규칙 정합화 또는 구현 범위에 판단이
    필요하면 사용자에게 알리고 임의로 결정하지 않습니다.

------------------------------------------------------------------------

## 10. SQLite / Transaction 규칙

SQLite 설정 방향: - WAL - `synchronous=NORMAL` - `busy_timeout=5000ms`

원칙: - Complete의 성장 연산은 원자적으로 처리합니다. - 트랜잭션은 짧게
유지합니다. - 중복 Complete로 보상이 중복 지급되지 않아야 합니다. - 과거
QuestExecution 이력을 임의로 수정/삭제하지 않습니다.

구체적인 JPA/Repository 구현은 `ilque-be/agent.md`에서 정의합니다.

### 10.1 Schema 관리

MVP는 SQLite와 JPA/Hibernate로 스키마를 관리합니다. 초기 개발 단계에서는
`ddl-auto=update`를 허용합니다.

-   `ddl-auto=update`는 초기 개발 편의를 위한 설정이며, 장기 운영 환경의
    마이그레이션 전략으로 간주하지 않습니다.
-   MVP 초기에는 Flyway 또는 Liquibase를 도입하지 않습니다.
-   스키마가 안정화된 뒤에는 `ddl-auto=validate` 등의 방식으로 전환하는
    것을 검토할 수 있습니다.

------------------------------------------------------------------------

## 11. FE / BE 책임 분리

### Root Agent

-   도메인 의미와 관계
-   비즈니스 규칙
-   상태 전이
-   EXP / Level / Streak
-   Reset
-   API 계약
-   FE/BE 데이터 경계
-   전체 아키텍처 원칙

### Frontend Agent

-   React 컴포넌트
-   TypeScript 타입/API Client
-   상태관리
-   UI/UX
-   Tailwind CSS
-   Electron Renderer

### Backend Agent

-   Spring Boot 패키지
-   Controller / Service / Repository
-   Entity / DTO / Mapper
-   JPA / SQLite
-   Transaction / Validation / Exception
-   테스트

------------------------------------------------------------------------

## 12. Git 커밋 컨벤션

-   `feat`: 기능 추가
-   `fix`: 버그 수정
-   `refactor`: 기능 변경 없는 리팩토링
-   `docs`: 문서 수정
-   `style`: 포맷팅 등
-   `test`: 테스트 추가/수정

하나의 커밋은 하나의 논리적인 변경 단위를 갖도록 합니다.

------------------------------------------------------------------------

## 13. AI Agent 행동 지침

### 작업 전

1.  작업 대상 폴더의 `agent.md`를 읽습니다.
2.  기존 코드 구조를 확인합니다.
3.  변경의 도메인/API/파일 영향 범위를 파악합니다.
4.  기획서와 기존 구현의 충돌을 확인합니다.

### 구현 중

1.  전체 재작성보다 점진적 변경을 우선합니다.
2.  명시적 타입을 사용하고 불필요한 `any`를 사용하지 않습니다.
3.  Entity/API/DB 구조 변경 시 이유와 영향을 확인합니다.
4.  새 라이브러리 추가 전 기존 의존성을 확인합니다.
5.  기획서에 없는 기능을 임의로 추가하지 않습니다.
6.  도메인 불변조건을 위반하는 구현을 하지 않습니다.
7.  FE/BE DTO 또는 API 변경 시 영향 파일을 먼저 식별합니다.
8.  과거 이력이나 성장 보상이 중복 생성되지 않도록 합니다.

### 금지되는 구현

-   Quest에 기간별 완료 상태 직접 저장
-   Reset 시 기존 QuestExecution 초기화/삭제
-   QuestExecution 이력 임의 덮어쓰기
-   Quick Todo에 EXP/Streak 연결
-   CalendarEvent에 EXP/Streak 연결
-   CalendarEvent가 자동으로 QuestExecution 생성
-   Complete 재요청에 의한 중복 보상
-   불필요한 GameQuest 도메인 생성
-   기획서에 없는 대규모 RPG 시스템 임의 추가

### 작업 후

다음을 요약합니다. 1. 변경 파일 2. 주요 변경 내용 3. 도메인/API 영향 4.
빌드/테스트 결과 5. 미해결 문제 6. 사용자 결정 필요 사항

------------------------------------------------------------------------

## 14. 개발 우선순위

``` text
1. Root agent.md 확정
2. ilque-fe/agent.md 작성
3. ilque-be/agent.md 작성
4. 프로젝트 디렉터리/패키지 구조 확정
5. DB Schema 및 Entity 관계 확정
6. API Contract 확정
7. Foundation
8. Quest Core
9. QuestExecution
10. EXP / Level
11. Streak / Rest Day
12. Focus Session
13. Achievement
14. Today Dashboard
15. History / Statistics
16. Game Quest
17. Electron Tray / Notification
```

MVP를 우선 구현합니다. 고급 Achievement, 고급 통계 차트, 게임별 프리셋,
트레이 알림/사운드/애니메이션, 캐릭터/장비 등은 핵심 루프 안정화 이후
확장합니다.

------------------------------------------------------------------------

## 15. AI Agent 최우선 원칙

> **Quest는 정의이고, QuestExecution은 수행 기록이다.**
>
> **Reset은 상태 초기화가 아니라 새로운 수행 기간의 Execution
> 생성이다.**
>
> **완료 보상은 한 번만 반영되어야 하며, 과거 수행 이력은 보존되어야
> 한다.**
>
> **Quick Todo와 CalendarEvent는 Quest 성장 루프와 분리한다.**

Root Agent와 `project-spec.md`가 충돌하면 `project-spec.md`를 설계의 Source
of Truth로 따릅니다. 구현 범위에 추가 판단이 필요한 경우에는 AI Agent가
임의로 코드를 수정하지 않고 충돌 내용을 사용자에게 명확히 알린 후 결정을
기다립니다.
