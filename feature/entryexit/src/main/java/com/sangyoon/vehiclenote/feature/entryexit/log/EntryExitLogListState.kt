package com.sangyoon.vehiclenote.feature.entryexit.log

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord

/**
 * 입출차 로그 목록 화면 UI 상태.
 *
 * @property records 표시 중인 입출차 기록 목록 (검색/필터 결과 반영).
 * @property searchQuery 검색창 입력 텍스트.
 * @property isSearchActive 검색창 활성화 여부.
 * @property isLoading 데이터 로딩 중 여부.
 * @property isFilteredToday 오늘 날짜 기록만 표시하는 필터 활성화 여부.
 * @property isExporting CSV 내보내기 처리 중 여부.
 */
data class EntryExitLogListState(
    val records: List<EntryExitRecord> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val isFilteredToday: Boolean = false,
    val isExporting: Boolean = false,
)

/** 액션에 따른 동기 상태 전이. 비동기 작업 결과 반영은 ViewModel에서 수행한다. */
fun EntryExitLogListState.reduce(action: EntryExitLogListAction): EntryExitLogListState =
    when (action) {
        is EntryExitLogListAction.SearchQueryChanged -> copy(searchQuery = action.query)

        is EntryExitLogListAction.SearchActiveChanged ->
            copy(
                isSearchActive = action.active,
                searchQuery = if (!action.active) "" else searchQuery
            )

        EntryExitLogListAction.OnExportClicked -> copy(isExporting = true)

        else -> this
    }
