package com.sangyoon.vehiclenote.feature.vehicle.detail

/** 차량 상세 화면 사용자 액션. */
sealed interface VehicleDetailAction {
    /** 삭제 버튼 클릭. 삭제 확인 다이얼로그 표시. */
    data object ShowDeleteDialog : VehicleDetailAction
    /** 삭제 확인 다이얼로그 취소. */
    data object DismissDeleteDialog : VehicleDetailAction
    /** 삭제 확인 다이얼로그에서 삭제 확정. */
    data object DeleteConfirmed : VehicleDetailAction
    /** 수정 버튼 클릭. */
    data object EditClicked : VehicleDetailAction
}
