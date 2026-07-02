# 입출차 상세 화면 (EntryExitDetail)

> 관련 파일: `feature/entryexit/.../feature/entryexit/detail/`

---

## PRD (제품 요구사항)

### 개요
개별 입출차 기록의 상세 정보를 보여주는 화면. 기록 시각, 번호판, 유형(입차/출차)을 강조 표시하고, 연결된 차량 정보(등록된 경우)를 함께 보여준다. 미등록 차량이면 차량 등록 유도 UI를 표시한다.

### 사용자 목표
- 특정 입출차 기록의 상세 정보 확인
- 기록과 연결된 차량의 상세 정보 확인
- 미등록 차량의 경우 바로 등록 화면으로 이동

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| ED-01 | 입차/출차 구분 색상 배지 (primaryContainer / tertiaryContainer) | Must |
| ED-02 | 기록 시각 크게 표시 (HH:mm:ss, headlineLarge) | Must |
| ED-03 | 차량 번호 크게 표시 (displaySmall, Bold) | Must |
| ED-04 | 등록된 차량이면 차량 정보 카드 표시 (차주명, 부서, 연락처, 차종) | Must |
| ED-05 | 미등록 차량이면 경고 카드 + "차량 등록하기" 버튼 표시 | Should |
| ED-06 | "차량 등록하기" 클릭 시 번호판 선입력된 AddVehicle로 이동 | Should |
| ED-07 | 로딩/에러 상태 처리 | Must |

---

## IA (정보 구조)

```
입출차 상세 화면
├── TopAppBar ("입출차 상세" + 뒤로가기)
└── 스크롤 가능한 콘텐츠 (Column)
    ├── 입차/출차 배지 카드 (Surface, RoundedCorner)
    │   ├── "입차 등록" 또는 "출차 등록" (labelLarge)
    │   └── 시각 HH:mm:ss (headlineLarge)
    ├── 차량 번호 섹션
    │   ├── "차량 번호" 레이블
    │   └── 번호판 (displaySmall, Bold)
    ├── [미등록 차량인 경우]
    │   ├── 경고 카드 (errorContainer)
    │   │   ├── "미등록 차량입니다"
    │   │   └── "차량 정보를 등록하여 관리하세요"
    │   └── "차량 등록하기" 버튼 (fullWidth)
    ├── 입차 정보 카드 (Card, elevation=1)
    │   ├── "입차 정보" 제목 + 구분선
    │   ├── 입차 시각 (yyyy년 MM월 dd일 (E) HH:mm)
    │   └── 입차 방법: "자동 인식 (OCR)"
    └── [등록된 차량인 경우] 차량 정보 카드 (Card, elevation=1)
        ├── "차량 정보" 제목 + 구분선
        ├── 차주명
        ├── 소속 (있을 때만)
        ├── 연락처 (있을 때만)
        └── 차종 (있을 때만)
```

### 화면 이동
- "차량 등록하기" 클릭 → `add_vehicle?licensePlate={record.licensePlate}`
- 뒤로가기 → `popBackStack()`

---

## TechSpec (기술 명세)

### Route
```
entry_exit_detail/{recordId}  (recordId: Long)
```

### ViewModel: `EntryExitDetailViewModel`

초기화 시 `SavedStateHandle`에서 `recordId` 읽어 기록 및 연결 차량 로드.

**State: `EntryExitDetailState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `record` | `EntryExitRecord?` | 입출차 기록 |
| `vehicle` | `Vehicle?` | 연결된 차량 (없으면 null = 미등록) |
| `isLoading` | `Boolean` | 로딩 상태 |
| `error` | `String?` | 에러 메시지 |

**SideEffect: `EntryExitDetailSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateBack` | 이전 화면 복귀 |
| `NavigateToAddVehicle(licensePlate)` | 번호판 선입력된 차량 등록 화면 이동 |

**Action 처리**
- `NavigateBackClicked` → `NavigateBack` SideEffect
- `RegisterVehicleClicked` → `NavigateToAddVehicle(record.licensePlate)` SideEffect

### 데이터 로드 순서
```kotlin
1. GetEntryExitRecordByIdUseCase(recordId) → record
2. record.vehicleId != null → GetVehicleByIdUseCase(vehicleId) → vehicle
   record.vehicleId == null → vehicle = null (미등록)
```

### UI 색상 규칙
| 상태 | 배경색 | 텍스트색 |
|------|--------|---------|
| 입차 | `primaryContainer` | `onPrimaryContainer` |
| 출차 | `tertiaryContainer` | `onTertiaryContainer` |
| 미등록 경고 | `errorContainer` | `onErrorContainer` |

### 시각 포맷
- 배지 내 시각: `HH:mm:ss`
- 입차 정보 카드: `yyyy년 MM월 dd일 (E) HH:mm` (Locale.KOREAN)

### UseCase
- `GetEntryExitRecordByIdUseCase(id: Long): EntryExitRecord?`
- `GetVehicleByIdUseCase(id: Long): Vehicle?`
