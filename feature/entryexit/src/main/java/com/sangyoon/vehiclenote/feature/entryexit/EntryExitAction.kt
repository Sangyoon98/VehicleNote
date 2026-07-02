package com.sangyoon.vehiclenote.feature.entryexit

/** 입출차 메인 화면 사용자 액션. */
sealed interface EntryExitAction {
    // 번호판 인식
    /** 카메라가 번호판을 인식함. 확인 다이얼로그 표시. */
    data class PlateDetected(val plate: String) : EntryExitAction
    /** 확인 다이얼로그에서 번호판 텍스트 직접 수정. */
    data class DetectedPlateEdited(val plate: String) : EntryExitAction
    /** 번호판 확인 다이얼로그에서 확정. 입출차 기록 저장 요청. */
    data object PlateConfirmed : EntryExitAction
    /** 번호판 확인 다이얼로그 취소. */
    data object PlateConfirmDismissed : EntryExitAction

    // 수동 입력
    /** 수동 입력 버튼 클릭. 수동 입력 다이얼로그 표시. */
    data object ManualInputClicked : EntryExitAction
    /** 수동 입력 다이얼로그 텍스트 변경. */
    data class ManualPlateChanged(val plate: String) : EntryExitAction
    /** 수동 입력 다이얼로그에서 확정. 입출차 기록 저장 요청. */
    data object ManualInputConfirmed : EntryExitAction
    /** 수동 입력 다이얼로그 취소. */
    data object ManualInputDismissed : EntryExitAction

    // 목록
    /** 입출차 기록 아이템 클릭. */
    data class RecordClicked(val recordId: Long) : EntryExitAction
    /** 전체 로그 목록 화면으로 이동 버튼 클릭. */
    data object LogListClicked : EntryExitAction
}
