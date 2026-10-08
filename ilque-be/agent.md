# 백엔드 에이전트 지침 (Backend Agent Rules)

## 1. 개요 (Overview)

본 문서는 **일퀘 (ilque)** 프로젝트의 백엔드 모듈인 `ilque-be/` 디렉터리 작업 시 AI 에이전트가 준수해야 할 백엔드 아키텍처, 코딩 컨벤션 및 비즈니스 로직 구현 규칙을 정의합니다.

> **우선순위**
> 1. `docs/project-spec.md` (설계 Source of Truth)
> 2. 프로젝트 루트 `agent.md`
> 3. 본 문서
> 4. 기존 코드의 관례
>
> 상위 문서와 본 문서가 충돌하면 `docs/project-spec.md`를 설계 기준으로 하며, 구현 범위에 추가 판단이 필요하면 충돌 내용을 먼저 사용자에게 명시합니다.

---

## 2. 기술 스택 (Tech Stack)

- **Language & JDK**: Java 21
- **Framework**: Spring Boot 3.x
- **Persistence & ORM**: Spring Data JPA, Hibernate
- **Database**: SQLite
- **Build Tool**: Gradle
- **Utilities**: Lombok, MapStruct (필요한 경우에만)

### 2.1 데이터베이스 원칙

- 기본 대상 데이터베이스는 **SQLite**입니다.
- SQLite의 데스크톱 로컬 환경 특성을 고려하여 짧은 트랜잭션을 유지합니다.
- SQLite는 WAL 모드, `synchronous=NORMAL`, `busy_timeout=5000` 설정을 우선 고려합니다.
- 동시성 문제를 애플리케이션 코드만으로 가정하지 말고 DB 트랜잭션 경계를 함께 검토합니다.
- MySQL 등 다른 DB를 추가 지원하는 것은 명시적인 요구사항이 없는 한 임의로 구현하지 않습니다.
- MVP 초기 개발에서는 JPA/Hibernate schema generation과 `ddl-auto=update`를 사용할 수 있습니다.
- `ddl-auto=update`는 장기 운영 환경의 마이그레이션 전략이 아닙니다. 스키마 안정화 후에는 `ddl-auto=validate` 등의 전환을 검토합니다.
- MVP에는 Flyway/Liquibase를 도입하지 않습니다. 별도의 스키마 마이그레이션이 필요해질 때에만 별도 설계 결정으로 검토합니다.

---

## 3. 디렉터리 및 패키지 구조 (Directory Structure)

권장 구조는 다음과 같습니다. 기존 프로젝트의 구조가 이미 존재한다면 무조건 재구성하지 말고, 현재 구조와 변경 이유를 먼저 검토합니다.

```text
ilque-be/
├── agent.md
├── build.gradle
└── src/
    ├── main/
    │   ├── java/com/ilque/
    │   │   ├── domain/
    │   │   │   ├── user/           # 사용자 및 사용자 통계
    │   │   │   ├── quest/          # Quest 정의
    │   │   │   ├── execution/      # QuestExecution
    │   │   │   ├── todo/           # Quick Todo
    │   │   │   ├── focus/          # FocusSession
    │   │   │   ├── statistics/     # StatisticsService / UserStatisticsSummary
    │   │   │   ├── category/       # Category
    │   │   │   ├── reset/          # ResetPolicy
    │   │   │   ├── achievement/    # Achievement / UserAchievement
    │   │   │   └── calendar/       # CalendarEvent
    │   │   └── global/
    │   │       ├── config/         # Web, JPA, SQLite 등 공통 설정
    │   │       ├── error/          # GlobalExceptionHandler, ErrorCode
    │   │       ├── response/       # 공통 응답 규격
    │   │       └── util/           # 공통 시간/날짜 유틸리티
    │   └── resources/
    │       └── application.yml
    └── test/
```

### 3.1 도메인 분리 원칙

