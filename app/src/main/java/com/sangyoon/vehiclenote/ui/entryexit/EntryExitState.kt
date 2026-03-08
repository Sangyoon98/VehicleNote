package com.sangyoon.vehiclenote.ui.entryexit

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord

data class EntryExitState(
    val records: List<EntryExitRecord> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    // 번호판 확인 다이얼로그
    val showPlateConfirmDialog: Boolean = false,
    val detectedPlate: String = "",
    // 수동 입력 다이얼로그
    val showManualInputDialog: Boolean = false,
    val manualInputPlate: String = ""
)
