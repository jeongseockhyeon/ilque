# 프론트엔드 에이전트 지침 (Frontend Agent Rules)

## 1. 개요 (Overview)

본 문서는 **일퀘 (ilque)** 프로젝트의 `ilque-fe/` 디렉터리에서 AI Agent가 준수해야 할 프론트엔드 아키텍처, 코딩 컨벤션, 상태 관리 및 UI/UX 구현 규칙을 정의합니다.

본 문서는 Root `agent.md`의 도메인 및 비즈니스 규칙을 **프론트엔드에서 어떻게 표현하고 소비할 것인지**를 정의합니다.

Root Agent와 충돌하는 규칙은 이 문서보다 Root Agent의 규칙을 우선합니다.

---

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

## 2. 기술 스택 (Tech Stack)

- **Framework & Core**: React + TypeScript + Vite
- **Desktop Container**: Electron
- **Styling**: Tailwind CSS
- **Server State**: TanStack Query
- **Client/UI State**: Zustand
- **Icons**: Lucide React
- **Animation**: Framer Motion

### 기술 사용 원칙

- 서버에서 관리되는 데이터는 기본적으로 TanStack Query를 사용합니다.
- Zustand에는 UI 상태, 사용자 상호작용 상태, Electron 연동 상태 등 **클라이언트가 소유해야 하는 상태**를 저장합니다.
- 동일한 서버 데이터를 TanStack Query와 Zustand 양쪽에 중복 저장하지 않습니다.
- Axios와 Fetch 등 HTTP Client는 프로젝트 전체에서 가능한 한 하나의 방식으로 통일합니다.
- 새로운 라이브러리를 추가하기 전에 기존 의존성으로 해결할 수 있는지 확인합니다.

---

## 3. Electron 프로세스 구조

```text
Electron Main Process
        │
        ├── Spring Boot JAR 실행/관리
        ├── Window
        ├── Tray
        ├── Notification
        └── IPC
              │
              ▼
       Preload Bridge
              │
              ▼
React Renderer Process
              │
              └── HTTP → Spring Boot API
```

### 프로세스 책임

#### Electron Main

- Spring Boot 백엔드 프로세스 실행 및 종료 관리
- BrowserWindow 관리
- 시스템 트레이 관리
- 데스크톱 알림
- 필요한 Electron 전용 기능 제공

#### Preload

- Renderer에 필요한 Electron API만 안전하게 노출합니다.
- `window.electronAPI` 형태의 명시적인 Bridge를 사용합니다.
- 필요한 IPC 채널만 최소한으로 노출합니다.

#### React Renderer

- UI 렌더링
- 사용자 입력
- 서버 API 호출
- 서버 상태 표시
- 클라이언트 UI 상태 관리

### Electron 보안 원칙

- Renderer에서 Node.js/Electron 내부 API를 직접 사용하지 않습니다.
- `window.require`, `require('electron')` 등의 직접 접근을 사용하지 않습니다.
- IPC는 Preload Bridge를 통해서만 사용합니다.
- `contextIsolation` 등 Electron의 보안 설정을 임의로 비활성화하지 않습니다.
- 새로운 IPC 채널을 추가할 경우 Main / Preload / Renderer의 영향을 함께 확인합니다.

---

## 4. 권장 디렉터리 구조

```text
ilque-fe/
├── agent.md
├── electron/
│   ├── main.ts
│   └── preload.ts
└── src/
    ├── assets/
    ├── components/
    │   ├── common/
    │   ├── layout/
    │   └── features/
    │       ├── quest/
    │       ├── execution/
    │       ├── todo/
    │       ├── calendar/
    │       ├── focus/
    │       ├── achievement/
    │       ├── gamification/
    │       ├── dashboard/
    │       └── history/
    ├── hooks/
    ├── services/
    ├── store/
    ├── types/
    └── utils/
```

### 디렉터리 책임

- `components/common/`: 재사용 가능한 UI 컴포넌트
- `components/layout/`: Header, Sidebar, DashboardLayout 등 레이아웃
- `components/features/`: 도메인별 UI
- `hooks/`: React lifecycle 및 UI 동작을 캡슐화하는 Custom Hook
- `services/`: REST API 및 외부 통신
- `store/`: Zustand 기반 클라이언트/UI 상태
- `types/`: API DTO 및 프론트엔드 도메인 타입
- `utils/`: 순수 함수, 날짜/시간 포맷 등 공통 유틸리티
- `electron/`: Electron Main/Preload 전용 코드

