package com.sangyoon.vehiclenote.ui.settings

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod

sealed interface SettingsAction {
    data object OnRetentionPeriodClicked : SettingsAction
    data class OnPeriodSelected(val period: DataRetentionPeriod) : SettingsAction
    data object OnRetentionDialogDismissed : SettingsAction
    data object OnUnlimitedWarningConfirmed : SettingsAction
    data object OnUnlimitedWarningDismissed : SettingsAction
}
