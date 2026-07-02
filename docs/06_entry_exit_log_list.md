# 입출차 기록 목록 화면 (EntryExitLogList)

> 관련 파일: `feature/entryexit/.../feature/entryexit/log/`

---

## PRD (제품 요구사항)

### 개요
전체 입출차 기록을 시간 역순으로 보여주는 목록 화면. 번호판 또는 차주명으로 검색 가능하며, 홈 통계에서 오늘 날짜 필터로 진입할 수도 있다.

### 사용자 목표
- 전체 또는 오늘의 입출차 기록 조회
- 번호판·차주명으로 특정 기록 검색
- 개별 기록 상세 조회

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| EL-01 | 전체 입출차 기록 목록 (시간 역순) | Must |
| EL-02 | `filterToday=true` 진입 시 오늘 날짜 기록만 표시 | Should |
| EL-03 | 번호판·차주명 검색 (SearchBar) | Must |
| EL-04 | 총 건수 표시 | Should |
| EL-05 | 기록 아이템 탭 → 상세 이동 | Must |
| EL-06 | 빈 상태 처리 (기록 없음 / 검색 결과 없음) | Must |
| EL-07 | 현재 목록을 CSV 파일로 내보내기 (Excel 공유) | Must |

---

## IA (정보 구조)

```
입출차 기록 목록 화면
├── TopAppBar = SearchBar (항상 펼쳐진 상태)
│   ├── 검색 비활성: 뒤로가기 아이콘 + 플레이스홀더 + [내보내기 아이콘] + 검색 아이콘
│   │   └── 내보내기 아이콘: 기록 없으면 비활성 / 내보내는 중이면 ProgressIndicator
│   └── 검색 활성: 뒤로가기(검색 종료) + 입력 + 지우기 아이콘
│       └── 검색 결과 목록 (expanded 영역)
└── 메인 콘텐츠 (검색 비활성 시)
    ├── "총 N건" 레이블
    └── 기록 카드 목록 (LazyColumn)
        └── 각 카드: 차량 아이콘 + 번호판 + 시각 + 입차완료/출차완료 뱃지
```

### 화면 이동
- 기록 카드 탭 → `entry_exit_detail/{recordId}`
- 뒤로가기 → `popBackStack()`

---

## TechSpec (기술 명세)

### Route
```
entry_exit_log_list?filterToday={filterToday}  (filterToday: Boolean, 기본값 false)
```

### ViewModel: `EntryExitLogListViewModel`

초기화 시 `SavedStateHandle`에서 `filterToday` 읽어 로드 전략 결정.

**State: `EntryExitLogListState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `records` | `List<EntryExitRecord>` | 현재 표시 중인 기록 목록 |
| `searchQuery` | `String` | 검색어 |
| `isSearchActive` | `Boolean` | SearchBar 활성 여부 |
| `isFilteredToday` | `Boolean` | 오늘 필터 적용 여부 (플레이스홀더 텍스트 변경에 사용) |
| `isLoading` | `Boolean` | 로딩 상태 |
| `error` | `String?` | 에러 메시지 |
| `isExporting` | `Boolean` | CSV 내보내기 진행 중 여부 |

**Action: `EntryExitLogListAction`** (추가)

| 이름 | 설명 |
|------|------|
| `OnExportClicked` | 내보내기 아이콘 탭 |

**SideEffect: `EntryExitLogListSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateToDetail(recordId)` | 기록 상세 이동 |
| `ShareFile(uri, fileName)` | Android 공유시트로 CSV 파일 전달 |

### 로드 전략
- `filterToday = false`: `GetEntryExitRecordsUseCase()` → 전체 기록 Flow 구독
- `filterToday = true`: 전체 기록에서 오늘 자정 이후 timestamp만 필터링

### 검색
- `SearchQueryChanged` → `SearchEntryExitRecordsUseCase(query)` Flow 구독
- 번호판과 차주명(연결된 차량) 둘 다 검색 대상

### 빈 상태 메시지
- `searchQuery.isNotEmpty()` → "검색 결과가 없습니다"
- 그 외 → "입출차 기록이 없습니다"

### UseCase
- `GetEntryExitRecordsUseCase(): Flow<List<EntryExitRecord>>`
- `SearchEntryExitRecordsUseCase(query: String): Flow<List<EntryExitRecord>>`

### CSV 내보내기 (`CsvExporter`)
- 위치: `app/.../util/CsvExporter.kt` (@Singleton, @Inject)
- 컬럼: `번호판`, `유형(입차/출차)`, `일시(yyyy-MM-dd HH:mm:ss)`, `차주명`
- 인코딩: UTF-8 BOM (`\uFEFF`) — Excel 한국어 깨짐 방지
- 필드 포맷: RFC 4180 CSV (따옴표 감쌈, 내부 따옴표 두 번 반복)
- 저장 위치: `cacheDir/exports/entry_exit_yyyyMMdd_HHmmss.csv`
- FileProvider authority: `${applicationId}.fileprovider` (cache-path)
- 공유: `Intent.ACTION_SEND` + `FLAG_GRANT_READ_URI_PERMISSION` → `createChooser()`
- 내보내기 중 trailing icon → `CircularProgressIndicator` 표시
- 기록 없을 때 내보내기 버튼 비활성화
