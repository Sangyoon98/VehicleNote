package com.sangyoon.vehiclenote.ui.detail

sealed interface VehicleDetailIntent {
    data object DeleteClicked : VehicleDetailIntent
    data object EditClicked : VehicleDetailIntent
    data object MessageShown : VehicleDetailIntent
}