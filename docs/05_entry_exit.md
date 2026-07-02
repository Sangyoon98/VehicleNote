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
- 다이얼로그(확인/수동입력) 표시 중에는 `analysisEnabled=false`로 프레임 분석 자체를 일시 중지 — 다음 차량 인식 결과가 버려지며 쿨다운만 소모되는 문제 방지
- `DisposableEffect`로 화면 이탈 시 `plateRecognizer.close()` 호출

**인식 파이프라인 (`ocr` 모듈)**

1. **카메라 입력** (`CameraPreviewWithRecognition`)
   - 분석 해상도 1280x720 (`ResolutionSelector`) — 원거리·움직이는 번호판의 글자 픽셀 확보
   - 리티클 영역(중앙 약간 위)에 3초 주기 AF/AE 측광 — 저조도·역광에서 번호판 초점·노출 우선
2. **저조도 보정** (`LumaFrameEnhancer`)
   - 프레임 휘도 히스토그램(서브샘플링)으로 저조도/저대비 프레임만 선별
   - Y 플레인을 p5~p95 선형 스트레치한 그레이스케일로 변환해 ML Kit에 전달
   - 색상 정보를 버리므로 전기차(파랑)·영업용(노랑) 등 유색 번호판도 배경색과 무관
3. **후보 추출** (`KoreanPlateFilter.findPlateCandidates`)
   - `12가1234`, `123가1234`, `서울12가1234`, `임1816` 등 한국 번호판 규격 후보만 추출 (일련번호는 1000~9999만 발급되므로 첫 자리 0 제외)
   - 원본 텍스트 매칭 우선, OCR 오인식 문자 보정(O→0 등)은 폴백으로만 적용 — 보정이 주변 문자를 오염시키는 오탐 방지
   - 두 줄 번호판 대응: 인접 블록 텍스트를 정방향·역방향으로 병합해 매칭
   - 다른 문구와 붙어 있는 후보는 `차량번호`, `번호판`, `차번` 라벨이 있는 경우만 허용
   - 품질(독립 매칭) → 화면 중앙 근접도 순으로 정렬된 후보 목록 반환
4. **프레임 간 추적** (`PlateTracker`)
   - 번호판별 독립 추적: 1.5초 이내 2회 관측 시 확정 (프레임 누락 허용 — 움직이는 번호판 대응)
   - 한 프레임에 여러 번호판이 보이면 최대 3개를 동시 추적, 순차 확정
   - 1글자 오차 변형은 같은 번호판으로 묶고 확정 시 다수 관측 변형을 반환
   - 확정된 번호판은 3초 쿨다운, 화면에 계속 보이면 쿨다운 연장 (같은 차량 반복 확정 방지 — 다른 번호판은 즉시 인식 가능)

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
