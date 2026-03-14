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
    val departmentStats: Map<String, Int> = emptyMap()
)