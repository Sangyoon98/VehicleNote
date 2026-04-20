package com.sangyoon.vehiclenote.ui.home

/** 홈 화면 단발성 이벤트 (네비게이션, 스낵바 등). */
sealed interface HomeSideEffect {
    /** 차량 등록 화면으로 이동. */
    data object NavigateToAdd : HomeSideEffect
    /** 차량 상세 화면으로 이동. */
    data class NavigateToDetail(val vehicleId: Long) : HomeSideEffect
    /** 스낵바 메시지 표시. */
    data class ShowSnackbar(val message: String) : HomeSideEffect
    /** 부서 필터 칩 영역으로 스크롤. */
    data object ScrollToFilter : HomeSideEffect
}
