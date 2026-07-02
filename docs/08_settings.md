# 설정 화면 (Settings)

> 관련 파일: `app/.../ui/settings/`, `app/.../util/VehicleCsvExporter.kt`, `app/.../util/VehicleCsvParser.kt`

---

## PRD (제품 요구사항)

### 개요
앱 전반의 동작을 제어하는 설정 화면. 바텀 내비게이션 세 번째 탭에서 진입하며, 설정 항목을 섹션 리스트 형태로 표시한다.

### 사용자 목표
- 입출차 기록 저장 기간 조절
- 차량 데이터 백업(내보내기) 및 복원(가져오기)
- 저장 공간 사용을 의식적으로 관리

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| ST-01 | 설정 항목 섹션 목록 표시 | Must |
| ST-02 | 입출차 기록 저장 기간 설정 (1일 / 1주일 / 1달 / 무한) | Must |
| ST-03 | 기간 선택 다이얼로그 (라디오 버튼) | Must |
| ST-04 | 무한 선택 시 경고 다이얼로그 → 동의 시에만 적용 | Must |
| ST-05 | 기간 변경 즉시 초과 기록 삭제 | Must |
| ST-06 | 앱 시작 시 설정된 기간 기준으로 자동 정리 | Must |
| ST-07 | 차량 데이터 CSV 내보내기 → Android 공유시트 | Must |
| ST-08 | 차량 데이터 CSV 가져오기 (파일 피커 → 확인 → 가져오기) | Must |
| ST-09 | 가져오기 시 중복 번호판 skip + 결과 다이얼로그 표시 | Must |

---

## IA (정보 구조)

```
설정 화면
├── TopAppBar: "설정"
└── 설정 항목 목록 (스크롤 가능)
    ├── [기록 관리] 섹션
    │   └── 입출차 기록 저장 기간 (현재 값 표시)
    │       ▼ (탭)
    │       [저장 기간 선택 다이얼로그] → 라디오 4개 (1일·1주일·1달·무한)
    │                                    무한 선택 시 → [경고 다이얼로그]
    └── [데이터 관리] 섹션
        ├── 차량 데이터 내보내기
        │   ▼ (탭) → 진행 중: ProgressIndicator
        │   Android 공유시트로 CSV 파일 전달
        └── 차량 데이터 가져오기
            ▼ (탭) → 파일 피커 오픈
            파일 선택 후 → [가져오기 확인 다이얼로그]
                            ├── 추가될 차량 수 / 필수항목 누락 행 수 표시
                            ├── 중복·사진 미포함 안내
                            ├── 가져오기 버튼 → 처리 → [완료 다이얼로그]
                            └── 취소
```

---

## TechSpec (기술 명세)

### Route
```
settings
```

### ViewModel: `SettingsViewModel`

**State: `SettingsState`**

| 필드 | 타입 | 설명 |
|------|------|------|
| `retentionPeriod` | `DataRetentionPeriod` | 현재 저장 기간 설정 |
| `showRetentionDialog` | `Boolean` | 기간 선택 다이얼로그 |
| `showUnlimitedWarningDialog` | `Boolean` | 무한 경고 다이얼로그 |
| `pendingPeriod` | `DataRetentionPeriod?` | 경고 확인 대기 기간 |
| `isExportingVehicles` | `Boolean` | 내보내기 진행 중 |
| `isImportingVehicles` | `Boolean` | 가져오기 진행 중 |
| `pendingImportVehicles` | `List<Vehicle>?` | 파싱 완료 후 확인 대기 |
| `pendingSkippedRows` | `Int` | 필수항목 누락으로 제외된 행 수 |
| `showImportConfirmDialog` | `Boolean` | 가져오기 확인 다이얼로그 |
| `importResult` | `VehicleImportResult?` | 완료 결과 (added, skippedByDuplicate) |

**Action: `SettingsAction`**

