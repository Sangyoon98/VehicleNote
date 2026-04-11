# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## AI 협업 지침 (하네스 엔지니어링)

이 프로젝트는 AI와 하네스 엔지니어링 방식으로 협업합니다. 아래 규칙은 **모든 작업에서 반드시** 준수합니다.

### 작업 시작 전 체크리스트
1. **화면 관련 작업**: 아래 스펙 문서 테이블에서 해당 화면 문서를 읽어 컨텍스트 확보
2. **코드 스타일 관련 지시**: `docs/conventions/code_conventions.md` 확인
3. **커밋/PR 작업**: `docs/conventions/commit_conventions.md` 확인
4. **반복 실수 방지**: `docs/conventions/mistakes_log.md` 확인 후 유사 패턴 주의

### 작업 완료 후 체크리스트
1. **린트/컴파일**: `.kt` 파일 수정 후 자동으로 훅이 실행됨. 에러가 있으면 반드시 수정 후 완료
2. **docs 최신화**: 화면 스펙(PRD·IA·TechSpec)이 변경된 경우 해당 `docs/0X_화면명.md` 업데이트
3. **실수 기록**: 잘못된 방향으로 수정했거나 사용자가 롤백을 요청한 경우 `docs/conventions/mistakes_log.md`에 기록
4. **커밋 메시지**: `docs/conventions/commit_conventions.md`의 type/format 규칙 준수

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

새 화면이 추가되면 위 표에 항목을 추가하고 `docs/` 문서를 함께 작성하세요.

### 컨벤션 문서

| 문서 | 경로 | 언제 참고 |
|------|------|-----------|
| 코드 컨벤션 | `docs/conventions/code_conventions.md` | 코드 작성·리뷰 시 항상 |
| 커밋 컨벤션 | `docs/conventions/commit_conventions.md` | 커밋·PR 생성 시 |
| 실수 로그 | `docs/conventions/mistakes_log.md` | 작업 시작 전 / 실수 발생 시 |

### 훅 (자동화)

`.claude/settings.json`에 PostToolUse 훅이 등록되어 있습니다:
- **트리거**: `Edit` 또는 `Write` 도구로 `.kt` 파일이 수정될 때
- **동작**: `./gradlew :app:compileDebugKotlin` 자동 실행
- **결과**: 컴파일 에러는 즉시 피드백, 성공 시 docs 최신화 알림 표시
- 훅 스크립트: `.claude/hooks/post_edit_lint.sh`

### 슬래시 커맨드

| 커맨드 | 설명 |
|--------|------|
| `/pr` | PR 생성 (빌드 검증 → 커밋 분석 → PR 작성 → 생성) |

---

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

# 빠른 컴파일 검증 (에러 확인용)
./gradlew :app:compileDebugKotlin
```

---

## 아키텍처 개요

Clean Architecture, 4개 모듈 구성: `domain`, `data`, `app`, `ocr`

### 모듈 역할

| 모듈 | 설명 |
|------|------|
| `domain/` | 순수 Kotlin JVM. 모델(`Vehicle`, `EntryExitRecord`, `CustomField`), 레포지토리 인터페이스, 유스케이스. Android 의존성 없음 |
| `data/` | Android Library. Room DB (버전 4, 마이그레이션 1→2→3→4), DAO, Entity, Mapper, 레포지토리 구현체. `customFields`는 `org.json`으로 JSON 직렬화 |
| `app/` | Android Application. Compose UI, ViewModel, Hilt DI 모듈, Navigation, `PhotoStorageManager` |
| `ocr/` | Android Library. ML Kit 한국어 OCR로 번호판 인식. `PlateRecognizer` 인터페이스, `CameraPreviewWithRecognition` Composable, `KoreanPlateFilter` 제공 |

### MVI 패턴

각 화면은 4개 파일로 구성: `XxxAction`, `XxxState`, `XxxSideEffect`, `XxxViewModel`
`BaseViewModel` 없음 — 각 ViewModel이 직접 소유:

```kotlin
private val _state = MutableStateFlow(XxxState())
val state: StateFlow<XxxState> = _state.asStateFlow()

private val _sideEffect = Channel<XxxSideEffect>(Channel.BUFFERED)
val sideEffect = _sideEffect.receiveAsFlow()
```

- **Action** → State 리듀서(`_state.update { it.reduce(action) }`) + SideEffect 발행
- **SideEffect** → Screen에서 `LaunchedEffect(Unit) { viewModel.sideEffect.collect { ... } }`로 소비
- **Dialog 가시성** → SideEffect가 아닌 State에 포함 (`showDeleteDialog`, `showPhotoSourceDialog` 등)

### Navigation

`Screen.kt` sealed class에 라우트 정의, `NavGraph.kt`에서 연결.
하단 탭 2개: **홈** (차량 목록), **입출차** (OCR 카메라 + 기록)

| 라우트 | 설명 |
|--------|------|
| `home` | 차량 목록 + 검색 + 통계 대시보드 |
| `add_vehicle?licensePlate={licensePlate}` | 차량 등록 폼 (OCR 번호판 선입력 가능) |
| `vehicle_detail/{vehicleId}` | 차량 상세 보기 |
| `edit_vehicle/{vehicleId}` | 차량 수정 폼 |
| `entry_exit` | OCR 카메라 + 최근 입출차 기록 |
| `entry_exit_log_list?filterToday={filterToday}` | 입출차 기록 전체 목록 |
| `entry_exit_detail/{recordId}` | 입출차 기록 상세 |

### DI (Hilt)

- `DatabaseModule` — `VehicleDatabase`(모든 마이그레이션 포함), `VehicleDao`, `EntryExitRecordDao` 제공
- `RepositoryModule` — `@Binds`로 `VehicleRepository` 바인딩 (abstract class)
- `EntryExitRepositoryModule` — `@Binds`로 `EntryExitRepository` 바인딩 (abstract class)
- `OcrModule` / `OcrEntryPoint` — `PlateRecognizer` 제공 (ocr 모듈에서 Hilt EntryPoint로 접근)
- UseCase는 `@Inject constructor` 직접 사용 — UseCaseModule 불필요

### 사진 처리

`PhotoStorageManager` (`@Singleton`)가 카메라 출력 파일 생성 및 갤러리 이미지를 내부 저장소로 복사 관리.
취소 시 `pendingCameraFilePath`로 `previousPhotoPath`를 복원하는 흐름 사용.

### Room DB

현재 버전: **4**. 모든 마이그레이션(1→2, 2→3, 3→4)은 `VehicleDatabase.kt`에 정의.
컬럼·테이블 추가 시 새 마이그레이션 상수를 추가하고 버전을 올리세요. `exportSchema = false`.

---

## 주요 규칙

- **필수 필드**: `licensePlate`, `ownerName`. 나머지 (`department`, `phoneNumber`, `carModel`, `memo`, `photoPath`, `customFields`) 는 모두 선택
- `customFields: List<CustomField>`는 Room에 JSON 문자열로 저장; 직렬화는 `VehicleMapper`에서 처리
- `local.properties`에 API 키 필요 (`gradleLocalProperties`로 접근)
- 릴리즈 빌드에는 프로젝트 루트의 `keystore.properties`, `version.properties` 필요
- Firebase Analytics 연동 (`google-services.json` 필요)
