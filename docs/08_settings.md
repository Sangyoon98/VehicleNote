# 설정 화면 (Settings)

> 관련 파일: `app/.../ui/settings/`

---

## PRD (제품 요구사항)

### 개요
앱 전반의 동작을 제어하는 설정 화면. 바텀 내비게이션 세 번째 탭에서 진입하며, 설정 항목을 리스트 형태로 표시한다.

### 사용자 목표
- 입출차 기록 저장 기간 조절
- 저장 공간 사용을 의식적으로 관리

### 기능 요구사항

| ID | 기능 | 우선순위 |
|----|------|----------|
| ST-01 | 설정 항목 목록 표시 | Must |
| ST-02 | 입출차 기록 저장 기간 설정 (1일 / 1주일 / 1달 / 무한) | Must |
| ST-03 | 기간 선택 다이얼로그 (라디오 버튼) | Must |
| ST-04 | 무한 선택 시 경고 다이얼로그 노출 → 동의 시에만 적용 | Must |
| ST-05 | 기간 변경 즉시 초과 기록 삭제 | Must |
| ST-06 | 앱 시작 시 설정된 기간 기준으로 자동 정리 | Must |

---

## IA (정보 구조)

```
설정 화면
├── TopAppBar: "설정"
└── 설정 항목 목록
    └── [입출차 기록 저장 기간]
        ├── 제목: "입출차 기록 저장 기간"
        └── 부제목: 현재 선택된 기간 (예: "1일")
            │
            ▼ (탭 시 다이얼로그)
        [기록 저장 기간 선택 다이얼로그]
        ├── 라디오: 1일 (기본값)
        ├── 라디오: 1주일
        ├── 라디오: 1달
        ├── 라디오: 무한
        └── 취소 버튼
            │
            ▼ (무한 선택 시)
        [경고 다이얼로그]
        ├── 내용: "데이터가 많아지면 용량을 많이 차지할 수 있습니다"
        ├── 확인 → 무한 설정 적용
        └── 취소 → 변경 취소
```

### 화면 이동
- 바텀 내비게이션 "설정" 탭 탭 → 진입
- 뒤로가기 없음 (루트 화면)

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
| `showRetentionDialog` | `Boolean` | 기간 선택 다이얼로그 표시 여부 |
| `showUnlimitedWarningDialog` | `Boolean` | 무한 경고 다이얼로그 표시 여부 |
| `pendingPeriod` | `DataRetentionPeriod?` | 경고 확인 대기 중인 기간 |

**Action: `SettingsAction`**

| 이름 | 설명 |
|------|------|
| `OnRetentionPeriodClicked` | 저장 기간 항목 탭 |
| `OnPeriodSelected(period)` | 라디오 버튼 선택 |
| `OnRetentionDialogDismissed` | 기간 다이얼로그 닫기 |
| `OnUnlimitedWarningConfirmed` | 경고 다이얼로그 확인 |
| `OnUnlimitedWarningDismissed` | 경고 다이얼로그 취소 |

**SideEffect**: 없음 (다이얼로그 상태가 State에 포함)

### Domain Model: `DataRetentionPeriod`

```kotlin
enum class DataRetentionPeriod(val days: Int?) {
    ONE_DAY(1),
    ONE_WEEK(7),
    ONE_MONTH(30),
    UNLIMITED(null)  // days == null → 삭제 안 함
}
```

### 설정 저장
- `SettingsRepository` 인터페이스 → `SettingsRepositoryImpl` (DataStore Preferences)
- Key: `"retention_period"` (String, DataRetentionPeriod.name 직렬화)
- 기본값: `DataRetentionPeriod.ONE_DAY`

### 자동 정리 (`PurgeOldRecordsUseCase`)
- 호출 시점 ① 앱 시작 (`MainActivity.onCreate`)
- 호출 시점 ② 기간 설정 변경 직후
- 로직: `timestamp < (now - days * 86400000)` 인 기록 삭제
- `UNLIMITED`인 경우 삭제 없음

### UseCase
- `GetRetentionPeriodUseCase(): Flow<DataRetentionPeriod>`
- `SetRetentionPeriodUseCase(period: DataRetentionPeriod)`
- `PurgeOldRecordsUseCase()` — 현재 설정 조회 후 초과 기록 삭제

### DAO 추가 쿼리
```sql
DELETE FROM entry_exit_records WHERE timestamp < :beforeTimestamp
```
