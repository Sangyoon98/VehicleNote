package com.sangyoon.vehiclenote.ui.edit

sealed interface EditVehicleIntent {
    data class LicensePlateChanged(val value: String) : EditVehicleIntent
    data class OwnerNameChanged(val value: String) : EditVehicleIntent
    data class DepartmentChanged(val value: String) : EditVehicleIntent
    data class PhoneNumberChanged(val value: String) : EditVehicleIntent
    data class CarModelChanged(val value: String) : EditVehicleIntent
    data class MemoChanged(val value: String) : EditVehicleIntent
    data object SaveClicked : EditVehicleIntent
}