package com.sangyoon.vehiclenote.feature.vehicle.add

import android.net.Uri

/** 차량 등록 화면 단발성 이벤트 (네비게이션, 카메라/갤러리 실행 등). */
sealed interface AddVehicleSideEffect {
    /** 저장 완료 후 이전 화면으로 복귀. */
    data object NavigateBack : AddVehicleSideEffect
    /** 스낵바 메시지 표시. */
    data class ShowSnackbar(val message: String) : AddVehicleSideEffect
    /** 카메라 앱 실행. outputUri에 촬영 결과가 저장됨. */
    data class LaunchCamera(val outputUri: Uri) : AddVehicleSideEffect
    /** 갤러리(사진 선택기) 실행. */
    data object LaunchGallery : AddVehicleSideEffect
}
