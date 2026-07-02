package com.sangyoon.vehiclenote.feature.vehicle.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

/**
 * 홈 화면 UI 상태.
 *
 * @property vehicles 현재 표시 중인 차량 목록 (검색/필터 결과 포함).
 * @property recentVehicles 최근 등록 차량 목록 (대시보드 표시용).
 * @property searchQuery 검색창 입력 텍스트.
 * @property isSearchActive 검색창 활성화 여부.
 * @property isLoading 데이터 로딩 중 여부.
 * @property error 에러 메시지. null이면 정상.
 * @property totalVehicleCount 전체 등록 차량 수 (vehicles에서 계산).
 * @property todayRegisteredCount 오늘 등록된 차량 수.
 * @property departmentStats 부서별 차량 수 통계 (부서명 → 차량 수).
 * @property departmentList 필터 칩에 표시할 부서 목록.
 * @property selectedDepartment 현재 선택된 부서 필터. null이면 전체 표시.
 */
data class HomeState(
    val vehicles: List<Vehicle> = emptyList(),
    val recentVehicles: List<Vehicle> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    // 통계 (ViewModel에서 vehicles로부터 계산)
    val totalVehicleCount: Int = 0,
    val todayRegisteredCount: Int = 0,
    val departmentStats: Map<String, Int> = emptyMap(),

    // 부서 필터
    val departmentList: List<String> = emptyList(),
    val selectedDepartment: String? = null
)

fun HomeState.reduce(action: HomeAction): HomeState = when (action) {
    is HomeAction.SearchQueryChanged -> copy(searchQuery = action.query)
    is HomeAction.SearchActiveChanged ->
        copy(
            isSearchActive = action.isActive,
            searchQuery = if (!action.isActive) "" else searchQuery
        )
    is HomeAction.DepartmentFilterSelected -> copy(selectedDepartment = action.department)
    else -> this
}