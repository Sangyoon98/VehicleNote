# AI 협업 지침 (하네스 엔지니어링)

이 프로젝트는 AI와 하네스 엔지니어링 방식으로 협업합니다. 아래 규칙은 모든 작업에서 반드시 준수합니다.

## 작업 시작 전 체크리스트
1. 화면 관련 작업이면 아래 화면 스펙 문서 표에서 해당 화면 문서를 읽어 컨텍스트를 확보합니다.
2. 코드 스타일 관련 지시가 있으면 `docs/conventions/code_conventions.md`를 확인합니다.
3. 커밋 또는 PR 작업 전에는 `docs/conventions/commit_conventions.md`를 확인합니다.
4. 반복 실수 방지를 위해 `docs/conventions/mistakes_log.md`를 읽고 유사 패턴을 피합니다.

## 작업 완료 후 체크리스트
1. `.kt` 파일 수정 시 훅 또는 수동 검증으로 컴파일 상태를 확인하고, 에러가 있으면 반드시 수정합니다.
2. 화면 스펙(PRD, IA, TechSpec)이 변경되면 해당 `docs/0X_화면명.md`를 함께 업데이트합니다.
3. 잘못된 방향으로 수정했거나 사용자가 롤백을 요청한 경우 `docs/conventions/mistakes_log.md`에 기록합니다.
4. 커밋 메시지는 `docs/conventions/commit_conventions.md`의 type과 format 규칙을 따릅니다.

## 화면 스펙 문서

| 화면 | 스펙 문서 |
|------|-----------|
| 홈 (차량 목록·통계) | `docs/01_home.md` |
| 차량 등록 | `docs/02_add_vehicle.md` |
| 차량 상세 | `docs/03_vehicle_detail.md` |
| 차량 수정 | `docs/04_edit_vehicle.md` |
| 입출차 관리 (OCR 카메라) | `docs/05_entry_exit.md` |
| 입출차 기록 목록 | `docs/06_entry_exit_log_list.md` |
| 입출차 상세 | `docs/07_entry_exit_detail.md` |

새 화면이 추가되면 위 표에 항목을 추가하고 `docs/` 문서를 함께 작성합니다.

## 컨벤션 문서

| 문서 | 경로 | 언제 참고 |
|------|------|-----------|
| 코드 컨벤션 | `docs/conventions/code_conventions.md` | 코드 작성·리뷰 시 항상 |
| 커밋 컨벤션 | `docs/conventions/commit_conventions.md` | 커밋·PR 생성 시 |
| 실수 로그 | `docs/conventions/mistakes_log.md` | 작업 시작 전 / 실수 발생 시 |

## 공통 자동화

- 공통 훅 스크립트: `.ai/hooks/post_edit_lint.sh`
- 목적: Kotlin 파일 수정 직후 `./gradlew :app:compileDebugKotlin`을 실행해 컴파일 에러를 즉시 확인하고, 성공 시 docs 최신화 필요 여부를 알립니다.
- Claude는 `.claude/settings.json`에서 이 스크립트를 호출합니다.
- Codex는 `AGENTS.md`와 `.codex/` 설정 문서를 통해 같은 스크립트와 같은 체크리스트를 따릅니다.

## 빌드 명령어

```bash
# 디버그 APK 빌드
./gradlew assembleDebug

# 릴리즈 APK 빌드
./gradlew assembleRelease

# 전체 유닛 테스트
./gradlew test

# 모듈별 테스트
./gradlew :domain:test
./gradlew :data:test
./gradlew :ocr:test

# 단일 테스트 클래스 실행
./gradlew :domain:test --tests "com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCaseTest"

# 빠른 컴파일 검증
./gradlew :app:compileDebugKotlin
```

## 아키텍처 개요

Clean Architecture 기반 4개 모듈 구성: `domain`, `data`, `app`, `ocr`

| 모듈 | 설명 |
|------|------|
| `domain/` | 순수 Kotlin JVM. 모델, 레포지토리 인터페이스, 유스케이스. Android 의존성 없음 |
| `data/` | Android Library. Room DB, DAO, Entity, Mapper, 레포지토리 구현체 |
| `app/` | Android Application. Compose UI, ViewModel, Hilt DI, Navigation, `PhotoStorageManager` |
| `ocr/` | Android Library. ML Kit 한국어 OCR, `PlateRecognizer`, `CameraPreviewWithRecognition`, `KoreanPlateFilter` |

### MVI 패턴

각 화면은 `XxxAction`, `XxxState`, `XxxSideEffect`, `XxxViewModel`, `XxxScreen` 파일로 구성합니다.

- Action → State 리듀서 + SideEffect 발행
- SideEffect → Screen에서 수집
- Dialog 가시성 → SideEffect가 아닌 State에 포함

### Navigation

`Screen.kt`에 라우트를 정의하고 `NavGraph.kt`에서 연결합니다.

| 라우트 | 설명 |
|--------|------|
| `home` | 차량 목록 + 검색 + 통계 대시보드 |
| `add_vehicle?licensePlate={licensePlate}` | 차량 등록 폼 |
| `vehicle_detail/{vehicleId}` | 차량 상세 보기 |
| `edit_vehicle/{vehicleId}` | 차량 수정 폼 |
| `entry_exit` | OCR 카메라 + 최근 입출차 기록 |
| `entry_exit_log_list?filterToday={filterToday}` | 입출차 기록 전체 목록 |
| `entry_exit_detail/{recordId}` | 입출차 기록 상세 |

### DI (Hilt)

- `DatabaseModule`이 `VehicleDatabase`, `VehicleDao`, `EntryExitRecordDao`를 제공합니다.
- `RepositoryModule`과 `EntryExitRepositoryModule`이 저장소 인터페이스를 바인딩합니다.
- `OcrModule`과 `OcrEntryPoint`가 OCR 기능을 연결합니다.
- UseCase는 `@Inject constructor`를 직접 사용합니다.

### Room DB

- 현재 버전: 4
- 모든 마이그레이션은 `VehicleDatabase.kt`에 정의합니다.
- 컬럼 또는 테이블 추가 시 새 마이그레이션 상수를 추가하고 버전을 올립니다.

## 주요 규칙

- 필수 필드: `licensePlate`, `ownerName`
- `customFields: List<CustomField>`는 Room에 JSON 문자열로 저장합니다.
- `local.properties`에 API 키가 필요합니다.
- 릴리즈 빌드에는 프로젝트 루트의 `keystore.properties`, `version.properties`가 필요합니다.
- Firebase Analytics를 사용하므로 `google-services.json`이 필요합니다.
