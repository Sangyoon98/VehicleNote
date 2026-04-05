package com.sangyoon.vehiclenote.ui.add

import com.sangyoon.vehiclenote.domain.model.CustomField

data class AddVehicleState(
    val licensePlate: String = "",
    val ownerName: String = "",
    val department: String = "",
    val phoneNumber: String = "",
    val carModel: String = "",
    val memo: String = "",
    val photoPath: String? = null,
    val pendingCameraFilePath: String? = null,
    val previousPhotoPath: String? = null,
    val customFields: List<CustomField> = emptyList(),
    val showPhotoSourceDialog: Boolean = false,
    val licensePlateError: String? = null,
    val ownerNameError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

fun AddVehicleState.reduce(action: AddVehicleAction): AddVehicleState = when (action) {
    is AddVehicleAction.LicensePlateChanged ->
        copy(licensePlate = action.value, licensePlateError = null)

    is AddVehicleAction.OwnerNameChanged ->
        copy(ownerName = action.value, ownerNameError = null)

    is AddVehicleAction.DepartmentChanged -> copy(department = action.value)
    is AddVehicleAction.PhoneNumberChanged -> copy(phoneNumber = action.value)
    is AddVehicleAction.CarModelChanged -> copy(carModel = action.value)
    is AddVehicleAction.MemoChanged -> copy(memo = action.value)

    AddVehicleAction.PhotoClicked -> copy(showPhotoSourceDialog = true)
    AddVehicleAction.PhotoSourceDialogDismissed -> copy(showPhotoSourceDialog = false)
    AddVehicleAction.GallerySelected -> copy(showPhotoSourceDialog = false)

    AddVehicleAction.AddCustomField ->
        copy(customFields = customFields + CustomField("", ""))

    is AddVehicleAction.RemoveCustomField ->
        copy(customFields = customFields.toMutableList().also { it.removeAt(action.index) })

    is AddVehicleAction.CustomFieldKeyChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(key = action.key)
        })

    is AddVehicleAction.CustomFieldValueChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(value = action.value)
        })

    else -> this
}
