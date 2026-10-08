# 프론트엔드 에이전트 지침 (Frontend Agent Rules)

## 1. 개요 (Overview)
본 문서는 **일퀘 (ilque)** 프로젝트의 `ilque-fe/` 디렉터리 작업 시 AI 에이전트가 준수해야 할 프론트엔드 아키텍처, 코딩 컨벤션 및 UI/UX 구현 규칙을 정의합니다.

---

## 2. 기술 스택 (Tech Stack)
- **Framework & Core**: React 18, TypeScript, Vite
- **Desktop Container**: Electron
- **Styling**: Tailwind CSS
- **State Management**: Zustand (전역 및 게이밍 상태), React Query / TanStack Query (서버 상태)
- **Icons & Animation**: Lucide React, Framer Motion

---

## 3. 디렉터리 구조 (Directory Structure)
```text
ilque-fe/
├── agent.md                    # [ilque-fe] 에이전트 전용 지침 파일
├── electron/                   # Electron Main Process & Preload Scripts
│   ├── main.ts
│   └── preload.ts
└── src/                        # React Renderer Process
    ├── assets/                 # 이미지, 사운드(경험치/레벨업 효과음 등)
    ├── components/             # UI 컴포넌트
    │   ├── common/             # Button, Modal, Card 등 범용 UI
    │   ├── layout/             # Header, Sidebar, DashboardLayout
    │   └── features/           # 도메인별 컴포넌트
    │       ├── quest/          # 퀘스트 목록, 카드, 리셋 타이머
    │       ├── todo/           # Quick Todo 컴포넌트
    │       ├── timer/          # Focus Session (몰입 타이머)
    │       └── gamification/   # EXP 바, 레벨업 팝업, Streak 배지
    ├── hooks/                  # 커스텀 훅 (useResetTimer, useEXP 등)
    ├── services/               # 백엔드 REST API 통신 (Axios / Fetch)
    ├── store/                  # Zustand 전역 상태 (userStore, questStore 등)
    ├── types/                  # TypeScript 인터페이스 및 타입 정의
    └── utils/                  # 날짜 계산, 리셋 타임 포맷터 등 유틸리티
```

---

## 4. 코딩 및 컴포넌트 작성 규칙 (Coding Conventions)

### 4.1 TypeScript 작성 지침
- 모든 DTO, Props, State에는 명시적 타입을 지정합니다. `any` 사용은 엄격히 금지합니다.
- API 응답 타입은 루트 `agent.md`에 정의된 `ResponseEnvelope<T>` 포맷을 반영합니다.

```typescript
// 예시: API 표준 응답 타입 정의
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: {
    code: string;
    message: string;
  } | null;
  timestamp: string;
}
```

### 4.2 컴포넌트 설계 가이드
- **단일 책임 원칙**: 비즈니스 로직 및 API 연동은 `hooks/` 또는 `store/`로 분리하고 컴포넌트는 UI 표현에 집중합니다.
- **컴포넌트 구조 순서**:
  1. Hooks 및 Store 호출
  2. Local State (`useState`)
  3. Event Handlers
  4. JSX Return

---

## 5. 게이밍 & UI/UX 특화 규칙 (Gamification & UX)

1. **시각적 반응성 (Immediate Feedback)**:
   - 퀘스트 완료 체크 시 경험치 바(EXP Bar) 애니메이션 및 이펙트가 즉각 동작해야 합니다.
2. **리셋 카운트다운 타이머**:
   - 카테고리별/게임별 리셋 시각(예: 새벽 04:00)까지 남은 시간을 표시하는 타이머 유틸리티(`useResetTimer`)를 활용합니다.
3. **Electron IPC 연동**:
   - 데스크톱 알림, 시스템 트레이 연동, 오버레이 타이머 등은 `electron/` 메인 프로세스와 안전한 Bridge (`window.electronAPI`)를 통해서만 통신합니다.

---

## 6. 에이전트 작업 실행 가이드 (Agent Execution Guidelines)
- 기존 Tailwind 클래스 스타일링 및 테마(다크 모드 기본) 일관성을 유지하세요.
- 컴포넌트 생성 시 관련된 TypeScript type 및 API service 함수를 함께 작성하거나 업데이트하세요.
- 수정 완료 후 프론트엔드 빌드 명령(`npm run build` 또는 `vite build`) 시 타입 에러가 발생하지 않도록 검증하세요.