- `Quest`와 `QuestExecution`을 하나의 상태 객체로 취급하지 않습니다.
- `Quest`는 반복 여부와 목표 등의 **영구적인 퀘스트 정의**입니다.
- `QuestExecution`은 특정 기간에 실제 수행해야 하는 **기간별 실행 기록**입니다.
- `FocusSession`은 `QuestExecution`에 종속된 실제 집중 세션 기록입니다.
- `QuickTodo`는 Quest와 별개의 일회성 할 일입니다.
- `CalendarEvent`는 일정/이벤트를 관리하기 위한 보조 도메인입니다.
- `Achievement`와 `UserAchievement`는 업적 정의와 사용자 획득 이력을 분리합니다.

### 3.2 User / Identity

MVP는 개인용 로컬 데스크톱 애플리케이션이며, `User`는 인증 주체가 아니라 데이터 소유권의 기준입니다.

```text
User
├── Quest
│   └── QuestExecution
│       └── FocusSession
├── QuickTodo
├── CalendarEvent
└── UserAchievement
```

- 앱 최초 실행 시 기본 Local User가 없으면 하나 생성하고, 이후 이를 현재 사용자로 사용합니다.
- 현재 MVP에서는 실제 로그인 사용자나 Spring Security `Authentication`으로 User를 결정하지 않습니다.
- 회원가입, 로그인, 비밀번호, JWT, OAuth, Spring Security 기반 사용자 인증/인가, 다중 사용자 계정은 구현하지 않습니다.
- `User`와 소유 관계는 향후 서버/다중 사용자 구조로 확장할 때의 기준으로 유지하지만, 그 확장 기능 자체는 MVP 범위가 아닙니다.

---

## 4. 도메인 모델 및 불변 규칙 (Domain Rules)

### 4.1 Quest

Quest는 다음과 같은 개념을 명확히 구분합니다.

- `questType`
  - `CHECK`
  - `TIME_BASED`
- `recurrenceType`
  - `ONCE`
  - `DAILY`
  - `WEEKLY`
  - `CUSTOM`

`DAILY`, `WEEKLY`, `CUSTOM`을 Quest의 종류(`questType`)로 취급하지 않습니다.

`recurrenceType`은 Quest가 **언제 수행 대상인지**만 결정합니다.

- `ONCE`: 한 번만 수행
- `DAILY`: 매일 수행
- `WEEKLY`: 매주 수행
- `CUSTOM`: 사용자가 선택한 요일에 수행

MVP의 `CUSTOM`은 선택 요일 반복으로 한정합니다. N일 간격, 2주 간격, 월간 반복, 특정 날짜 반복, 월말/마지막 요일, 복잡한 RRULE 형태는 구현하지 않습니다.

Quest는 대략 다음 정보를 가집니다.

- title
- description
- questType
- recurrenceType
- categoryId
- resetPolicyId
- difficulty
- targetCount
- targetTimeMinutes
- baseExp
- expPerMinute
- active

Quest 자체에는 특정 기간의 완료 상태를 저장하지 않습니다.

### 4.2 QuestExecution

QuestExecution은 특정 Quest가 특정 기간에 수행되어야 하는 실제 실행 단위입니다.

주요 상태:

```text
PENDING
IN_PROGRESS
COMPLETED
MISSED
SKIPPED
REST_DAY
```

주요 정보:

- questId
- periodStart
- periodEnd
- status
- startedAt
- completedAt
- targetCount
- actualCount
- targetDuration
- actualDuration
- earnedExp

### 4.3 ResetPolicy

Reset은 기존 Execution의 상태를 단순히 초기화하는 작업이 아닙니다.

새로운 기간이 시작되면 해당 Quest에 대한 새로운 `QuestExecution`을 생성하고, 이전 Execution은 그대로 이력으로 보존합니다.

`RecurrenceType`과 `ResetPolicy`의 책임을 서로 대체하지 않습니다.

- `RecurrenceType`은 Quest가 수행 대상인 시점(및 `CUSTOM`의 선택 요일)을 결정합니다.
- `ResetPolicy`는 해당 QuestExecution의 기간 경계(`periodStart`, `periodEnd`)를 결정합니다.

