package com.sangyoon.vehiclenote.ui.edit

sealed interface EditVehicleAction {
    data class LicensePlateChanged(val value: String) : EditVehicleAction
    data class OwnerNameChanged(val value: String) : EditVehicleAction
    data class DepartmentChanged(val value: String) : EditVehicleAction
    data class PhoneNumberChanged(val value: String) : EditVehicleAction
    data class CarModelChanged(val value: String) : EditVehicleAction
    data class MemoChanged(val value: String) : EditVehicleAction
    data object SaveClicked : EditVehicleAction
}
