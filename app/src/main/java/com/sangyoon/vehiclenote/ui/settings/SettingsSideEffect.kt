package com.sangyoon.vehiclenote.ui.settings

import android.net.Uri

sealed interface SettingsSideEffect {
    data class ShareVehicleCsv(val uri: Uri, val fileName: String) : SettingsSideEffect
    data object LaunchVehicleFilePicker : SettingsSideEffect
}
