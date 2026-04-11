# Harness Engineering

이 스킬은 VehicleNote 저장소에서 Claude와 Codex가 동일한 하네스 엔지니어링 절차를 따르도록 안내합니다.

## 사용 시점

- 화면 변경, 아키텍처 변경, 문서 변경, 커밋, PR 작성 전후
- Kotlin 파일 수정 후 컴파일 검증이 필요할 때

## 수행 절차

1. `docs/conventions/ai_harness_engineering.md`를 읽고 공통 체크리스트를 따릅니다.
2. 관련 화면이면 대응하는 `docs/0X_화면명.md`를 먼저 읽습니다.
3. 코드 작성 전 `docs/conventions/code_conventions.md`를 확인합니다.
4. 커밋 또는 PR 전 `docs/conventions/commit_conventions.md`를 확인합니다.
5. Kotlin 파일을 수정했다면 `.ai/hooks/post_edit_lint.sh <path-to-file.kt>` 또는 자동 훅으로 `./gradlew :app:compileDebugKotlin`을 검증합니다.
6. 화면 스펙이 바뀌면 해당 `docs/` 문서를 함께 업데이트합니다.
7. 잘못된 수정이나 롤백 요청이 발생하면 `docs/conventions/mistakes_log.md`를 업데이트합니다.

## 관련 파일

- 공통 지침: `docs/conventions/ai_harness_engineering.md`
- 공통 훅: `.ai/hooks/post_edit_lint.sh`
- PR 절차: `.codex/commands/pr.md`, `.claude/commands/pr.md`