예를 들어 `recurrenceType = DAILY`, `resetTime = 05:00`이면 매일 05:00을 기준으로 새로운 Execution 기간을 생성합니다. ResetPolicy를 변경해도 이미 생성된 과거 QuestExecution의 기간 경계와 기록은 변경하지 않습니다.

ResetPolicy는 다음과 같은 정책을 표현할 수 있습니다.

- resetType
- resetTime
- timezone
- weekStartDay
- active

시간 계산 시 시스템 자정(00:00)을 임의로 기준으로 사용하지 말고 Quest에 연결된 ResetPolicy와 timezone을 기준으로 판단합니다.

장기간 앱이 꺼져 있었던 경우 Lookback은 최대 **7개 기간**까지만 개별 QuestExecution을 생성합니다. QuestExecution 조회와 Streak 관련 계산도 기본적으로 최대 7개 기간만 직접 조회합니다. 7개 기간보다 오래된 미수행분은 개별 Execution을 대량 생성하거나 무제한 조회하지 않고 `UserStatisticsSummary.longTermMissedCount` 등의 장기 집계로 처리합니다.

`ResetCalculator` 등의 유틸리티를 사용할 수 있지만, 도메인 규칙 자체를 유틸리티에 숨겨서는 안 됩니다.

### 4.4 FocusSession

FocusSession은 특정 QuestExecution의 집중 수행 기록입니다.

- 하나의 QuestExecution은 여러 FocusSession을 가질 수 있습니다.
- FocusSession의 실제 누적 시간은 Execution의 `actualDuration` 및 통계 계산에 사용될 수 있습니다.
- FocusSession 자체가 Quest 완료를 임의로 확정하거나 EXP를 직접 지급해서는 안 됩니다.

### 4.5 Quick Todo

Quick Todo는 Quest와 완전히 별개의 간단한 일회성 할 일입니다.

반드시 다음 규칙을 지킵니다.

- EXP를 지급하지 않습니다.
- Level에 영향을 주지 않습니다.
- Streak에 영향을 주지 않습니다.
- QuestExecution을 생성하지 않습니다.
- FocusSession을 생성하거나 사용하지 않습니다.
- 완료 이력은 기본적으로 최근 **30일** 범위에서 관리하는 단순 기능으로 취급합니다.

따라서 Quick Todo 완료 로직에 Quest 완료 로직이나 `ExpLevelService`를 억지로 재사용하지 않습니다.

### 4.6 CalendarEvent

CalendarEvent는 **Quest 수행을 보조하는 일정/이벤트 관리 기능**입니다.

주요 정보:

- id
- userId
- title
- description
- eventType
- startAt
- endAt
- allDay
- categoryId (optional)
- reminderAt
- active
- createdAt
- updatedAt

이벤트 유형:

```text
GENERAL
GAME_EVENT
GAME_PICKUP
GAME_UPDATE
```

CalendarEvent는 기본적으로 다음 기능을 수행하지 않습니다.

- QuestExecution 생성
- EXP 지급
- Level 변경
- Streak 변경

MVP에서는 CalendarEvent의 CRUD, 기간별 조회, Upcoming 조회, `reminderAt` 기반 알림 대상 판단 및 Dashboard Upcoming Events 제공을 구현 대상으로 합니다. 외부 캘린더 연동과 협업/회의 중심 기능은 구현하지 않습니다.

`CalendarReminderService`는 `reminderAt`을 기준으로 알림 대상 여부를 계산하고, 실제 데스크톱 알림 표시는 Electron Main Process가 담당합니다.

CalendarEvent는 일반적인 협업 캘린더/회의 시스템으로 확장하지 않습니다.

### 4.7 Achievement

Achievement는 업적의 정의이고, `UserAchievement`는 사용자가 업적을 획득한 기록입니다.

Achievement 주요 정보:

- code
- title
- description
- conditionType
- conditionValue
- tier
- rewardExp
- isHidden
- badgeIcon

UserAchievement는 다음과 같은 사용자별 획득 상태를 가집니다.

- userId
- achievementId
- unlockedAt
- equipped

