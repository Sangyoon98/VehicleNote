# 코드 컨벤션 (Code Conventions)

> AI와 협업 시 이 문서를 항상 참고하세요.
> 새로운 컨벤션이 결정되면 이 문서에 추가하고 CLAUDE.md의 참조 테이블도 업데이트합니다.

---

## 1. 아키텍처 컨벤션

### 1.1 레이어 의존성 방향
```
app → domain ← data
ocr (독립)
```
- `domain`은 Android 의존성을 가질 수 없음 (`pure Kotlin JVM`)
- `data`는 `domain` 인터페이스를 구현하지만, `app`을 참조하지 않음
- `app`은 `domain` UseCase만 직접 호출; `data` 구현체는 절대 직접 참조하지 않음

### 1.2 MVI 파일 구조
화면 하나 = 반드시 아래 4개 파일:
```
ui/{화면명}/
├── {화면명}Action.kt      # sealed interface
├── {화면명}State.kt       # data class + reduce() 확장함수
├── {화면명}SideEffect.kt  # sealed interface
└── {화면명}ViewModel.kt   # @HiltViewModel
```
- `BaseViewModel` 없음 — 각 ViewModel이 직접 소유
- 화면 진입점은 `{화면명}Screen.kt` (별도 파일)
- 공통 컴포넌트는 `ui/components/`, 화면 전용 컴포넌트는 `ui/{화면명}/components/`

### 1.3 State 리듀서
```kotlin
// State 파일 내에 reduce() 확장함수로 정의
fun XxxState.reduce(action: XxxAction): XxxState = when (action) {
    is XxxAction.SomeAction -> copy(field = action.value)
    else -> this
}
```
- ViewModel에서 `_state.update { it.reduce(action) }` 패턴 사용
- 부수효과(네비게이션, 스낵바, 카메라 등)는 `reduce()`에서 처리하지 않고 ViewModel에서 SideEffect로 발행

### 1.4 SideEffect 발행
```kotlin
private fun sendSideEffect(effect: XxxSideEffect) {
    viewModelScope.launch { _sideEffect.send(effect) }
}
```

---

## 2. KDoc 주석 컨벤션

### 2.1 작성 대상
모든 `class`, `interface`, `object`, `enum`, `fun`, `property`에 KDoc 주석을 작성한다.
IDE 호버링 시 설명이 표시되는 것을 목표로 한다.

### 2.2 형식 규칙
```kotlin
/**
 * 한 줄 요약 (첫 줄, 마침표 없이).
 *
 * 필요하면 두 번째 문단에 상세 설명.
 * 동작 규칙, 제약 사항, 주의사항 등을 기술한다.
 *
 * @property xxx 프로퍼티 설명 (data class / 클래스 수준)
 * @param xxx 파라미터 설명
 * @return 반환값 설명
 */
```

### 2.3 레이어별 작성 기준

| 레이어 | 클래스 | 함수/프로퍼티 |
|--------|--------|--------------|
| **domain/model** | 모델 역할 + 각 프로퍼티 `@property` | — |
| **domain/repository** | 인터페이스 역할 + 구현체 위치 명시 | 각 메서드에 `@param`/`@return` |
| **domain/usecase** | 동작 규칙(토글 로직 등) 상세 기술 | `invoke`에 `@param`/`@return` |
| **data/entity** | 테이블명, 제약 조건, 특이사항 기술 | 각 프로퍼티 `@property` |
| **data/dao** | — | 각 쿼리 메서드에 목적 + `@param`/`@return` |
| **data/database** | 버전, 마이그레이션 이력 표 | 각 마이그레이션 상수에 변경 내용 |
| **data/mapper** | — | 변환 방향, null 처리 규칙 기술 |
| **data/repository** | 구현 전략, `@Singleton` 이유 | 오버라이드 메서드에 간략 설명 |
| **app/util** | 클래스 목적, 저장 경로, 파일 형식 | 각 함수에 `@param`/`@return` |
| **app/ViewModel** | 화면 역할, MVI 구조(상태·사이드이펙트 타입) | public 함수에 설명 |
| **app/Screen** | — | 각 화면 객체에 화면 역할 + 인수 `@param` |
| **ocr** | 알고리즘 전략(debounce, 패턴 등) | 핵심 로직 함수에 상세 설명 |

### 2.4 금지 사항
- 코드만 보면 자명한 내용 반복 금지 (`// id를 반환한다` 수준)
- 구현 세부사항이 아닌 **WHY / 제약 / 규칙**에 집중
- `/** @param vehicle 차량 */` 처럼 타입만 반복하는 빈 주석 금지

---

## 3. Kotlin 컨벤션

### 3.1 네이밍
| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스/인터페이스 | PascalCase | `VehicleRepository` |
| 함수/변수 | camelCase | `loadVehicles()` |
| 상수 (`val` in companion/object) | SCREAMING_SNAKE_CASE | `TRANSITION_DURATION` |
| private backing field | 언더스코어 prefix | `_state`, `_sideEffect` |
| 파라미터 | camelCase | `vehicleId` |

