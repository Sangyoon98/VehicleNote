package com.sangyoon.vehiclenote.ui.detail

sealed interface VehicleDetailAction {
    data object ShowDeleteDialog : VehicleDetailAction
    data object DismissDeleteDialog : VehicleDetailAction
    data object DeleteConfirmed : VehicleDetailAction
    data object EditClicked : VehicleDetailAction
}