Quest 완료 후 필요한 경우 Achievement 조건을 검사하고, 최초 달성 시 UserAchievement를 생성합니다.

### 4.8 Game Quest

Game Quest는 별도의 게임 전용 테이블을 먼저 만드는 방식이 아닙니다.

기본적으로 다음 기존 도메인을 조합하여 표현합니다.

```text
Category + ResetPolicy + Quest
```

게임별 리셋 시각이 필요한 경우 ResetPolicy를 사용합니다.

---

## 5. 경험치 / 레벨 / 스트릭 규칙 (Gamification Rules)

### 5.1 EXP 지급

EXP 계산은 반드시 백엔드가 최종 결정합니다.

기본 난이도 값:

| Difficulty | Base EXP | EXP / Minute | Completion Bonus |
|---|---:|---:|---:|
| EASY | 10 | 1 | 20 |
| NORMAL | 20 | 1 | 30 |
| HARD | 40 | 2 | 50 |
| EPIC | 80 | 3 | 100 |

기본 계산식:

```text
earnedExp
= baseExp
+ min(actualMinutes, targetMinutes) × expPerMinute
+ completionBonus
```

목표 시간을 초과하여 수행한 실제 시간은 이력/통계에는 보존할 수 있지만, 기본적으로 초과 시간만큼 EXP를 추가 지급하지 않습니다.

`earnedExp`는 완료 시점에 계산하여 해당 Execution에 저장하는 **스냅샷 값**으로 취급합니다.

### 5.2 TIME_BASED 완료 규칙

시간 기반 Quest는 목표 시간 대비 수행률을 기준으로 완료 여부를 판단합니다.

- 80% 미만: 기본적으로 `IN_PROGRESS`
- 80% 이상: `COMPLETED` 조건 충족
- 80% 미만에서 사용자가 부분 완료를 명시적으로 선택하는 경우 별도 정책을 적용할 수 있습니다.
정확한 상태 전이는 Service 계층에서 일관되게 관리합니다.

### 5.3 Level

레벨 계산의 기준 경험치는 다음 규칙을 사용합니다.

```text
requiredExp(level)
= 100 + ((level - 1) × 50)
```

Level 계산과 누적 EXP의 최종 상태는 서버가 관리합니다.

### 5.4 Streak

Streak 규칙:

- `COMPLETED` → 증가
- `MISSED` → 0으로 초기화
- `SKIPPED` → 유지
- `REST_DAY` → 유지
- `PENDING` / `IN_PROGRESS` → 아직 판정하지 않음

Streak은 Quest 하나의 완료 상태가 아니라 정의된 판정 규칙에 따라 서버에서 계산합니다.

### 5.5 Rest Day

Rest Day는 사용자의 Streak을 유지하기 위한 상태입니다.

- EXP를 지급하지 않습니다.
- Streak은 유지합니다.
- 일반 Quest 완료와 동일하게 취급하지 않습니다.

---

## 6. 완료 처리 / 트랜잭션 / 동시성 (Completion & Transaction)

QuestExecution 완료 처리는 하나의 일관된 서버 트랜잭션으로 처리합니다.

일반적인 처리 순서는 다음과 같습니다.

```text
Execution 완료 검증
→ 실제 수행량 계산
→ earnedExp 계산
→ Execution COMPLETED 저장
→ EXP 반영
→ Level 계산
→ Streak 반영
→ Achievement 조건 검사
→ UserAchievement / 보상 반영
```

### 6.1 Complete API 멱등성

Complete 요청은 멱등적이어야 합니다.

이미 `COMPLETED` 상태인 Execution에 동일한 완료 요청이 다시 들어오면:

- EXP를 다시 지급하지 않습니다.
- Streak을 다시 증가시키지 않습니다.
- Achievement 보상을 중복 지급하지 않습니다.
- 기존 `earnedExp`와 상태를 반환할 수 있습니다.

중복 요청으로 인한 이중 보상은 허용하지 않습니다.

### 6.2 트랜잭션 원칙

Complete 처리에서 다음 작업은 가능한 하나의 트랜잭션 경계 안에서 원자적으로 처리합니다.

