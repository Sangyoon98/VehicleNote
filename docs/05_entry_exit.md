# 입출차 관리 화면 (EntryExit)

> 관련 파일: `app/.../ui/entryexit/`, `ocr/`

---

## PRD (제품 요구사항)

### 개요
하단 탭 두 번째 화면. CameraX + ML Kit OCR로 차량 번호판을 실시간 인식해 입출차를 자동 기록한다. 번호판 인식이 어려운 경우 직접 입력도 지원한다. 최근 기록 2건을 하단 오버레이로 표시한다.

### 사용자 목표
- 카메라로 차량 번호판 자동 인식 → 입출차 기록
- 자동 인식이 어려울 때 번호판 직접 입력
- 최근 입출차 기록 빠르게 확인
- 전체 기록 목록으로 이동

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| EE-01 | 실시간 OCR 번호판 인식 (CameraPreviewWithRecognition) | Must |
| EE-02 | 번호판 인식 시 확인 다이얼로그 (PlateConfirmDialog) 표시 | Must |
| EE-03 | 다이얼로그에서 번호판 수정 후 확인 | Should |
| EE-04 | 확인 시 자동 입차/출차 판별 후 기록 저장 | Must |
| EE-05 | 번호판 직접 입력 (ManualInputDialog) | Must |
| EE-06 | 최근 기록 2건 하단 오버레이 표시 | Should |
| EE-07 | 전체 기록 보기 버튼 | Must |
| EE-08 | 카메라 권한 없을 때 권한 요청 UI | Must |

### 입차/출차 자동 판별 로직
- 해당 번호판의 가장 최근 기록 조회
- 최근 기록 없음 또는 마지막이 `출차` → **입차** 기록
- 마지막이 `입차` → **출차** 기록

---

## IA (정보 구조)

```
입출차 관리 화면
├── 전체화면 카메라 프리뷰 (CameraPreviewWithRecognition)
│   └── 권한 없음: 검정 배경 + 권한 요청 안내 + 버튼
└── 하단 오버레이 (RoundedCorner 상단 모서리, background 색상)
    ├── 최근 기록 섹션 (최대 2건, 애니메이션 fade)
    │   └── 각 기록: 차량 아이콘 + 번호판 + 시각 + 입차/출차 뱃지
    ├── 구분선
    └── 액션 버튼 Row
        ├── "직접 입력" (Secondary Small, Edit 아이콘)
        └── "전체 기록 보기" (Ghost Small)
```

### 다이얼로그
1. `PlateConfirmDialog`: 인식된 번호판 표시 + 수정 가능 TextField + 확인/취소
2. `ManualInputDialog`: 번호판 직접 입력 TextField + 확인/취소

### 화면 이동
- 최근 기록 아이템 탭 → `entry_exit_detail/{recordId}`
- "전체 기록 보기" → `entry_exit_log_list`

---

## TechSpec (기술 명세)

### ViewModel: `EntryExitViewModel`

**State: `EntryExitState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `records` | `List<EntryExitRecord>` | 전체 기록 (최근 2건만 표시) |
| `isLoading` | `Boolean` | 로딩 상태 |
| `error` | `String?` | 에러 메시지 |
| `showPlateConfirmDialog` | `Boolean` | 번호판 확인 다이얼로그 |
| `detectedPlate` | `String` | OCR 인식된 번호판 (다이얼로그에서 수정 가능) |
| `showManualInputDialog` | `Boolean` | 수동 입력 다이얼로그 |
| `manualInputPlate` | `String` | 직접 입력 번호판 |

**SideEffect: `EntryExitSideEffect`**

| 이름 | 설명 |
|------|------|
| `NavigateToDetail(recordId)` | 기록 상세 이동 |
| `NavigateToLogList` | 전체 기록 목록 이동 |
| `ShowSnackbar(message)` | 스낵바 표시 |

**Action 처리**

| Action | State 변화 | ViewModel 처리 |
|--------|-----------|----------------|
| `PlateDetected(plate)` | 다이얼로그 미표시 상태에서만 `showPlateConfirmDialog=true` | — |
| `DetectedPlateEdited(plate)` | `detectedPlate` 업데이트 | — |
| `PlateConfirmed` | 다이얼로그 닫기 | `recordEntryExit(detectedPlate)` |
| `ManualInputConfirmed` | 다이얼로그 닫기 | `recordEntryExit(manualInputPlate)` |
| `RecordClicked(id)` | — | `NavigateToDetail` SideEffect |
| `LogListClicked` | — | `NavigateToLogList` SideEffect |

### 입출차 기록 저장 (`recordEntryExit`)
```kotlin
1. GetVehicleByLicensePlateUseCase(plate) → vehicleId (없으면 null)
2. EntryExitRepository.getLastRecordByPlate(plate) → lastRecord
3. type = if (lastRecord == null || lastRecord.type == ENTRY) EXIT else ENTRY
   // 마지막 기록이 입차면 출차, 그 외는 입차
4. RecordEntryExitUseCase(EntryExitRecord(plate, type, now, vehicleId))
5. 성공 시 ShowSnackbar ("입차 완료" 또는 "출차 완료")
```

### OCR 연동
- `PlateRecognizer`는 `OcrEntryPoint` (Hilt EntryPoint)로 접근 → `ocr` 모듈의 `PlateRecognizerImpl`
- `CameraPreviewWithRecognition`에서 프레임마다 `onPlateDetected` 콜백 발화
- `PlateDetected` Action은 다이얼로그가 이미 표시 중이면 무시 (중복 방지)
- `DisposableEffect`로 화면 이탈 시 `plateRecognizer.close()` 호출
- `KoreanPlateFilter`는 `12가1234`, `123가1234`, `서울12가1234` 등 한국 번호판 규격 후보만 추출
- 다른 문구와 붙어 있는 후보는 `차량번호`, `번호판`, `차번` 라벨이 있는 경우만 허용하고, 일반 문장 속 혼합 텍스트는 오탐으로 제외
- 여러 번호판 후보가 동시에 보이면 직전 프레임에서 추적 중인 후보를 유지하고, 신규 후보는 화면 중앙에 가까운 번호판을 우선 선택
- 인식 성공 후 동일 번호판은 쿨다운에서 제외하지만, 다른 번호판 후보는 즉시 다음 인식 대상으로 선택 가능

### 도메인 모델
```kotlin
data class EntryExitRecord(
    val id: Long,
    val licensePlate: String,
    val type: RecordType,  // ENTRY | EXIT
    val timestamp: Long,
    val vehicleId: Long?   // 등록된 차량이면 연결, 미등록이면 null
)
```
