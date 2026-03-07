package com.sangyoon.vehiclenote.ui.detail

sealed interface VehicleDetailSideEffect {
    data object NavigateBack : VehicleDetailSideEffect
    data class NavigateToEdit(val vehicleId: Long) : VehicleDetailSideEffect
    data class ShowSnackbar(val message: String) : VehicleDetailSideEffect
}
