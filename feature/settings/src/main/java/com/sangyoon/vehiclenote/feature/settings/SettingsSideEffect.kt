package com.sangyoon.vehiclenote.feature.settings

import android.net.Uri

/** 설정 화면 단발성 이벤트 (파일 공유, 파일 선택기 실행 등). */
sealed interface SettingsSideEffect {
    /** 생성된 차량 CSV 파일을 공유 인텐트로 전달. */
    data class ShareVehicleCsv(val uri: Uri, val fileName: String) : SettingsSideEffect
    /** 차량 CSV 가져오기용 파일 선택기 실행. */
    data object LaunchVehicleFilePicker : SettingsSideEffect
}
