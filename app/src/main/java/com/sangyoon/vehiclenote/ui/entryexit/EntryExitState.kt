package com.sangyoon.vehiclenote.ui.entryexit

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord

data class EntryExitState(
    val records: List<EntryExitRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPlateConfirmDialog: Boolean = false,
    val detectedPlate: String = "",
    val showManualInputDialog: Boolean = false,
    val manualInputPlate: String = ""
)
