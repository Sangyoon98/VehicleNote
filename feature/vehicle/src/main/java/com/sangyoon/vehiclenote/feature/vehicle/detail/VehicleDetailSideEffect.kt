package com.sangyoon.vehiclenote.feature.vehicle.detail

/** 차량 상세 화면 단발성 이벤트 (네비게이션, 스낵바 등). */
sealed interface VehicleDetailSideEffect {
    /** 삭제 완료 또는 뒤로가기 시 이전 화면으로 복귀. */
    data object NavigateBack : VehicleDetailSideEffect
    /** 차량 수정 화면으로 이동. */
    data class NavigateToEdit(val vehicleId: Long) : VehicleDetailSideEffect
    /** 스낵바 메시지 표시. */
    data class ShowSnackbar(val message: String) : VehicleDetailSideEffect
}
