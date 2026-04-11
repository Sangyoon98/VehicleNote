# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## AI 협업 지침 (하네스 엔지니어링)

공통 규칙과 체크리스트는 `docs/conventions/ai_harness_engineering.md`를 단일 기준 문서로 사용합니다.
Claude와 Codex 모두 같은 문서를 읽고 같은 방식으로 작업해야 합니다.

### 훅 (자동화)

`.claude/settings.json`에 PostToolUse 훅이 등록되어 있습니다:
- **트리거**: `Edit` 또는 `Write` 도구로 `.kt` 파일이 수정될 때
- **동작**: 공통 훅 스크립트 `.ai/hooks/post_edit_lint.sh` 실행
- **결과**: 컴파일 에러는 즉시 피드백, 성공 시 docs 최신화 알림 표시
- Claude 래퍼 스크립트: `.claude/hooks/post_edit_lint.sh`

### 슬래시 커맨드

| 커맨드 | 설명 |
|--------|------|
| `/pr` | PR 생성 (빌드 검증 → 커밋 분석 → PR 작성 → 생성) |
