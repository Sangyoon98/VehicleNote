package com.sangyoon.vehiclenote.ui.edit

import android.net.Uri

/** 차량 수정 화면 단발성 이벤트 (네비게이션, 카메라/갤러리 실행 등). */
sealed interface EditVehicleSideEffect {
    /** 저장 완료 후 이전 화면으로 복귀. */
    data object NavigateBack : EditVehicleSideEffect
    /** 스낵바 메시지 표시. */
    data class ShowSnackbar(val message: String) : EditVehicleSideEffect
    /** 카메라 앱 실행. outputUri에 촬영 결과가 저장됨. */
    data class LaunchCamera(val outputUri: Uri) : EditVehicleSideEffect
    /** 갤러리(사진 선택기) 실행. */
    data object LaunchGallery : EditVehicleSideEffect
}