프로젝트 초기 구조와 실제 구현이 다를 경우 기존 구조를 우선 확인하고 필요한 범위에서 점진적으로 정리합니다. AI Agent가 디렉터리를 무조건 위 구조로 재편성해서는 안 됩니다.

---

## 5. 프론트엔드 도메인 타입 규칙

Root Agent에서 정의한 도메인 의미를 프론트엔드 타입에서도 동일하게 유지합니다.

### Quest

Quest는 **정의 데이터**입니다.

다음 두 개념을 반드시 구분합니다.

```text
questType
├── CHECK
└── TIME_BASED

recurrenceType
├── ONCE
├── DAILY
├── WEEKLY
└── CUSTOM
```

프론트엔드에서 Quest 카드에 완료 여부가 필요하더라도 Quest 자체에 기간별 완료 상태를 임의로 추가하지 않습니다.

필요한 기간별 상태는 `QuestExecution` 데이터로 표현합니다.

### QuestExecution

기간별 실제 수행 상태를 표시하는 데이터입니다.

```text
PENDING
IN_PROGRESS
COMPLETED
MISSED
SKIPPED
REST_DAY
```

Today 화면에서 “오늘 완료했는가?”를 표시할 때는 Quest 자체의 임의 상태값이 아니라 해당 기간의 QuestExecution을 기준으로 합니다.

### Quick Todo

Quick Todo는 Quest와 별도의 일회성 할 일이며, 완료 이력은 기본적으로 최근 **30일** 범위에서 조회합니다.

프론트엔드에서 Quick Todo 완료 UI를 만들 때 다음 정보를 임의로 표시하거나 계산하지 않습니다.

- EXP 획득
- Level 증가
- Streak 증가
- QuestExecution 생성

### CalendarEvent

CalendarEvent는 일정/이벤트 표시 및 알림을 위한 도메인입니다.

다음과 혼합하지 않습니다.

- QuestExecution
- EXP
- Streak
- Quest 완료

CalendarEvent에서 Quest 또는 Quick Todo를 생성하는 UI를 제공할 수 있지만, 생성된 Quest/Quick Todo는 별도의 도메인 흐름으로 처리합니다.

### FocusSession

FocusSession은 QuestExecution에 연결되는 집중 수행 기록입니다.

Focus Timer UI가 실제 수행 시간을 표시하더라도 최종 완료/보상 판정은 서버 결과를 기준으로 합니다.

---

## 6. 서버 상태와 클라이언트 상태

### 6.1 TanStack Query를 사용하는 상태

다음은 서버 상태이므로 기본적으로 TanStack Query를 사용합니다.

- Quest
- QuestExecution
- Quick Todo
- CalendarEvent
- Achievement
- UserAchievement
- History
- Statistics
- Today Dashboard 데이터
- User/Level/Streak 등 서버에서 관리되는 정보

### 6.2 Zustand를 사용하는 상태

예:

- Sidebar 열림/닫힘
- Modal 열림/닫힘
- 선택된 Quest/Execution ID
- Focus Timer UI 상태
- UI 필터/정렬 상태
- 테마 등 클라이언트 설정
- Electron 연결 상태

단, 실제 서버의 완료 상태, EXP, Level, Streak을 Zustand가 독자적으로 결정하지 않습니다.

---

## 7. API 통신 규칙

### 7.1 API의 Source of Truth

프론트엔드는 API 응답을 기준으로 서버 상태를 표시합니다.

특히 다음 값은 프론트엔드가 임의로 확정하지 않습니다.

- QuestExecution 상태
- earnedExp
- 현재 EXP
- Level
- Streak
- Achievement 해금 여부
- 완료 결과

프론트엔드의 즉각적인 애니메이션은 **UI 피드백**이며 실제 게임 상태를 확정하는 근거가 아닙니다.

### 7.2 Response Envelope

Root Agent의 공통 Response Envelope를 사용합니다. 일반 API의 성공/오류 응답은 모두 동일한 Envelope이며, 필드는 `success`, `data`, `error`, `timestamp`로 구성됩니다.

```typescript
export interface FieldViolation {
  field: string;
  message: string;
}

export interface ApiError {
  code: string;
  message: string;
  details: FieldViolation[] | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiError | null;
  timestamp: string;
}
```

`ApiError`는 백엔드의 `ErrorDetail`에 대응하는 프론트엔드 측 타입 이름이다. 이름은 다르지만 필드 구조(`code`, `message`, `details`)와 JSON 응답 계약은 동일하므로 별도의 변환 없이 사용한다.

공통 API Client 처리 규칙:

- 성공 응답은 `success === true`이며 `error`는 `null`입니다. 성공 응답에서도 데이터가 없을 수 있으므로 `data`가 항상 존재한다고 가정하지 않고 `data === null`인 경우를 처리합니다.
- 오류 응답은 `success === false`, `data === null`이며 `error` 객체를 기준으로 처리합니다. `error.code`로 분기하고 `error.message`를 사용자에게 표시할 기본 메시지로 사용합니다.
- `error.details`는 선택적 필드입니다. `code === "VALIDATION_ERROR"`인 경우 `details` 배열의 `field`/`message`를 폼의 필드별 오류 표시에 사용합니다. 그 외 일반 오류에서는 `details`가 `null`이므로 필드별 오류 UI를 표시하지 않습니다.
- 성공·오류 여부와 별개로 HTTP 상태 코드는 의미에 맞게 사용됩니다. 공통 API Client는 Envelope의 `success`/`error`를 기준으로 처리하되, 필요 시 HTTP 상태(예: 404, 409)에 따른 분기를 함께 고려합니다.
- Envelope를 반환하는 빈 작업 응답은 기본적으로 `200 OK`로 오므로 별도의 `204 No Content` 빈 본문 처리를 기본 동작으로 가정하지 않습니다.
- 예상하지 못한 서버 오류는 일반 메시지로 전달되므로, Stack Trace나 내부 경로 같은 상세 정보가 응답에 포함된다고 가정하지 않습니다.
- 인증·인가는 MVP 범위가 아니므로 공통 API Client에 인증 토큰/인가 처리 로직을 새로 추가하지 않습니다.
- 페이지네이션은 실제 API 요구사항이 정해지기 전까지 공통 응답 구조에 포함되지 않으므로, 목록 응답에 페이지 정보가 있다고 임의로 가정하지 않습니다.

`timestamp`는 ISO 8601 형식의 UTC 시각 문자열입니다 (예: `2026-10-06T14:00:00Z`). 숫자형 Unix timestamp로 반환되지 않으므로 문자열 파싱을 기준으로 처리합니다. 클라이언트 표시용 시각 변환이 필요하면 이 값을 기준으로 변환합니다.

### 7.3 API Service

API 호출은 컴포넌트 내부에 직접 작성하지 않고 `services/`에 분리합니다.

예:

```text
services/
├── questService.ts
├── executionService.ts
├── quickTodoService.ts
├── calendarService.ts
├── achievementService.ts
├── statisticsService.ts
└── userService.ts
```

TanStack Query의 Query/Mutation Hook은 `hooks/` 또는 프로젝트에서 정한 별도 query 영역에서 Service를 호출하는 구조를 우선합니다.

### 7.4 Complete Mutation

QuestExecution Complete 요청은 서버의 멱등성 규칙을 신뢰합니다.

프론트엔드는:

- 동일 Complete 요청을 불필요하게 중복 전송하지 않습니다.
- 요청 중 버튼을 적절히 비활성화할 수 있습니다.
- 이미 완료된 Execution을 다시 완료 처리하지 않습니다.
- 서버가 반환한 `earnedExp`, Level, Streak, Achievement 결과를 기준으로 UI를 갱신합니다.

Complete 요청 이후 Query Cache를 적절히 invalidate/refetch하여 서버 상태와 UI 상태를 일치시킵니다.

---

## 8. Reset Time 및 날짜/시간 처리

Reset Time은 단순한 자정 기준이 아닐 수 있습니다.

예:

```text
게임 A → 05:00
게임 B → 04:00
일반 일일 퀘스트 → 00:00
```

### 원칙

- Reset Time의 **권위 있는 판정은 백엔드**가 담당합니다.
- 프론트엔드는 서버가 제공한 period 정보를 기준으로 현재 Execution을 표시합니다.
- `useResetTimer` 등의 타이머는 사용자에게 남은 시간을 보여주기 위한 UI 기능입니다.
- 프론트엔드가 임의로 날짜를 변경하여 새로운 QuestExecution을 확정해서는 안 됩니다.
- timezone을 고려하지 않은 `new Date()` 기반의 단순 날짜 비교로 Reset 경계를 확정하지 않습니다.

### Reset Countdown

`useResetTimer`는 다음과 같은 UI 목적으로 사용합니다.

```text
현재 시각
    ↓
다음 Reset Time까지 남은 시간 계산
    ↓
UI Countdown 표시
```

Countdown 종료 시 서버 상태를 다시 조회하여 실제 Execution을 갱신합니다.

---

## 9. EXP / Level / Streak UI 규칙

