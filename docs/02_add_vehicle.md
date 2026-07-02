# 차량 등록 화면 (AddVehicle)

> 관련 파일: `app/.../ui/add/`

---

## PRD (제품 요구사항)

### 개요
새 차량을 등록하는 폼 화면. 필수 정보(차량번호, 차주명)와 선택 정보(부서, 연락처, 차종, 메모, 사진, 커스텀 필드)를 입력해 저장한다. 입출차 화면에서 OCR로 인식된 번호판이 선입력되어 진입할 수도 있다.

### 사용자 목표
- 차량 정보를 빠르게 입력해 등록
- 사진을 카메라 촬영 또는 갤러리에서 첨부
- 기본 필드 외 커스텀 필드로 자유롭게 추가 정보 기록

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| AV-01 | 차량번호 입력 (필수, 유효성 검사) | Must |
| AV-02 | 차주명 입력 (필수, 유효성 검사) | Must |
| AV-03 | 부서·연락처·차종·메모 입력 (선택) | Must |
| AV-04 | 사진 첨부: 카메라 촬영 또는 갤러리 선택 | Should |
| AV-05 | 커스텀 필드 추가/삭제 (key-value 쌍) | Should |
| AV-06 | 저장 버튼 클릭 시 유효성 검사 후 DB 저장 | Must |
| AV-07 | 저장 완료 또는 뒤로가기 시 이전 화면 복귀 | Must |
| AV-08 | OCR 진입 시 licensePlate 파라미터 선입력 | Should |

---

## IA (정보 구조)

```
차량 등록 화면
├── TopAppBar ("차량 등록" + 뒤로가기)
└── 스크롤 가능한 폼 (Column)
    ├── PhotoSection (사진 영역)
    │   ├── 사진 없음: 플레이스홀더 + 카메라 아이콘
    │   └── 사진 있음: 썸네일 + 삭제 버튼
    ├── 차량번호 * (필수, 에러 표시)
    ├── 차주명 * (필수, 에러 표시)
    ├── 소속부서 (선택)
    ├── 연락처 (선택, 전화 키보드)
    ├── 차종 (선택)
    ├── 메모 (선택, 멀티라인 120dp)
    ├── 커스텀 필드 섹션 (CustomFieldsSection)
    │   ├── 필드 행: key 입력 + value 입력 + 삭제 버튼
    │   └── "필드 추가" 버튼
    └── "등록하기" 버튼 (VnButton Primary Large FullWidth)
```

### 다이얼로그
- `PhotoSourceDialog`: 사진 영역 클릭 시 표시 → "카메라" / "갤러리" 선택

### 화면 이동
- 저장 성공 → `popBackStack()`
- 뒤로가기 → `popBackStack()`

---

## TechSpec (기술 명세)

### Route
```
add_vehicle?licensePlate={licensePlate}
```
- `licensePlate` 파라미터: 기본값 `""`, OCR 진입 시 인식된 번호판 값 전달

### ViewModel: `AddVehicleViewModel`

**State: `AddVehicleState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `licensePlate` | `String` | 차량번호 입력값 |
| `licensePlateError` | `String?` | 유효성 에러 메시지 |
| `ownerName` | `String` | 차주명 입력값 |
| `ownerNameError` | `String?` | 유효성 에러 메시지 |
| `department` | `String` | 부서 입력값 |
| `phoneNumber` | `String` | 연락처 입력값 |
| `carModel` | `String` | 차종 입력값 |
| `memo` | `String` | 메모 입력값 |
| `photoPath` | `String?` | 저장된 사진 경로 |
| `pendingCameraFilePath` | `String?` | 카메라 촬영 전 생성된 임시 파일 경로 |
| `previousPhotoPath` | `String?` | 카메라 취소 시 복원할 이전 경로 |
| `customFields` | `List<CustomField>` | 커스텀 필드 목록 |
| `showPhotoSourceDialog` | `Boolean` | 사진 소스 선택 다이얼로그 표시 여부 |
| `isLoading` | `Boolean` | 저장 중 로딩 상태 |
| `error` | `String?` | 저장 에러 메시지 |

**SideEffect: `AddVehicleSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateBack` | 이전 화면 복귀 |
| `ShowSnackbar(message)` | 스낵바 표시 |
| `LaunchCamera(outputUri)` | 카메라 앱 실행 |
| `LaunchGallery` | 갤러리 실행 |

### 유효성 검사 (SaveClicked)
```
licensePlate.isBlank() → licensePlateError = "차량번호를 입력해주세요"
ownerName.isBlank() → ownerNameError = "차주명을 입력해주세요"
둘 다 통과 시 → AddVehicleUseCase 호출
```
- 필수 필드 규칙은 domain에도 존재: `AddVehicleUseCase`/`UpdateVehicleUseCase`가
  차량번호·차주명이 비어 있으면 `IllegalArgumentException` 실패를 반환 (UI 검사는 UX용, domain 검사가 최종 방어선)

### 사진 흐름
1. `PhotoClicked` → `showPhotoSourceDialog = true`
2. `CameraSelected` → `PhotoStorageManager.createCameraOutputFile()` → `LaunchCamera(uri)` SideEffect
   - 카메라 앱에서 `pendingCameraFilePath` 저장, `previousPhotoPath = photoPath`
3. `CameraResultReceived(success)`
   - `true`: `photoPath = pendingCameraFilePath`, 이전 사진 삭제
   - `false`: `pendingCameraFilePath` 임시 파일 삭제, `photoPath = previousPhotoPath` 복원
4. `GallerySelected` → `LaunchGallery` SideEffect
5. `GalleryResultReceived(uri)` → `PhotoStorageManager.copyGalleryImageToInternal()` → `photoPath` 업데이트
6. `PhotoRemoved` → `PhotoStorageManager.deletePhoto(photoPath)`, `photoPath = null`

### 카메라 권한 처리 (Screen 레벨)
- 권한 있음: 바로 `cameraLauncher.launch(uri)`
- 권한 없음: `cameraPermissionLauncher.launch(CAMERA)` → 허용 시 launch, 거부 시 `CameraPermissionDenied` Action
- 갤러리: API 33+ `PickVisualMedia`, 구버전 `GetContent("image/*")`

### UseCase
- `AddVehicleUseCase(vehicle: Vehicle): Result<Long>` — 필수 필드 검증 포함, 성공 시 생성된 id 반환
