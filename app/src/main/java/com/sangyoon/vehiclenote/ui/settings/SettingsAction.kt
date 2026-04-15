package com.sangyoon.vehiclenote.ui.settings

import android.net.Uri
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod

sealed interface SettingsAction {
    // 입출차 기록 저장 기간
    data object OnRetentionPeriodClicked : SettingsAction
    data class OnPeriodSelected(val period: DataRetentionPeriod) : SettingsAction
    data object OnRetentionDialogDismissed : SettingsAction
    data object OnUnlimitedWarningConfirmed : SettingsAction
    data object OnUnlimitedWarningDismissed : SettingsAction

    // 차량 데이터 내보내기/가져오기
    data object OnExportVehiclesClicked : SettingsAction
    data object OnImportVehiclesClicked : SettingsAction
    data class OnImportFilePicked(val uri: Uri) : SettingsAction
    data object OnImportConfirmed : SettingsAction
    data object OnImportDialogDismissed : SettingsAction
    data object OnImportResultDismissed : SettingsAction
}
