package com.sangyoon.vehiclenote.ui.add

sealed interface AddVehicleSideEffect {
    data object NavigateBack : AddVehicleSideEffect
    data class ShowSnackbar(val message: String) : AddVehicleSideEffect
}
