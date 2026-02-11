package com.sangyoon.vehiclenote.ui.add

sealed interface AddVehicleIntent {
    data class LicensePlateChanged(val value: String) : AddVehicleIntent
    data class OwnerNameChanged(val value: String) : AddVehicleIntent
    data class DepartmentChanged(val value: String) : AddVehicleIntent
    data class PhoneNumberChanged(val value: String) : AddVehicleIntent
    data class CarModelChanged(val value: String) : AddVehicleIntent
    data class MemoChanged(val value: String) : AddVehicleIntent
    data object SaveClicked : AddVehicleIntent
}