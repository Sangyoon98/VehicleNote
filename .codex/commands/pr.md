PR(Pull Request)을 생성합니다.

아래 단계를 순서대로 실행하세요.

## 1단계: 현재 상태 파악 (병렬 실행)

다음을 동시에 실행하세요:
- `git status` — 미커밋 변경사항 확인
- `git log --oneline main..HEAD` — PR에 포함될 커밋 목록
- `git diff main...HEAD --stat` — 변경 파일 통계
- `git branch --show-current` — 현재 브랜치명

## 2단계: 커밋 컨벤션 확인

`docs/conventions/commit_conventions.md`를 읽고 PR 제목/본문 형식을 준수합니다.

## 3단계: 린트/빌드 확인

PR 생성 전 반드시 확인:
```bash
./gradlew :app:compileDebugKotlin
```
빌드 실패 시 PR 생성을 중단하고 에러를 먼저 수정합니다.

## 4단계: PR 제목 및 본문 작성

커밋 목록과 diff를 분석해 아래 형식으로 작성합니다:

**제목 형식**: `{type}: {변경 내용 요약}` (50자 이내)
- type: feat / fix / refactor / style / docs / test / chore

**본문 템플릿**:
```markdown
## 변경 사항
- (변경된 내용을 bullet으로 요약)

## 테스트
- [ ] 로컬 빌드 성공 확인
- [ ] 관련 유닛 테스트 통과
- [ ] 에뮬레이터/실기기 수동 테스트

## 관련 문서
- (수정된 화면이 있으면 docs/0X_화면명.md 링크)

🤖 Generated with Codex
```

## 5단계: PR 생성

```bash
gh pr create \
  --base dev \
  --title "{제목}" \
  --body "$(cat <<'EOF'
{본문}
EOF
)"
```

- 기본 base 브랜치: `dev` (main에 직접 PR 금지)
- 현재 브랜치가 이미 `dev`이면 사용자에게 브랜치를 확인 요청

## 6단계: 결과 보고

PR URL을 사용자에게 전달합니다.
