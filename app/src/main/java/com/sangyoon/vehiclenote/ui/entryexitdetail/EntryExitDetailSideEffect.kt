package com.sangyoon.vehiclenote.ui.entryexitdetail

sealed interface EntryExitDetailSideEffect {
    data object NavigateBack : EntryExitDetailSideEffect
    data class NavigateToAddVehicle(val licensePlate: String) : EntryExitDetailSideEffect
}
