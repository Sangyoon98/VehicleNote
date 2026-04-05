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

fun EntryExitState.reduce(action: EntryExitAction): EntryExitState = when (action) {
    is EntryExitAction.PlateDetected ->
        if (!showPlateConfirmDialog) copy(showPlateConfirmDialog = true, detectedPlate = action.plate)
        else this

    is EntryExitAction.DetectedPlateEdited -> copy(detectedPlate = action.plate)

    EntryExitAction.PlateConfirmed ->
        copy(showPlateConfirmDialog = false, detectedPlate = "")

    EntryExitAction.PlateConfirmDismissed ->
        copy(showPlateConfirmDialog = false, detectedPlate = "")

    EntryExitAction.ManualInputClicked ->
        copy(showManualInputDialog = true, manualInputPlate = "")

    is EntryExitAction.ManualPlateChanged -> copy(manualInputPlate = action.plate)

    EntryExitAction.ManualInputConfirmed ->
        copy(showManualInputDialog = false, manualInputPlate = "")

    EntryExitAction.ManualInputDismissed ->
        copy(showManualInputDialog = false, manualInputPlate = "")

    else -> this
}
