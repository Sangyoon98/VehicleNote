package com.sangyoon.vehiclenote.ui.add

import android.net.Uri

sealed interface AddVehicleSideEffect {
    data object NavigateBack : AddVehicleSideEffect
    data class ShowSnackbar(val message: String) : AddVehicleSideEffect
    data class LaunchCamera(val outputUri: Uri) : AddVehicleSideEffect
    data object LaunchGallery : AddVehicleSideEffect
}