### 3.2 함수 길이
- 단일 함수는 **40줄** 이내 권장
- 복잡한 UI 빌드는 Composable 함수로 분리

### 3.3 nullable 처리
- `!!` 사용 금지 — `?.let`, `?: return`, `?: throw` 사용
- 예외: 컴파일러가 null이 아님을 보장할 수 없지만 로직상 확실한 경우, `requireNotNull()` 또는 `checkNotNull()` 사용

### 3.4 Flow
- Repository에서 반환하는 읽기 작업은 `Flow<T>` 사용
- ViewModel에서 `collect` 시 `launchIn(viewModelScope)` + `onEach` 체이닝 패턴
- 단건 읽기/쓰기/삭제는 `suspend fun` 반환

---

## 3. Compose 컨벤션

### 3.1 Screen vs Content 분리
```kotlin
// Screen: 훅/SideEffect/SnackbarHost/ViewModel 바인딩 담당
@Composable
fun XxxScreen(onNavigateBack: () -> Unit, viewModel: XxxViewModel = hiltViewModel()) { ... }

// Content: 순수 UI, Preview 가능하도록 State만 파라미터로 받음
@Composable
private fun XxxContent(state: XxxState, onAction: (XxxAction) -> Unit) { ... }
```

### 3.2 Preview
- `@Preview`는 `XxxContent` 또는 개별 컴포넌트에만 작성
- Preview 함수명: `{컴포넌트명}Preview`, `{컴포넌트명}EmptyPreview` 등
- `@Preview` 함수는 항상 `private`

### 3.3 Modifier
- 컴포넌트 함수는 `modifier: Modifier = Modifier` 파라미터를 마지막에 정의
- 내부에서 `modifier.then(...)` 또는 체이닝으로 확장

### 3.4 상태 호이스팅
- 로컬 UI 상태(`var isExpanded by remember { ... }`)는 Composable 내부에서만 사용
- 비즈니스 로직이 필요한 상태는 반드시 ViewModel State로 올림

### 3.5 Dialog 표시
- Dialog 가시성은 `State`에 `showXxxDialog: Boolean`으로 포함
- `SideEffect`로 Dialog를 열지 않음

---

## 4. DI (Hilt) 컨벤션

### 4.1 주입 방식
- UseCase: `@Inject constructor` — 별도 Module 불필요
- ViewModel: `@HiltViewModel` + `@Inject constructor`
- Repository 구현체: `@Singleton` + `@Inject constructor` + `@Binds` Module
- DB/DAO: `@Provides` Module (`DatabaseModule`)

### 4.2 모듈 파일 위치
- `app/src/main/java/.../di/` 하위
- 추상 바인딩(`@Binds`): abstract class + `@Module` + `@InstallIn(SingletonComponent::class)`
- 구체 제공(`@Provides`): object + `@Module`

---

## 5. Room DB 컨벤션

### 5.1 마이그레이션 규칙
- 기존 컬럼/테이블 **절대 삭제 불가** (마이그레이션에서 DROP 금지)
- 새 컬럼은 `ALTER TABLE ... ADD COLUMN` + `NOT NULL`이면 반드시 `DEFAULT` 값 지정
- 마이그레이션 상수명: `MIGRATION_X_Y` (X→Y 버전)
- `@Database(version = N)` 버전 올릴 때 반드시 `MIGRATION_(N-1)_N` 추가

### 5.2 Entity 네이밍
- 테이블명: snake_case (`vehicles`, `entry_exit_records`)
- 컬럼명: camelCase → Room이 자동으로 매핑
- PK: `id INTEGER PRIMARY KEY AUTOINCREMENT`

---

## 6. 테스트 컨벤션

### 6.1 단위 테스트 위치
- `domain` 레이어 UseCase 테스트: `domain/src/test/`
- `data` 레이어 Repository 테스트: `data/src/test/`
- Fake 구현체: `domain/src/test/.../repository/Fake{이름}.kt`

### 6.2 테스트 네이밍
```kotlin
@Test
fun `loadVehicles 호출 시 Flow로 목록을 반환한다`() { ... }
```
- 한글 backtick 스타일 사용
- given-when-then 구조 권장

### 6.3 테스트 도구
- Flow 테스트: `app.cash.turbine` (`Turbine`)
- Mocking: `mockk` (`io.mockk`)
- Fake 우선 사용, Mock은 외부 시스템 경계에서만

---

## 변경 이력

| 날짜 | 내용 | 결정 근거 |
|------|------|-----------|
| 2026-04-11 | 초기 문서 작성 | 기존 코드베이스 분석 기반 |
| 2026-04-19 | 섹션 2 KDoc 주석 컨벤션 추가 | 전체 코드베이스 KDoc 작성 후 규칙 문서화 |
