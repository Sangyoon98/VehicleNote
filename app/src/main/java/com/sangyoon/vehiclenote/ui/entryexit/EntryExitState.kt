package com.sangyoon.vehiclenote.ui.entryexit

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord

/**
 * 입출차 메인 화면 UI 상태.
 *
 * @property records 최근 입출차 기록 목록.
 * @property isLoading 데이터 로딩 중 여부.
 * @property error 에러 메시지. null이면 정상.
 * @property showPlateConfirmDialog 번호판 인식 결과 확인 다이얼로그 표시 여부.
 * @property detectedPlate 카메라로 인식된 번호판 문자열 (확인 다이얼로그에 표시).
 * @property showManualInputDialog 번호판 수동 입력 다이얼로그 표시 여부.
 * @property manualInputPlate 수동 입력 다이얼로그의 입력 텍스트.
 */
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
