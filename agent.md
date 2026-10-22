# 프로젝트 규칙 및 AI 에이전트 지침 (Root Agent Rules)

## 언어 및 응답 규칙

* 사용자와의 모든 대화 및 응답은 한국어로 작성한다.
* 작업 계획, 진행 상황, 작업 결과, 오류 설명, 추가 확인 요청도 한국어로 작성한다.
* 코드 수정 및 구현 결과를 설명할 때도 한국어를 사용한다.
* 문서, README, 주석, 커밋 메시지 등 프로젝트 산출물은 별도의 명시적 요구가 없는 한 한국어로 작성한다.
* 기술 용어, 라이브러리명, API 경로, 클래스명, 메서드명, 변수명 등은 정확성을 위해 원문을 유지할 수 있다.
* 코드 식별자와 프레임워크의 필수 규칙은 임의로 번역하거나 변경하지 않는다.
* 영어로 된 오류 메시지나 로그를 인용할 경우 원문을 유지하되, 설명은 한국어로 작성한다.
* 사용자가 다른 언어로 답변하거나 산출물을 작성하도록 명시적으로 요청한 경우에만 해당 요청을 따른다.
* 이 규칙은 모든 작업 세션과 작업 유형에 공통으로 적용한다.


## 1. 프로젝트 개요 (Project Overview)

-   **프로젝트명**: 일퀘 (ilque)
-   **개요**: 게이머와 개발자를 위한 게이미피케이션(Gamification) 기반
    개인용 데스크톱 생산성 & 일일 숙제 통합 관리 시스템
-   **기술 스택**: React + TypeScript + Tailwind CSS + Electron / Java
    21 + Spring Boot 4.1.1 + Gradle (Groovy DSL) / Spring Data JPA + Hibernate /
    SQLite
-   **MVP 실행 구조**: `Electron Desktop → React Renderer → HTTP → Spring
    Boot 4.1.1 (Java 21) → JPA/Hibernate → SQLite`
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
-   작업 전 `docs/project-spec.md`와 작업 대상 하위 디렉터리의 `agent.md`를
    확인합니다. ResetPolicy 백엔드 작업은 특히 `ilque-be/agent.md`의 상세
    구현 기준을 함께 확인합니다.
-   하위 Agent가 `project-spec.md` 또는 Root의 공통 규칙과 충돌하는 구현을
    제안하면 임의로 구현하지 않고 사용자에게 알립니다.
-   문서 간 충돌을 발견하면 임의로 한쪽을 선택해 구현하지 않고, 충돌 항목과
    영향 범위를 보고합니다. 확정 요구사항, 제안 사항, 미결정 사항을
    구분해서 기록합니다.
-   루트 공통 지침 문서의 실제 파일명은 `agent.md`입니다. 백엔드 전용 지침
    문서는 `ilque-be/agent.md`이며 두 문서를 혼동하지 않습니다.
-   도메인 식별자 필드는 해당 도메인의 lower camel case 이름에 `Id`를 붙인
    `{domain}Id` 형식을 사용합니다. 예: `userId`, `categoryId`,
    `resetPolicyId`, `questId`.
-   사용자가 수정 대상으로 지정하지 않은 문서와 소스는 직접 변경하지
    않습니다.
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

주요 필드: - `questId`, `categoryId`, `resetPolicyId` - `difficulty`: `EASY`,
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

ResetPolicy 작업 시 다음 공통 원칙을 적용합니다.

- Quest의 반복 유형(`RecurrenceType`)과 ResetPolicy의 초기화 경계 유형은 서로 다른 개념입니다.
- Category와 ResetPolicy는 독립적인 도메인이며, Quest가 각각을 참조하는 구조를 전제로 합니다. 둘 사이에 직접적인 소유 관계를 만들지 않습니다.
- `DAILY`, `WEEKLY`, `CUSTOM`의 세부 규칙과 `timezone`·DST 처리는 `docs/project-spec.md` 및 `ilque-be/agent.md`의 정의를 따릅니다.
- 날짜 및 주 간격은 달력상의 간격으로 계산하며 고정된 초 단위 간격으로 대체하지 않습니다.
- Quest별 초기화 상태와 `lastProcessedResetAt`은 정책 자체가 공유하는 전역 상태와 혼동하지 않습니다.
- 현재 Quest 도메인이 구현되지 않은 동안에는 ResetPolicy 자체의 모델, 입력 검증, 경계 계산 및 단위 테스트를 우선하며, Quest 연동은 후속 요구사항으로 분리합니다.

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

일반 API의 성공/오류 응답은 다음 공통 Envelope를 사용한다. 필드는 `success`, `data`, `error`, `timestamp`로 구성한다.

-   성공 시 `success=true`, `error=null`이다. 데이터가 없는 성공 응답은 `data=null`로 표현한다.
-   오류 시 `success=false`, `data=null`이다. 오류 객체는 `code`, `message`, `details`로 구성한다.
-   `details`는 선택적 필드이며, 필드별 유효성 검사 오류가 있을 때만 항목 배열을 담는다. 항목은 `field`, `message`로 구성한다. 일반 오류의 `details`는 `null`로 설정한다.
-   `timestamp`는 ISO 8601 형식의 UTC 시각 문자열이다 (예: `2026-10-06T14:00:00Z`). 숫자형 Unix timestamp로 반환하지 않는다.

성공:

``` json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-10-06T14:00:00Z"
}
```

데이터가 없는 성공 응답은 `data`를 `null`로 표현한다.

일반 오류:

``` json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "해당 리소스를 찾을 수 없습니다.",
    "details": null
  },
  "timestamp": "2026-10-06T14:00:00Z"
}
```

필드별 유효성 검사 오류:

``` json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 값이 유효하지 않습니다.",
    "details": [
      { "field": "title", "message": "제목은 필수입니다." }
    ]
  },
  "timestamp": "2026-10-06T14:00:00Z"
}
```

#### HTTP 상태 코드 및 오류 코드

성공·오류 여부와 별개로 HTTP 상태 코드는 의미에 맞게 사용한다. 기본 매핑은 다음과 같다.

-   `400 Bad Request`: `BAD_REQUEST` (잘못된 요청 값, 잘못된 상태 전이, 완료 조건 미충족)
-   `400 Bad Request`: `VALIDATION_ERROR` (요청 DTO 검증 실패, `details`에 필드별 오류 포함)
-   `404 Not Found`: `RESOURCE_NOT_FOUND` (존재하지 않는 리소스)
-   `409 Conflict`: `RESOURCE_CONFLICT` (중복, 이미 처리된 요청과의 충돌 등)
-   `500 Internal Server Error`: `INTERNAL_SERVER_ERROR` (예상하지 못한 서버 내부 오류)

추가 규칙:

-   Envelope를 반환하는 빈 작업 응답은 기본적으로 `200 OK`를 사용한다. `204 No Content`는 응답 본문이 없어야 하므로 기본 응답 규약에서는 사용하지 않는다.
-   필요한 추가 오류 코드(도메인별 특화 코드 등)는 실제 도메인 요구사항이 생길 때 확장한다. 기본 규약의 리소스 미존재 코드는 `RESOURCE_NOT_FOUND`를 사용한다. 기존 예시에 있던 `QUEST_NOT_FOUND` 같은 도메인 특화 코드는 확정된 기본 코드가 아니라 향후 도메인 요구사항 발생 시 확장하는 예시로 취급한다.
-   예상하지 못한 서버 오류의 Stack Trace, SQL, 내부 경로는 클라이언트에 노출하지 않는다.
-   인증·인가는 MVP 범위가 아니므로 인증 관련 설계를 새로 추가하지 않는다.
-   페이지네이션은 실제 API 요구사항이 정해지기 전까지 공통 응답 구조에 포함하지 않는다.

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
5.  현재 요청에서 수정이 허용된 파일과 구현·검토·보고 범위를 확정합니다.

### 구현 중

1.  전체 재작성보다 점진적 변경을 우선합니다.
2.  명시적 타입을 사용하고 불필요한 `any`를 사용하지 않습니다.
3.  Entity/API/DB 구조 변경 시 이유와 영향을 확인합니다.
4.  새 라이브러리 추가 전 기존 의존성을 확인합니다.
5.  기획서에 없는 기능을 임의로 추가하지 않습니다.
6.  도메인 불변조건을 위반하는 구현을 하지 않습니다.
7.  FE/BE DTO 또는 API 변경 시 영향 파일을 먼저 식별합니다.
8.  과거 이력이나 성장 보상이 중복 생성되지 않도록 합니다.
9.  아직 구현되지 않은 도메인을 전제로 통합 테스트를 작성하거나 완료
    조건으로 요구하지 않습니다.
10. 현재 단계에서 가능한 단위 테스트와 후속 도메인 구현 이후의 통합
    테스트를 구분합니다.
11. 미구현 기능을 완료된 기능처럼 문서화하지 않습니다.

### 사용자 협업 및 학습 중심 리뷰

사용자가 먼저 직접 코드를 작성하고 AI 에이전트의 검토를 통해 개선하는
방식을 선호한다는 점을 고려합니다.

1. 사용자가 코드 리뷰를 요청한 경우 우선 문제점과 근거를 보고합니다.
2. 수정 요청이 명시되지 않았다면 대규모 리팩터링이나 전면 재작성부터
   수행하지 않습니다.
3. 사용자의 기존 구현 의도와 학습 기회를 보존합니다.
4. 사용자가 직접 수정할 수 있도록 문제, 이유, 수정 방향을 설명합니다.
5. 수정이 허용된 경우에도 요청 범위를 벗어난 변경은 피합니다.

### ResetPolicy 작업 범위 및 단계별 검증

현재 Quest 도메인이 구현되지 않았으므로 ResetPolicy 작업의 현재 범위는
다음으로 제한합니다.

- ResetPolicy 자체의 모델 및 필드 정의
- 입력값과 필드 조합 검증
- DAILY/WEEKLY/CUSTOM 경계 계산 규칙
- 시간대 및 DST 처리
- ResetPolicy 자체의 단위 테스트
- 관련 명세와 에이전트 지침의 정합성 확인

다음 항목은 Quest 도메인 구현 이후의 후속 작업입니다.

- Quest 상태 초기화
- Quest와 ResetPolicy 간 정책 재할당
- Quest별 마지막 처리 경계의 실제 저장 및 갱신
- 앱 재시작 시 Quest 상태 복구
- Quest와 ResetPolicy 간 통합 테스트

후속 요구사항은 명세에서 삭제하지 않되, 현재 구현 완료 조건과 분리해서
기록합니다. 테스트를 실행하지 않았다면 실행한 것처럼 보고하지 않습니다.

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
빌드/테스트 결과 5. 기존 지침과 충돌한 부분 6. 다른 문서와 정합성
확인이 필요한 항목 7. 현재 구현 가능한 작업과 후속 작업의 구분 8. 미결정
사항 및 사용자 결정 필요 사항

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
