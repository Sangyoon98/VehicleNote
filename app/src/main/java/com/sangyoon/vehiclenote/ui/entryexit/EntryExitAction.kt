package com.sangyoon.vehiclenote.ui.entryexit

sealed interface EntryExitAction {
    // 검색
    data object SearchToggled : EntryExitAction
    data class SearchQueryChanged(val query: String) : EntryExitAction
    data object SearchDismissed : EntryExitAction

    // 번호판 인식
    data class PlateDetected(val plate: String) : EntryExitAction
    data class DetectedPlateEdited(val plate: String) : EntryExitAction
    data object PlateConfirmed : EntryExitAction
    data object PlateConfirmDismissed : EntryExitAction

    // 수동 입력
    data object ManualInputClicked : EntryExitAction
    data class ManualPlateChanged(val plate: String) : EntryExitAction
    data object ManualInputConfirmed : EntryExitAction
    data object ManualInputDismissed : EntryExitAction

    // 목록
    data class RecordClicked(val recordId: Long) : EntryExitAction
}
