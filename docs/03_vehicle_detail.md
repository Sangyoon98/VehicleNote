# 차량 상세 화면 (VehicleDetail)

> 관련 파일: `app/.../ui/detail/`

---

## PRD (제품 요구사항)

### 개요
특정 차량의 전체 정보를 조회하는 화면. 사진, 커스텀 필드를 포함한 모든 등록 정보를 표시하고, 수정 또는 삭제 액션을 제공한다.

### 사용자 목표
- 차량의 모든 상세 정보 확인
- 차량 사진 원본 보기
- 차량 정보 수정 화면으로 이동
- 차량 삭제

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| VD-01 | 차량 전체 정보 표시 (번호판, 차주명, 부서, 연락처, 차종, 메모) | Must |
| VD-02 | 사진 표시 및 탭 시 전체화면 뷰어 | Should |
| VD-03 | 커스텀 필드 목록 표시 | Must |
| VD-04 | 수정 버튼 → EditVehicle 이동 | Must |
| VD-05 | 삭제 버튼 → 확인 다이얼로그 → 삭제 후 홈 복귀 | Must |
| VD-06 | 로딩/에러 상태 처리 | Must |

---

## IA (정보 구조)

```
차량 상세 화면
├── TopAppBar ("차량 상세" + 뒤로가기)
└── 콘텐츠 (VehicleDetailContent)
    ├── 사진 섹션 (PhotoSection) — 사진 있을 때만
    │   └── 탭 시 ImageViewerDialog (전체화면, 핀치줌)
    ├── 기본 정보 섹션
    │   ├── 차량번호 (headline)
    │   ├── 차주명
    │   ├── 소속부서 (있을 때만)
    │   ├── 연락처 (있을 때만)
    │   ├── 차종 (있을 때만)
    │   └── 메모 (있을 때만)
    ├── 커스텀 필드 섹션 (있을 때만)
    │   └── key: value 행 목록
    └── 하단 버튼 Row
        ├── "수정" 버튼 (Secondary)
        └── "삭제" 버튼 (Danger)
```

### 다이얼로그
- `AlertDialog` (삭제 확인): "차량 삭제" 제목 + 번호판 포함 확인 메시지 → 삭제 / 취소

### 화면 이동
- 수정 버튼 → `edit_vehicle/{vehicleId}`
- 삭제 확인 → 삭제 후 `popBackStack()`
- 뒤로가기 → `popBackStack()`

---

## TechSpec (기술 명세)

### Route
```
vehicle_detail/{vehicleId}  (vehicleId: Long)
```

### ViewModel: `VehicleDetailViewModel`

`SavedStateHandle`에서 `vehicleId`를 읽어 `GetVehicleByIdUseCase`로 차량 로드.

**State: `VehicleDetailState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `vehicle` | `Vehicle?` | 조회된 차량 정보 |
| `isLoading` | `Boolean` | 로딩 상태 |
| `error` | `String?` | 에러 메시지 |
| `showDeleteDialog` | `Boolean` | 삭제 확인 다이얼로그 표시 여부 |

**SideEffect: `VehicleDetailSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateBack` | 이전 화면 복귀 |
| `NavigateToEdit(vehicleId)` | 수정 화면 이동 |
| `ShowSnackbar(message)` | 스낵바 표시 |

**Action → 처리**
- `EditClicked` → `NavigateToEdit(vehicle.id)` SideEffect
- `ShowDeleteDialog` → `showDeleteDialog = true`
- `DismissDeleteDialog` → `showDeleteDialog = false`
- `DeleteConfirmed` → `DeleteVehicleUseCase` 호출 → 성공 시 `NavigateBack`

### 컴포넌트
- `VehicleDetailContent` (`app/.../ui/detail/components/`)
- `ImageViewerDialog`: `telephoto` 라이브러리의 `ZoomableAsyncImage` 사용 (핀치줌 지원)
- `PhotoSection`: Coil `AsyncImage`로 썸네일, 탭 시 `ImageViewerDialog` 표시

### UseCase
- `GetVehicleByIdUseCase(id: Long): Vehicle?`
- `DeleteVehicleUseCase(vehicle: Vehicle): Result<Unit>`
