package com.sangyoon.vehiclenote.ui.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

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