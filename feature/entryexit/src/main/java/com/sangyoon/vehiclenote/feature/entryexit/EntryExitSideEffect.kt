package com.sangyoon.vehiclenote.feature.entryexit

/** 입출차 메인 화면 단발성 이벤트 (네비게이션, 스낵바 등). */
sealed interface EntryExitSideEffect {
    /** 입출차 기록 상세 화면으로 이동. */
    data class NavigateToDetail(val recordId: Long) : EntryExitSideEffect
    /** 전체 입출차 로그 목록 화면으로 이동. */
    data object NavigateToLogList : EntryExitSideEffect
    /** 스낵바 메시지 표시. */
    data class ShowSnackbar(val message: String) : EntryExitSideEffect
}
