package com.sangyoon.vehiclenote.ui.settings

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.model.Vehicle

/**
 * 설정 화면 UI 상태.
 *
 * @property retentionPeriod 현재 적용 중인 입출차 기록 보관 기간.
 * @property showRetentionDialog 보관 기간 선택 다이얼로그 표시 여부.
 * @property showUnlimitedWarningDialog 영구 보관 선택 시 경고 다이얼로그 표시 여부.
 * @property pendingPeriod 경고 다이얼로그 확인 전 임시 저장된 선택 기간.
 * @property isExportingVehicles 차량 CSV 내보내기 처리 중 여부.
 * @property isImportingVehicles 차량 CSV 가져오기 처리 중 여부.
 * @property pendingImportVehicles 파일 파싱 완료 후 최종 확인을 기다리는 차량 목록.
 * @property pendingSkippedRows 파싱 중 형식 오류로 건너뛴 행 수.
 * @property showImportConfirmDialog 가져오기 확인 다이얼로그 표시 여부.
 * @property importResult 가져오기 완료 결과. null이면 완료 전.
 */
data class SettingsState(
    // 입출차 기록 저장 기간
    val retentionPeriod: DataRetentionPeriod = DataRetentionPeriod.ONE_DAY,
    val showRetentionDialog: Boolean = false,
    val showUnlimitedWarningDialog: Boolean = false,
    val pendingPeriod: DataRetentionPeriod? = null,

    // 차량 데이터 내보내기
    val isExportingVehicles: Boolean = false,

    // 차량 데이터 가져오기
    val isImportingVehicles: Boolean = false,
    val pendingImportVehicles: List<Vehicle>? = null,
    val pendingSkippedRows: Int = 0,
    val showImportConfirmDialog: Boolean = false,
    val importResult: VehicleImportResult? = null
)

/**
 * 차량 CSV 가져오기 완료 결과.
 *
 * @property addedCount 새로 추가된 차량 수.
 * @property skippedByDuplicate 번호판 중복으로 건너뛴 차량 수.
 */
data class VehicleImportResult(
    val addedCount: Int,
    val skippedByDuplicate: Int
)

/** 액션에 따른 동기 상태 전이. 비동기 작업 결과 반영은 ViewModel에서 수행한다. */
fun SettingsState.reduce(action: SettingsAction): SettingsState = when (action) {
    SettingsAction.OnRetentionPeriodClicked -> copy(showRetentionDialog = true)

    is SettingsAction.OnPeriodSelected ->
        if (action.period == DataRetentionPeriod.UNLIMITED) {
            copy(
                showRetentionDialog = false,
                showUnlimitedWarningDialog = true,
                pendingPeriod = DataRetentionPeriod.UNLIMITED
            )
        } else {
            copy(showRetentionDialog = false)
        }

    SettingsAction.OnRetentionDialogDismissed -> copy(showRetentionDialog = false)

    SettingsAction.OnUnlimitedWarningConfirmed ->
        copy(showUnlimitedWarningDialog = false, pendingPeriod = null)

    SettingsAction.OnUnlimitedWarningDismissed ->
        copy(showUnlimitedWarningDialog = false, pendingPeriod = null)

    SettingsAction.OnExportVehiclesClicked -> copy(isExportingVehicles = true)

    is SettingsAction.OnImportFilePicked -> copy(isImportingVehicles = true)

    SettingsAction.OnImportConfirmed ->
        copy(showImportConfirmDialog = false, isImportingVehicles = true)

    SettingsAction.OnImportDialogDismissed ->
        copy(
            showImportConfirmDialog = false,
            pendingImportVehicles = null,
            pendingSkippedRows = 0
        )

    SettingsAction.OnImportResultDismissed -> copy(importResult = null)

    else -> this
}