- Execution 상태 변경
- earnedExp 저장
- 사용자 EXP/Level 반영
- Streak 반영
- Achievement 획득/보상 반영

SQLite 특성상 트랜잭션을 불필요하게 길게 유지하지 않습니다.

---

## 7. API / DTO 규칙 (API & DTO)

### 7.1 Controller

Controller는 다음만 담당합니다.

- HTTP 요청/응답 처리
- Request DTO 검증
- 현재 Local User 식별 정보 전달 (인증/인가 정보가 아님)
- Service 호출
- 표준 응답 반환

Controller에 EXP 계산, Reset 기간 계산, Streak 변경 등의 핵심 비즈니스 로직을 작성하지 않습니다.

### 7.2 Service

Service는 다음을 담당합니다.

- 도메인 규칙 검증
- 상태 전이
- 트랜잭션 경계
- EXP/Level 계산
- Streak 계산
- Achievement 처리
- Reset에 따른 Execution 생성
- Today / History / Statistics 조회 및 장기 미접속 요약
- CalendarEvent 및 reminderAt 처리
- 도메인 간 협력

서비스 책임은 다음과 같이 분리합니다.

```text
QuestService             Quest / Category / ResetPolicy 관리
ExecutionService         Execution 생성, Reset, Lookback, 상태 전이
FocusService             FocusSession 시작/종료 및 시간 집계
ExpLevelService          EXP 계산 및 Level 갱신
StreakService            Current / Longest Streak 계산
AchievementService       Achievement 조건 검사 및 보상
StatisticsService        Today / Week / Month 통계 및 History
QuickTodoService         Quick Todo 및 최근 30일 이력
CalendarEventService     CalendarEvent CRUD 및 조회
CalendarReminderService  reminderAt 기반 알림 대상 계산
```

### 7.3 Repository

Repository는 영속성 접근을 담당합니다.

- Spring Data JPA를 기본으로 사용합니다.
- 단순 CRUD를 위해 불필요하게 QueryDSL/JPQL을 사용하지 않습니다.
- 복잡한 조회가 필요한 경우 QueryDSL 또는 JPQL을 선택합니다.
- Repository에 비즈니스 규칙을 과도하게 넣지 않습니다.

### 7.4 DTO / Entity

Controller API의 Request/Response에 Entity를 직접 노출하지 않습니다.

- Request DTO와 Response DTO를 분리합니다.
- DTO는 `record`를 우선 고려합니다.
- Entity는 영속 상태와 도메인 관계 관리에 집중합니다.
- Entity를 그대로 JSON으로 반환하지 않습니다.
- `Entity → DTO` 변환 시 지연 로딩과 순환 참조를 주의합니다.

### 7.5 API 응답 규격

루트 `agent.md`에서 공통 응답 Envelope를 확정한 경우 이를 따릅니다.

현재 공통 형태는 다음을 기준으로 합니다.

```java
public record ApiResponseEnvelope<T>(
    boolean success,
    T data,
    ErrorDetail error,
    Instant timestamp
) {
    public static <T> ApiResponseEnvelope<T> success(T data) {
        return new ApiResponseEnvelope<>(
            true,
            data,
            null,
            Instant.now()
        );
    }

    public static <T> ApiResponseEnvelope<T> error(
        String code,
        String message
    ) {
        return new ApiResponseEnvelope<>(
            false,
            null,
            new ErrorDetail(code, message),
            Instant.now()
        );
    }
}
```

단, 이 응답 Envelope 자체가 프로젝트 사양보다 우선하는 것은 아닙니다. 루트 Agent와 실제 API 계약이 변경되면 함께 변경합니다.

### 7.6 API URL 규칙

현재 통합 기획서의 API Prefix는 **`/api`**입니다. MVP에서는 버전형 API Prefix를 임의로 도입하거나 Prefix를 혼용하지 않습니다.

기획서에 정의된 주요 엔드포인트는 다음을 기준으로 합니다.

