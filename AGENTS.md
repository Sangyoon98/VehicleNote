# Repository Guidelines

This file provides guidance to Codex when working with code in this repository.

## AI 협업 지침 (하네스 엔지니어링)

공통 규칙과 체크리스트는 `docs/conventions/ai_harness_engineering.md`를 단일 기준 문서로 사용합니다.
Claude와 Codex 모두 같은 문서를 읽고 같은 방식으로 작업해야 합니다.

### 작업 시작 전 체크리스트

1. 화면 관련 작업이면 해당 `docs/0X_화면명.md`를 먼저 읽습니다.
2. 코드 작성 전에는 `docs/conventions/code_conventions.md`를 확인합니다.
3. 커밋 또는 PR 작업 전에는 `docs/conventions/commit_conventions.md`를 확인합니다.
4. 과거 실수를 반복하지 않도록 `docs/conventions/mistakes_log.md`를 확인합니다.

### 작업 완료 후 체크리스트

1. `.kt` 파일 수정 후 공통 훅 또는 수동 검증으로 `./gradlew :app:compileDebugKotlin`을 확인합니다.
2. 화면 스펙이 바뀌면 코드와 함께 해당 `docs/` 문서를 같은 작업 안에서 업데이트합니다.
3. 실수 또는 롤백 요청이 발생하면 `docs/conventions/mistakes_log.md`에 기록합니다.
4. 커밋 메시지는 `docs/conventions/commit_conventions.md` 규칙을 따릅니다.

### 화면 스펙 문서

| 화면 | 스펙 문서 |
|------|-----------|
| 홈 (차량 목록·통계) | `docs/01_home.md` |
| 차량 등록 | `docs/02_add_vehicle.md` |
| 차량 상세 | `docs/03_vehicle_detail.md` |
| 차량 수정 | `docs/04_edit_vehicle.md` |
| 입출차 관리 (OCR 카메라) | `docs/05_entry_exit.md` |
| 입출차 기록 목록 | `docs/06_entry_exit_log_list.md` |
| 입출차 상세 | `docs/07_entry_exit_detail.md` |

### 컨벤션 문서

| 문서 | 경로 | 언제 참고 |
|------|------|-----------|
| 코드 컨벤션 | `docs/conventions/code_conventions.md` | 코드 작성·리뷰 시 항상 |
| 커밋 컨벤션 | `docs/conventions/commit_conventions.md` | 커밋·PR 생성 시 |
| 실수 로그 | `docs/conventions/mistakes_log.md` | 작업 시작 전 / 실수 발생 시 |

### 훅 및 자동화

- 공통 훅 스크립트: `.ai/hooks/post_edit_lint.sh`
- Codex 래퍼 스크립트: `.codex/hooks/post_edit_lint.sh`
- 목적: Kotlin 파일 수정 후 `./gradlew :app:compileDebugKotlin`을 실행하고, 성공 시 docs 최신화가 필요한지 다시 확인합니다.
- Codex에서 자동 훅이 지원되지 않는 환경에서는 동일 스크립트를 수동 검증 명령으로 사용합니다.

### 스킬 및 커맨드

- 공통 하네스 스킬: `.codex/skills/harness-engineering/SKILL.md`
- 공통 PR 작성 절차는 `.codex/commands/pr.md`와 `.claude/commands/pr.md`를 동일하게 유지합니다.
- Codex 작업도 Claude와 동일하게 `dev` 기준 PR, 커밋 컨벤션, 테스트 체크리스트를 따릅니다.

## 빌드 및 검증 명령

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew :domain:test
./gradlew :data:test
./gradlew :ocr:test
./gradlew :app:compileDebugKotlin
```

## 아키텍처 개요

Clean Architecture 기반 4개 모듈 구성: `domain`, `data`, `app`, `ocr`

- `domain/`: 순수 Kotlin JVM, 모델, 레포지토리 인터페이스, 유스케이스
- `data/`: Room DB, DAO, Entity, Mapper, 레포지토리 구현체
- `app/`: Compose UI, ViewModel, Hilt DI, Navigation
- `ocr/`: CameraX, ML Kit 한국어 OCR
