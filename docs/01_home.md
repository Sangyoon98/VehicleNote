# 홈 화면 (Home)

> 관련 파일: `feature/vehicle/.../feature/vehicle/home/`

---

## PRD (제품 요구사항)

### 개요
앱의 메인 진입 화면. 등록된 차량 전체 목록을 보여주고, 검색·필터·통계를 통해 빠르게 원하는 차량을 찾을 수 있다.

### 사용자 목표
- 등록된 차량 전체를 한눈에 파악
- 차량 번호 또는 이름으로 빠르게 검색
- 부서별로 필터링해 특정 그룹의 차량만 조회
- 통계 카드를 통해 전체 현황과 오늘 등록 현황을 즉시 확인
- 차량 등록 페이지로 이동

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| H-01 | 전체 차량 목록 표시 (실시간 Flow) | Must |
| H-02 | 차량 번호 검색 (SearchBar, 실시간) | Must |
| H-03 | 통계 섹션: 전체 등록 차량 수, 오늘 등록 수, 부서별 전체 현황 | Must |
| H-04 | 부서별 필터 칩 (전체 + 각 부서, pill 모양) | Must |
| H-05 | 최근 등록 차량 가로 스크롤 카드 (최대 5대) | Should |
| H-06 | 차량 목록 아이템 스와이프 삭제 | Should |
| H-07 | 통계 카드 클릭 → 해당 섹션 스크롤 또는 화면 이동 | Should |
| H-08 | FAB "차량 등록" 버튼 | Must |

### 비기능 요구사항
- 차량 목록은 Flow로 구독해 DB 변경 즉시 반영
- 검색 중에는 FAB 숨김
- 로딩/에러/빈 상태 UI 분리 처리

---

## IA (정보 구조)

```
홈 화면
├── 플로팅 SearchBar (항상 상단 고정)
│   └── 검색 활성 시: 검색 결과 리스트 (LazyColumn)
└── 메인 콘텐츠 (LazyColumn)
    ├── 통계 섹션 (StatisticsSection)
    │   ├── 전체 차량 수 카드 → 클릭 시 차량 목록으로 스크롤
    │   ├── 오늘 등록 수 카드 → 클릭 시 EntryExitLogList(filterToday=true)로 이동
    │   └── 부서별 통계 목록 (전체) → 클릭 시 해당 부서 필터 적용 + 필터 칩으로 스크롤
    ├── 최근 등록 차량 (LazyRow, 최대 5대) — 차량 있을 때만 표시
    ├── 차량 목록 헤더 ("차량 목록" + "총 N대")
    ├── 부서 필터 칩 (LazyRow) — 부서 있을 때만 표시
    │   ├── "전체" 칩
    │   └── 부서명 칩 목록 (알파벳순)
    └── 차량 리스트 (VehicleListItem)
        └── 각 아이템: 차량번호, 차주명, 부서, 스와이프 삭제
```

### 화면 이동
- FAB 클릭 → `add_vehicle`
- 차량 아이템 클릭 → `vehicle_detail/{vehicleId}`
- 오늘 등록 수 카드 클릭 → `entry_exit_log_list?filterToday=true`

---

## TechSpec (기술 명세)

### ViewModel: `HomeViewModel`

**State: `HomeState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `vehicles` | `List<Vehicle>` | 현재 필터/검색 적용된 차량 목록 |
| `recentVehicles` | `List<Vehicle>` | 최근 등록 차량 최대 5대 |
| `searchQuery` | `String` | 검색어 |
| `isSearchActive` | `Boolean` | SearchBar 활성 여부 |
| `isLoading` | `Boolean` | 로딩 상태 |
| `error` | `String?` | 에러 메시지 |
| `totalVehicleCount` | `Int` | 전체 차량 수 |
| `todayRegisteredCount` | `Int` | 오늘 등록된 차량 수 |
| `departmentStats` | `Map<String, Int>` | 부서별 차량 수 (전체, 내림차순) |
| `departmentList` | `List<String>` | 필터 칩용 부서 목록 (오름차순) |
| `selectedDepartment` | `String?` | 선택된 부서 필터 (null = 전체) |

**SideEffect: `HomeSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateToAdd` | 차량 등록 화면 이동 |
| `NavigateToDetail(vehicleId)` | 차량 상세 화면 이동 |
| `ShowSnackbar(message)` | 스낵바 표시 (삭제 성공/실패) |
| `ScrollToFilter` | 부서 필터 칩 위치로 스크롤 |

**핵심 로직**
- `loadVehicles()`: `GetAllVehiclesUseCase`를 Flow로 구독. `withComputedStats()`에서 통계·최근차량·부서목록을 한 번에 계산
- `cachedAllVehicles`: 부서 필터 로컬 처리를 위한 전체 목록 캐시. DB Flow 재구독 없이 필터 전환
- `selectDepartmentFromStats(department)`: 통계 카드 클릭 시 필터 적용 + `ScrollToFilter` SideEffect 발행

### 통계 계산 (`withComputedStats`)
```
todayStart = 오늘 자정 timestamp
todayRegisteredCount = vehicles.count { it.createdAt >= todayStart }
departmentStats = vehicles에서 department != null인 것들을 groupBy,
                  내림차순 정렬, 상위 5개만, Map으로 변환
departmentList = vehicles의 department를 distinct + sorted
```

### 검색
- 검색 활성 중: `SearchVehicleUseCase(query)` Flow 구독 → 결과를 `vehicles`에 반영
- 검색 비활성화 시: `loadVehicles()` 재호출로 전체 목록 복원

### 부서 필터
- SearchBar가 비활성 상태일 때만 동작
- `selectedDepartment == null`이면 `cachedAllVehicles` 전체 표시
- 통계 카드 클릭: `selectDepartmentFromStats()` → 필터 적용 후 칩 위치로 스크롤

### 스크롤 인덱스 계산
```
// 최근차량 있을 때
chipsIndex = 4  (통계=0, 최근차량헤더=1, 최근차량행=2, 목록헤더=3, 칩=4)
listHeaderIndex = 3

// 최근차량 없을 때
chipsIndex = 2  (통계=0, 목록헤더=1, 칩=2)
listHeaderIndex = 1
```

### 컴포넌트
- `StatisticsSection` — 통계 카드 3종 + 부서 바 차트
- `RecentVehicleCard` — 가로 스크롤 카드
- `VehicleListItem` — 리스트 아이템 (스와이프 삭제 포함)
- SearchBar: 항상 상단 고정, `ContentTopOffset = 16 + 56 + 16 = 88dp` 만큼 콘텐츠 상단 오프셋