```text
GET    /api/quests
POST   /api/quests
GET    /api/quests/{id}
PATCH  /api/quests/{id}
DELETE /api/quests/{id}

GET    /api/reset-policies
POST   /api/reset-policies
PATCH  /api/reset-policies/{id}

GET    /api/today
POST   /api/quests/{questId}/start
POST   /api/executions/{id}/complete
POST   /api/executions/{id}/skip
POST   /api/quests/{questId}/focus/start
POST   /api/focus/{sessionId}/stop

GET    /api/achievements
GET    /api/achievements/unlocked
PATCH  /api/achievements/{id}/equip

GET    /api/statistics/today
GET    /api/statistics/week
GET    /api/statistics/month

GET    /api/calendar/events
GET    /api/calendar/events/{id}
POST   /api/calendar/events
PATCH  /api/calendar/events/{id}
DELETE /api/calendar/events/{id}
GET    /api/calendar/upcoming
GET    /api/calendar/date/{date}
```

Quick Todo API는 실제 구현 계약이 정해지는 시점에 동일한 `/api` Prefix와 프로젝트의 REST 규칙을 적용합니다.

기획서에 없는 endpoint를 편의상 추가하지 않으며, API 변경 시 FE/BE 계약을 함께 확인합니다.

### 7.7 Backend authoritative source

다음 값과 결과는 프론트엔드가 최종 계산하거나 결정하지 않으며, 백엔드 Service가 authoritative source입니다.

- QuestExecution 상태
- `earnedExp`
- Level
- Streak
- Achievement 달성 여부
- Complete 처리 결과
- Reset 결과

프론트엔드는 API가 반환한 결과를 표시하는 역할을 중심으로 합니다.

## 8. 시간 / 날짜 처리 규칙

- ResetPolicy의 timezone을 기준으로 기간을 계산합니다.
- 서버 로컬 시간에 암묵적으로 의존하지 않습니다.
- `LocalDateTime`만으로 timezone이 필요한 비즈니스 규칙을 표현하지 않습니다.
- 외부 API/DB/클라이언트 경계에서 시간 타입을 명확히 정의합니다.
- `periodStart`, `periodEnd`, `startAt`, `endAt`, `reminderAt`의 의미와 timezone을 혼동하지 않습니다.
- 날짜 기반 조회는 사용자가 보는 기준 날짜와 ResetPolicy 기준 날짜가 다른 경우를 고려합니다.

---

## 9. 데이터 무결성 및 조회 규칙

다음 불변 조건을 유지합니다.

1. Quest와 QuestExecution을 혼합하지 않습니다.
2. Quest 자체에 기간별 완료 상태를 저장하지 않습니다.
3. Reset은 기존 Execution 초기화가 아니라 새로운 Execution 생성입니다.
4. 이전 Execution 이력은 삭제하지 않습니다.
5. `earnedExp`는 완료 당시 계산된 스냅샷입니다.
6. Quick Todo는 EXP/Level/Streak/QuestExecution/FocusSession을 사용하지 않습니다.
7. CalendarEvent는 기본적으로 EXP/Streak/QuestExecution을 사용하지 않습니다.
8. Complete는 멱등적으로 처리되어야 합니다.
9. Complete에서 보상 관련 변경은 가능한 원자적으로 처리합니다.
10. 실제 수행 시간과 EXP 산정 시간이 동일한 개념이라고 가정하지 않습니다.
11. Game Quest는 별도 게임 전용 시스템을 만들기보다 기존 Quest/Category/ResetPolicy를 우선 활용합니다.

---

## 10. 예외 처리 (Error Handling)

- 전역 예외 처리는 `GlobalExceptionHandler`에서 일관되게 처리합니다.
- 도메인 오류는 의미 있는 `ErrorCode`로 정의합니다.
- Controller마다 동일한 예외 처리 코드를 반복하지 않습니다.
- 다음과 같은 오류를 명확히 구분합니다.
  - 존재하지 않는 리소스
  - 잘못된 상태 전이
  - 완료 조건 미충족
  - 중복/이미 처리된 요청
  - 잘못된 요청 값
  - 서버 내부 오류
