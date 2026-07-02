# 차량 수정 화면 (EditVehicle)

> 관련 파일: `feature/vehicle/.../feature/vehicle/edit/`

---

## PRD (제품 요구사항)

### 개요
기존에 등록된 차량 정보를 수정하는 폼 화면. AddVehicle과 동일한 필드 구성이며, 기존 값이 선입력된 상태로 시작한다.

### 사용자 목표
- 기존 차량 정보를 불러와 원하는 항목 수정
- 사진 교체 또는 삭제
- 커스텀 필드 추가·수정·삭제

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| EV-01 | 기존 차량 정보 선입력 | Must |
| EV-02 | 모든 필드 수정 가능 (AddVehicle과 동일) | Must |
| EV-03 | 사진 교체/삭제 (카메라·갤러리) | Should |
| EV-04 | 커스텀 필드 추가·수정·삭제 | Should |
| EV-05 | 저장 시 유효성 검사 후 DB 업데이트 | Must |
| EV-06 | 저장 완료 후 상세 화면 복귀 | Must |

---

## IA (정보 구조)

```
차량 수정 화면
├── TopAppBar ("차량 수정" + 뒤로가기)
└── 스크롤 가능한 폼 (AddVehicle과 동일 구조)
    ├── PhotoSection (기존 사진 선표시)
    ├── 차량번호 * (기존값 선입력)
    ├── 차주명 * (기존값 선입력)
    ├── 소속부서 (기존값 선입력)
    ├── 연락처 (기존값 선입력)
    ├── 차종 (기존값 선입력)
    ├── 메모 (기존값 선입력)
    ├── 커스텀 필드 섹션 (기존값 선입력)
    └── "수정하기" 버튼 (VnButton Primary Large FullWidth)
```

### 다이얼로그
- `PhotoSourceDialog`: AddVehicle과 동일

### 화면 이동
- 저장 성공 → `popBackStack()`
- 뒤로가기 → `popBackStack()`

---

## TechSpec (기술 명세)

### Route
```
edit_vehicle/{vehicleId}  (vehicleId: Long)
```

### ViewModel: `EditVehicleViewModel`

초기화 시 `SavedStateHandle`에서 `vehicleId` 읽어 `GetVehicleByIdUseCase`로 기존 데이터 로드 → State에 선입력.

**State: `EditVehicleState`**
AddVehicleState와 동일한 필드 구조 + 초기 로드 상태 관리:

| 필드 | 타입 | 설명 |
|------|------|------|
| `vehicleId` | `Long` | 수정 대상 차량 ID |
| `licensePlate` | `String` | 차량번호 |
| `licensePlateError` | `String?` | 유효성 에러 |
| `ownerName` | `String` | 차주명 |
| `ownerNameError` | `String?` | 유효성 에러 |
| `department` | `String` | 부서 |
| `phoneNumber` | `String` | 연락처 |
| `carModel` | `String` | 차종 |
| `memo` | `String` | 메모 |
| `photoPath` | `String?` | 사진 경로 |
| `pendingCameraFilePath` | `String?` | 카메라 임시 파일 |
| `previousPhotoPath` | `String?` | 취소 시 복원 경로 |
| `customFields` | `List<CustomField>` | 커스텀 필드 |
| `showPhotoSourceDialog` | `Boolean` | 다이얼로그 표시 여부 |
| `isLoading` | `Boolean` | 저장 중 |
| `error` | `String?` | 에러 메시지 |

**SideEffect: `EditVehicleSideEffect`**
AddVehicleSideEffect와 동일: `NavigateBack`, `ShowSnackbar`, `LaunchCamera`, `LaunchGallery`

### 저장 로직
- 유효성 검사: AddVehicle과 동일
- `UpdateVehicleUseCase(vehicle: Vehicle): Result<Unit>` 호출
- 성공 시 `NavigateBack`

### 사진 흐름
AddVehicle과 완전히 동일한 흐름 (`PhotoStorageManager` 사용)

### AddVehicle과의 차이점
| 항목 | AddVehicle | EditVehicle |
|------|-----------|-------------|
| 초기 상태 | 빈 폼 | DB에서 로드한 기존 값 |
| 저장 UseCase | `AddVehicleUseCase` | `UpdateVehicleUseCase` |
| 버튼 텍스트 | "등록하기" | "수정하기" |
| TopAppBar 제목 | "차량 등록" | "차량 수정" |
| Route 파라미터 | `licensePlate?` (선택) | `vehicleId` (필수) |
