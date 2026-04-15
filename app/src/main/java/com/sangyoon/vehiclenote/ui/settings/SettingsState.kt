package com.sangyoon.vehiclenote.ui.settings

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod
import com.sangyoon.vehiclenote.domain.model.Vehicle

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
    val pendingImportVehicles: List<Vehicle>? = null, // 파싱 완료 후 확인 대기
    val pendingSkippedRows: Int = 0,
    val showImportConfirmDialog: Boolean = false,
    val importResult: VehicleImportResult? = null
)

data class VehicleImportResult(
    val addedCount: Int,
    val skippedByDuplicate: Int
)