- 내부 Stack Trace나 민감한 구현 정보를 API 응답에 노출하지 않습니다.

---

## 11. 테스트 규칙 (Testing)

비즈니스 규칙은 가능한 단위 테스트로 검증합니다.

특히 다음은 테스트 우선순위가 높습니다.

- Quest/Execution 분리
- Reset 기간 계산
- Reset에 따른 Execution 생성
- CHECK Quest 완료
- TIME_BASED 80% 규칙
- EXP 계산
- 목표 시간 초과 시 EXP 제한
- Level 계산
- Streak 증가/초기화/유지
- Rest Day
- Achievement 최초 획득
- Complete 멱등성
- 중복 완료 요청에 대한 이중 보상 방지
- CalendarEvent CRUD 및 날짜/기간 조회
- Quick Todo가 EXP/Streak에 영향을 주지 않는지

코드 변경 후 최소한 관련 테스트를 실행하고, 가능하면 다음을 확인합니다.

```bash
./gradlew test
./gradlew build
```

Windows 환경에서는 다음 명령도 사용할 수 있습니다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

테스트 실패를 무시하고 완료되었다고 보고하지 않습니다.

---

## 12. 에이전트 작업 실행 가이드 (Agent Execution Guidelines)

### 12.1 작업 전

1. `docs/project-spec.md`, 루트 `agent.md`, 본 문서를 먼저 확인합니다.
2. 관련 도메인의 기존 코드와 API를 확인합니다.
3. 기존 Entity/DTO/Repository/Service/Controller 구조를 확인합니다.
4. 기존 계약과 새 요구사항의 충돌 여부를 확인합니다.
5. DB 스키마 변경이 필요한지 판단합니다.

### 12.2 작업 중

- 요청된 범위를 벗어난 대규모 리팩터링을 하지 않습니다.
- 기존 API를 임의로 변경하지 않습니다.
- Entity 관계를 편의상 변경하지 않습니다.
- 프론트엔드에서 필요한 값을 맞추기 위해 백엔드 비즈니스 규칙을 약화하지 않습니다.
- 서버가 최종적으로 판단해야 하는 EXP/Level/Streak/Achievement 상태를 클라이언트에 위임하지 않습니다.
- 새 기능을 구현할 때 기존 도메인으로 표현할 수 있는지 먼저 검토합니다.
- 불명확한 요구사항을 임의로 발명하지 않습니다.

### 12.3 작업 후 보고

작업이 끝나면 다음을 명시합니다.

- 변경된 파일
- 추가/변경된 API
- 추가/변경된 Entity 및 DB 구조
- 핵심 비즈니스 규칙 변경
- 실행한 테스트/빌드
- 남아 있는 가정 또는 미확정 사항

---

## 13. 개발 우선순위

백엔드 구현은 다음 순서를 기본으로 합니다.

```text
1. 프로젝트/공통 설정
2. User / Category / ResetPolicy
3. Quest CRUD
4. QuestExecution 생성 및 상태 관리
5. Quest Complete + EXP / Level
6. Streak / Rest Day
7. FocusSession
8. Quick Todo / Today Dashboard 지원
9. Achievement
10. History / Statistics
11. Game Quest
12. CalendarEvent / Upcoming / Reminder
13. Electron Tray / Notification 및 기타 확장 기능
```

단, 실제 개발 단계에서 루트 Agent의 Phase 계획이나 현재 구현 상태가 다르면 현재 프로젝트 상태를 우선 확인합니다.

---

## 14. 백엔드 핵심 원칙

> **Quest는 정의이고, QuestExecution은 기간별 실행 기록이다.**

> **Reset은 상태 초기화가 아니라 새로운 Execution을 생성하는 것이다.**

> **EXP, Level, Streak, Achievement의 최종 판정은 백엔드가 담당한다.**

> **Quick Todo와 CalendarEvent는 Quest 보상 시스템과 분리한다.**

> **Complete는 멱등적이어야 하며 중복 보상을 허용하지 않는다.**

> **기존 코드와 상위 설계 문서를 확인한 뒤 최소 범위로 변경한다.**
