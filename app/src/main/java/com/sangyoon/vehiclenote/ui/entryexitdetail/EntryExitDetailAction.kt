package com.sangyoon.vehiclenote.ui.entryexitdetail

sealed interface EntryExitDetailAction {
    data object NavigateBackClicked : EntryExitDetailAction
    data object RegisterVehicleClicked : EntryExitDetailAction
}