### EXP

프론트엔드는 서버가 계산한 `earnedExp`를 표시합니다.

프론트엔드에서 EXP를 자체 계산하여 서버 결과를 덮어쓰지 않습니다.

### Level

Level Up 애니메이션은 서버 응답을 통해 실제 Level 변화가 확인된 경우에만 표시합니다.

### Streak

Streak은 QuestExecution 결과에 따라 서버가 결정합니다.

```text
COMPLETED → 증가
MISSED → 초기화
SKIPPED → 유지
REST_DAY → 유지
PENDING → 미결정
IN_PROGRESS → 미결정
```

프론트엔드는 이 규칙을 UI 표시 기준으로 사용하되 Streak 값을 직접 변경하지 않습니다.

### Rest Day

Rest Day UI는 Streak 유지 상태를 표현하지만 EXP 획득 UI를 표시하지 않습니다.

---

## 10. Focus Timer 규칙

Focus Timer는 UI 타이머와 실제 서버 수행 기록을 구분합니다.

```text
Focus Timer UI
      ↓
FocusSession 시작
      ↓
사용자 집중 수행
      ↓
FocusSession 종료
      ↓
QuestExecution 실제 수행 시간 반영
      ↓
완료/보상 판정은 서버
```

- Timer 화면의 경과 시간만으로 Quest 완료를 확정하지 않습니다.
- 브라우저/앱이 일시 중지되거나 창이 닫히는 경우를 고려합니다.
- 실제 저장이 필요한 시점은 백엔드 API 계약에 맞춥니다.
- Focus Timer 재진입 시 서버에 저장된 상태와 로컬 UI 상태의 충돌을 확인합니다.

---

## 11. UI / UX 규칙

### 11.1 Today 중심

기본 화면은 Today Dashboard를 중심으로 구성합니다.

주요 영역:

- Today Quest
- Quick Todo
- Focus
- EXP / Level
- Streak
- Upcoming Events
- Adventurer Log

### 11.2 Immediate Feedback

Quest 완료 요청 후 다음과 같은 UI 피드백을 제공할 수 있습니다.

- EXP Bar Animation
- Level Up Animation
- Streak 변화
- Achievement Unlock Animation
- Toast / Modal

단, 애니메이션 자체가 서버 상태를 대신하지 않습니다.

### 11.3 Loading / Error / Empty State

서버 요청을 사용하는 모든 주요 화면은 최소한 다음 상태를 고려합니다.

- Loading
- Success
- Empty
- Error

Mutation에는 가능한 경우 다음 상태도 제공합니다.

- Pending
- Success
- Error

### 11.4 접근성과 사용성

- 버튼/입력 요소는 키보드로 접근 가능해야 합니다.
- 아이콘만 사용하는 버튼에는 적절한 `aria-label`을 제공합니다.
- 색상만으로 완료/실패 상태를 구분하지 않습니다.
- 애니메이션이 사용성을 방해하지 않도록 합니다.

---

## 12. 컴포넌트 설계 규칙

### 단일 책임

컴포넌트는 UI 표현과 사용자 상호작용에 집중합니다.

다음과 같은 로직은 적절한 Hook/Service/Query 계층으로 분리합니다.

- API 호출
- 복잡한 데이터 변환
- Reset Time 계산
- Focus Timer 제어
- 서버 상태 동기화

### 컴포넌트 작성 순서

권장 순서:

1. Imports
2. Props / Type
3. Query / Mutation / Hook
4. Local State
5. Derived State
6. Event Handler
7. JSX Return

### 컴포넌트 크기

하나의 컴포넌트가 지나치게 많은 도메인 책임을 가지면 하위 컴포넌트로 분리합니다.

단, 단순히 파일 수를 늘리기 위한 과도한 컴포넌트 분리는 피합니다.

---

## 13. TypeScript 규칙

- DTO, Props, State, Event에 명시적 타입을 사용합니다.
- `any` 사용을 금지합니다.
- 서버 응답 타입과 UI 전용 타입을 필요에 따라 구분합니다.
- API 응답을 임의의 타입으로 단언(`as`)하여 오류를 숨기지 않습니다.
- `unknown`이 필요한 외부 입력은 검증 후 안전한 타입으로 변환합니다.
- `enum` 또는 Union Type 등 프로젝트에서 정한 방식으로 도메인 상태를 일관되게 표현합니다.

예:

```typescript
export type QuestType = "CHECK" | "TIME_BASED";

export type RecurrenceType =
  | "ONCE"
  | "DAILY"
  | "WEEKLY"
  | "CUSTOM";

export type QuestExecutionStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "MISSED"
  | "SKIPPED"
  | "REST_DAY";
```

---

## 14. 스타일 및 애니메이션 규칙

- 기존 Tailwind 클래스와 프로젝트 테마를 우선 재사용합니다.
- 기본 테마는 프로젝트 설정에 따르며 AI Agent가 임의로 전체 테마를 변경하지 않습니다.
- Framer Motion은 상태 변화가 사용자에게 의미가 있을 때 사용합니다.
- 단순한 CSS 상태 변화에 무조건 Framer Motion을 사용하지 않습니다.
- 애니메이션이 데이터 처리보다 우선되어서는 안 됩니다.

---

## 15. Agent 작업 실행 가이드

### 작업 전

1. Root `agent.md`를 확인합니다.
2. 현재 FE 디렉터리 구조와 package 의존성을 확인합니다.
3. 관련 API Service, Type, Query/Mutation, Component의 기존 구현을 확인합니다.
4. 변경 범위를 먼저 파악합니다.
5. 백엔드 API/DTO 변경이 필요한 경우 영향받는 BE 영역을 먼저 안내합니다.

### 구현 중

1. 기존 코드를 전체 재작성하지 않습니다.
2. 요청된 기능과 관계없는 리팩토링을 하지 않습니다.
3. 기존 API 계약을 임의로 변경하지 않습니다.
4. 새로운 의존성을 추가하기 전에 기존 라이브러리를 확인합니다.
5. 서버가 결정해야 하는 상태를 프론트엔드에서 임의로 확정하지 않습니다.
6. Quick Todo와 CalendarEvent를 Quest 성장 로직과 혼합하지 않습니다.
7. Quest와 QuestExecution을 혼동하지 않습니다.
8. Electron Main 기능을 Renderer에서 직접 구현하지 않습니다.

### 작업 후

다음을 보고합니다.

1. 변경된 파일
2. 주요 변경 내용
3. API/DTO 영향
4. 상태 관리 영향
5. Electron IPC 영향
6. 실행한 빌드/테스트 결과
7. 남아 있는 문제 또는 사용자 결정 필요 사항

---

## 16. 금지되는 구현

명시적인 요구사항 변경이 없다면 다음 구현을 하지 않습니다.

- Quest에 기간별 완료 상태를 직접 저장
- Quest와 QuestExecution을 하나의 상태로 취급
- 프론트엔드가 EXP를 최종 확정
- 프론트엔드가 Streak을 최종 확정
- Reset 시 프론트엔드가 새로운 Execution을 직접 생성
- Quick Todo에 EXP/Streak 연결
- CalendarEvent에 EXP/Streak 연결
- CalendarEvent가 자동으로 QuestExecution을 생성
- Complete 재요청으로 중복 보상이 발생하도록 구현
- Electron Renderer에서 Node/Electron API 직접 호출
- 기존 API 계약을 임의 변경
- 기획서에 없는 대규모 UI/게임 시스템을 임의 추가

---

## 17. 프론트엔드 개발 우선순위

Root Agent의 전체 개발 순서를 따르되, FE에서는 다음 순서로 구현을 진행합니다.

```text
1. Vite / React / TypeScript / Electron Foundation
2. 공통 Layout / Theme / UI Foundation
3. API Client / Response Type
4. Quest / QuestExecution
5. Today Dashboard
6. Quick Todo
7. EXP / Level / Streak UI
8. Focus Timer
9. Achievement
10. History / Statistics
11. Game Quest UI
12. CalendarEvent / Upcoming
13. Electron Tray / Notification
```

핵심 도메인의 서버 계약이 확정되지 않은 상태에서 화면만 임의로 확장하지 않습니다.

---

## 18. 최우선 원칙

> **프론트엔드는 서버의 비즈니스 상태를 표현하고 사용자 행동을 전달하며, 서버의 도메인 판정을 임의로 대체하지 않는다.**

특히 다음을 항상 구분합니다.

```text
Quest
= 퀘스트 정의

QuestExecution
= 특정 기간의 수행 기록

Quick Todo
= EXP/Streak과 분리된 일회성 할 일

CalendarEvent
= Quest 수행을 보조하는 일정/이벤트

FocusSession
= QuestExecution에 연결된 집중 수행 기록
```

UI의 즉각적인 피드백과 애니메이션은 사용자 경험을 위한 것이며, 실제 EXP / Level / Streak / Achievement / 완료 상태의 최종 기준은 백엔드 응답입니다.
