package com.sangyoon.vehiclenote.ui.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

data class HomeState(
    val vehicles: List<Vehicle> = emptyList(),
    val recentVehicles: List<Vehicle> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error : String? = null,
    val totalVehicleCount: Int = 0,
    val todayRegisteredCount: Int = 0,
    val departmentStats: Map<String, Int> = emptyMap()
)