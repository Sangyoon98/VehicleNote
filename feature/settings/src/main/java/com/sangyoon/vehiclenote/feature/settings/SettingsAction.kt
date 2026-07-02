package com.sangyoon.vehiclenote.feature.settings

import android.net.Uri
import com.sangyoon.vehiclenote.domain.model.DataRetentionPeriod

/** 설정 화면 사용자 액션. */
sealed interface SettingsAction {
    // 입출차 기록 저장 기간
    /** 보관 기간 항목 클릭. 선택 다이얼로그 표시. */
    data object OnRetentionPeriodClicked : SettingsAction
    /** 보관 기간 선택. UNLIMITED이면 경고 다이얼로그를 먼저 표시. */
    data class OnPeriodSelected(val period: DataRetentionPeriod) : SettingsAction
    /** 보관 기간 선택 다이얼로그 취소. */
    data object OnRetentionDialogDismissed : SettingsAction
    /** 영구 보관 경고 다이얼로그 확인. */
    data object OnUnlimitedWarningConfirmed : SettingsAction
    /** 영구 보관 경고 다이얼로그 취소. */
    data object OnUnlimitedWarningDismissed : SettingsAction

    // 차량 데이터 내보내기/가져오기
    /** 차량 데이터 내보내기(CSV) 버튼 클릭. */
    data object OnExportVehiclesClicked : SettingsAction
    /** 차량 데이터 가져오기 버튼 클릭. 파일 선택기 실행. */
    data object OnImportVehiclesClicked : SettingsAction
    /** 파일 선택기에서 CSV 파일 선택 완료. */
    data class OnImportFilePicked(val uri: Uri) : SettingsAction
    /** 가져오기 확인 다이얼로그에서 확정. DB 저장 요청. */
    data object OnImportConfirmed : SettingsAction
    /** 가져오기 확인 다이얼로그 취소. */
    data object OnImportDialogDismissed : SettingsAction
    /** 가져오기 결과 다이얼로그 닫기. */
    data object OnImportResultDismissed : SettingsAction
}
