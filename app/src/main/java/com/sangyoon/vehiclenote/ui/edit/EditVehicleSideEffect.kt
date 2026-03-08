package com.sangyoon.vehiclenote.ui.edit

import android.net.Uri

sealed interface EditVehicleSideEffect {
    data object NavigateBack : EditVehicleSideEffect
    data class ShowSnackbar(val message: String) : EditVehicleSideEffect
    data class LaunchCamera(val outputUri: Uri) : EditVehicleSideEffect
    data object LaunchGallery : EditVehicleSideEffect
}
