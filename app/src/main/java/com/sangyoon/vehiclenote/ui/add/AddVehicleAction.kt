package com.sangyoon.vehiclenote.ui.add

sealed interface AddVehicleAction {
    data class LicensePlateChanged(val value: String) : AddVehicleAction
    data class OwnerNameChanged(val value: String) : AddVehicleAction
    data class DepartmentChanged(val value: String) : AddVehicleAction
    data class PhoneNumberChanged(val value: String) : AddVehicleAction
    data class CarModelChanged(val value: String) : AddVehicleAction
    data class MemoChanged(val value: String) : AddVehicleAction
    data object SaveClicked : AddVehicleAction
}
