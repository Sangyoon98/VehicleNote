package com.sangyoon.vehiclenote.feature.entryexit.detail

/** 입출차 기록 상세 화면 사용자 액션. */
sealed interface EntryExitDetailAction {
    /** 뒤로가기 버튼 클릭. */
    data object NavigateBackClicked : EntryExitDetailAction
    /** 미등록 차량 등록하기 버튼 클릭. 차량 등록 화면으로 이동. */
    data object RegisterVehicleClicked : EntryExitDetailAction
}
