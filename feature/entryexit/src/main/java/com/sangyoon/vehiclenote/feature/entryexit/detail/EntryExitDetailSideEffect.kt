package com.sangyoon.vehiclenote.feature.entryexit.detail

/** 입출차 기록 상세 화면 단발성 이벤트 (네비게이션 등). */
sealed interface EntryExitDetailSideEffect {
    /** 이전 화면으로 복귀. */
    data object NavigateBack : EntryExitDetailSideEffect
    /** 차량 등록 화면으로 이동. licensePlate가 등록 폼에 자동 입력됨. */
    data class NavigateToAddVehicle(val licensePlate: String) : EntryExitDetailSideEffect
}