| 이름 | 설명 |
|------|------|
| `OnRetentionPeriodClicked` | 저장 기간 항목 탭 |
| `OnPeriodSelected(period)` | 라디오 버튼 선택 |
| `OnRetentionDialogDismissed` | 기간 다이얼로그 닫기 |
| `OnUnlimitedWarningConfirmed` | 경고 확인 |
| `OnUnlimitedWarningDismissed` | 경고 취소 |
| `OnExportVehiclesClicked` | 내보내기 버튼 탭 |
| `OnImportVehiclesClicked` | 가져오기 버튼 탭 |
| `OnImportFilePicked(uri)` | 파일 피커에서 파일 선택됨 |
| `OnImportConfirmed` | 가져오기 확인 다이얼로그 확인 |
| `OnImportDialogDismissed` | 가져오기 확인 다이얼로그 취소 |
| `OnImportResultDismissed` | 완료 다이얼로그 닫기 |

**SideEffect: `SettingsSideEffect`**

| 이름 | 설명 |
|------|------|
| `ShareVehicleCsv(uri, fileName)` | Android 공유시트로 CSV 전달 |
| `LaunchVehicleFilePicker` | 파일 피커 오픈 |

---

### 차량 CSV 포맷 (COLUMN_VERSION = 1)

| 순서 | 컬럼명 | 필수 | 비고 |
|------|--------|------|------|
| 0 | 번호판 | ✅ | 비어있으면 행 skip |
| 1 | 차주명 | ✅ | 비어있으면 행 skip |
| 2 | 부서 | - | 빈 문자열 허용 |
| 3 | 전화번호 | - | 빈 문자열 허용 |
| 4 | 차종 | - | 빈 문자열 허용 |
| 5 | 메모 | - | 빈 문자열 허용 |
| 6 | 커스텀필드 | - | JSON 배열 문자열 (`[{"key":"…","value":"…"}]`) |

**제외 항목:** `id`(자동생성), `photoPath`(미지원), `createdAt`/`updatedAt`(시스템)

**인코딩:** UTF-8 BOM (`\uFEFF`) — Excel 한국어 깨짐 방지

**필드 포맷:** RFC 4180 (따옴표 감쌈, 내부 따옴표 `""` 이스케이프)

> ⚠️ **DB 스키마 변경 주의사항**
> `VehicleEntity`의 컬럼이 추가/변경되면:
> 1. `VehicleCsvExporter.HEADERS` 및 `toRow()` 메서드 업데이트
> 2. `VehicleCsvParser.parse()` 컬럼 인덱스 업데이트
> 3. `VehicleCsvExporter.COLUMN_VERSION` 증가
> 4. 이 문서의 CSV 포맷 표 업데이트

---

### 사진(photoPath) 미지원 이유

사진 export/import는 CSV 단일 파일만으로 처리 불가 — ZIP 번들링, 내부 저장소 경로 재매핑, FileProvider 연동이 필요해 복잡도가 크게 증가함. 현재는 의도적으로 제외하며, 향후 ZIP 기반 백업 기능으로 별도 구현 예정.

---

### 가져오기 중복 처리

- `ImportVehiclesUseCase(vehicles): ImportResult` — 중복 skip 규칙은 domain에 위치
  - 번호판 존재 여부 확인 후 **이미 존재하면 skip** (update 아님) → 기존 데이터 보호
  - 반환: `ImportResult(addedCount, skippedByDuplicate)`
- ViewModel은 파싱(`VehicleCsvParser`, 내부에서 IO 디스패처 처리) → 확인 다이얼로그 → UseCase 호출만 담당
- 결과 다이얼로그: "N개 추가됨, M개 건너뜀"

---

### UseCase (저장 기간)
- `GetRetentionPeriodUseCase(): Flow<DataRetentionPeriod>`
- `SetRetentionPeriodUseCase(period: DataRetentionPeriod)`
- `PurgeOldRecordsUseCase()` — 현재 설정 조회 후 초과 기록 삭제

### UseCase (차량 CSV)
- `GetAllVehiclesUseCase(): Flow<List<Vehicle>>` — 내보내기용 스냅샷
- `AddVehicleUseCase(vehicle): Result<Long>` — 가져오기 시 1건씩 삽입
- `GetVehicleByLicensePlateUseCase(plate): Vehicle?` — 중복 체크

### DAO 추가 쿼리
```sql
-- PurgeOldRecordsUseCase용
DELETE FROM entry_exit_records WHERE timestamp < :beforeTimestamp
```
