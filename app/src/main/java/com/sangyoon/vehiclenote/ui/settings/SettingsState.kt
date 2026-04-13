package com.sangyoon.vehiclenote.ui.settings

import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod

data class SettingsState(
    val retentionPeriod: DataRetentionPeriod = DataRetentionPeriod.ONE_DAY,
    val showRetentionDialog: Boolean = false,
    val showUnlimitedWarningDialog: Boolean = false,
    val pendingPeriod: DataRetentionPeriod? = null
)
