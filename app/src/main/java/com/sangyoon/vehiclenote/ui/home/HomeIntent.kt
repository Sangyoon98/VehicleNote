package com.sangyoon.vehiclenote.ui.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

sealed interface HomeIntent {
    data class SearchQueryChanged(val query: String) : HomeIntent
    data class SearchActiveChanged(val isActive: Boolean) : HomeIntent
    data class DeleteVehicle(val vehicle: Vehicle) : HomeIntent
    data object AddVehicleClicked : HomeIntent
    data class VehicleClicked(val vehicleId: Long) : HomeIntent
    data object Refresh : HomeIntent
}