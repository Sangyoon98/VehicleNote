package com.sangyoon.vehiclenote.ui.entryexitlog

/** 입출차 로그 목록 화면 사용자 액션. */
sealed interface EntryExitLogListAction {
    /** 검색어 변경. */
    data class SearchQueryChanged(val query: String) : EntryExitLogListAction
    /** 검색창 활성/비활성 전환. */
    data class SearchActiveChanged(val active: Boolean) : EntryExitLogListAction
    /** 기록 아이템 클릭. */
    data class RecordClicked(val recordId: Long) : EntryExitLogListAction
    /** CSV 내보내기 버튼 클릭. */
    data object OnExportClicked : EntryExitLogListAction
}
