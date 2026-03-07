package com.sangyoon.vehiclenote.ui.edit

sealed interface EditVehicleSideEffect {
    data object NavigateBack : EditVehicleSideEffect
    data class ShowSnackbar(val message: String) : EditVehicleSideEffect
}
