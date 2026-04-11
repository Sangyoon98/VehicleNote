# 커밋 컨벤션 (Commit Conventions)

> 이 프로젝트의 모든 커밋은 아래 규칙을 따릅니다.
> AI가 커밋 메시지를 작성할 때도 반드시 이 문서를 참고합니다.

---

## 1. 기본 형식

```
{type}: {subject}

{body}  ← 선택사항, 72자 이내 줄바꿈
```

### 규칙
- **제목(subject)**: 50자 이내, 마침표 없음, 명령형 현재 시제
- **본문(body)**: 무엇을 왜 변경했는지 설명 (어떻게는 코드로 표현)
- **빈 줄**: 제목과 본문 사이 반드시 1줄 빈 줄

---

## 2. Type 목록

| Type | 사용 상황 | 예시 |
|------|-----------|------|
| `feat` | 새 기능 추가 | `feat: 입출차 OCR 번호판 인식 기능 추가` |
| `fix` | 버그 수정 | `fix: 카메라 취소 시 사진 경로 복원 안 되는 문제 수정` |
| `refactor` | 기능 변경 없는 코드 개선 | `refactor: HomeViewModel 통계 계산 로직 분리` |
| `style` | 포맷·공백 등 UI/코드 스타일 변경 | `style: 부서 필터 칩 모양을 pill 형태로 변경` |
| `docs` | 문서 변경 (코드 무관) | `docs: 홈 화면 스펙 문서 최신화` |
| `test` | 테스트 코드 추가·수정 | `test: AddVehicleUseCase 유효성 검사 테스트 추가` |
| `chore` | 빌드·의존성·설정 변경 | `chore: Room 버전 2.8.4로 업그레이드` |
| `perf` | 성능 개선 | `perf: 차량 목록 LazyColumn key 추가로 리컴포지션 최소화` |
| `revert` | 이전 커밋 되돌리기 | `revert: feat: 입출차 기록 삭제 기능 추가 커밋 되돌리기` |

---

## 3. Scope (선택사항)

Type 뒤에 `(scope)`를 붙여 영향 범위를 명시할 수 있습니다.

```
feat(entryexit): 번호판 수동 입력 다이얼로그 추가
fix(home): 부서 필터 선택 후 검색 시 필터 초기화 안 되는 버그 수정
chore(deps): Hilt 2.59.2로 업그레이드
```

| Scope 예시 | 설명 |
|-----------|------|
| `home` | 홈 화면 |
| `add` | 차량 등록 화면 |
| `detail` | 차량 상세 화면 |
| `edit` | 차량 수정 화면 |
| `entryexit` | 입출차 관리 화면 |
| `entryexitlog` | 입출차 기록 목록 |
| `entryexitdetail` | 입출차 기록 상세 |
| `domain` | domain 모듈 |
| `data` | data 모듈 |
| `ocr` | ocr 모듈 |
| `db` | Room DB·마이그레이션 |
| `di` | Hilt DI 모듈 |
| `nav` | Navigation |
| `deps` | 의존성 변경 |

---

## 4. 브랜치 전략

```
main ← 릴리즈 브랜치 (직접 push 금지)
  └─ dev ← 개발 통합 브랜치
       └─ feature/{기능명} ← 기능 개발
       └─ fix/{버그명}     ← 버그 수정
       └─ claude/{작업명}  ← AI 작업 브랜치 (자동 생성)
```

### 브랜치 네이밍
- `feature/ocr-plate-recognition`
- `fix/camera-cancel-restore`
- `claude/review-dev-branch-xxxxx` (Claude Code 자동 생성 패턴)

---

## 5. PR 규칙

### 제목
커밋 타입과 동일한 형식:
```
feat: 입출차 OCR 번호판 인식 기능 추가
```

### 본문 템플릿
```markdown
## 변경 사항
- 변경 내용 요약 (bullet)

## 테스트
- [ ] 로컬 빌드 성공 확인
- [ ] 관련 유닛 테스트 통과
- [ ] 에뮬레이터/실기기 수동 테스트

## 관련 이슈
closes #이슈번호
```

### PR 크기
- 단일 기능 단위로 작게 유지
- 1 PR = 1 목적 (기능 + 리팩토링 혼용 금지)

---

## 6. 커밋 단위 원칙

- **원자적 커밋**: 하나의 커밋은 하나의 논리적 변경
- **빌드 가능 상태 유지**: 모든 커밋은 빌드 가능해야 함
- `WIP` 커밋은 PR 전에 squash 또는 `fixup`
- AI가 작업할 때: 기능 단위로 커밋, 린트 통과 후 커밋

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-04-11 | 초기 문서 작성 |
