package com.sangyoon.vehiclenote.ui.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

sealed interface HomeAction {
    data class SearchQueryChanged(val query: String) : HomeAction
    data class SearchActiveChanged(val isActive: Boolean) : HomeAction
    data class DeleteVehicle(val vehicle: Vehicle) : HomeAction
    data object AddVehicleClicked : HomeAction
    data class VehicleClicked(val vehicleId: Long) : HomeAction
    data object Refresh : HomeAction
    data class DepartmentFilterSelected(val department: String?) : HomeAction
}
