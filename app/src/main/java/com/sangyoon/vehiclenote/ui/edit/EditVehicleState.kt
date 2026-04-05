package com.sangyoon.vehiclenote.ui.edit

import com.sangyoon.vehiclenote.domain.model.CustomField

data class EditVehicleState(
    val vehicleId: Long = 0,
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

fun EditVehicleState.reduce(action: EditVehicleAction): EditVehicleState = when (action) {
    is EditVehicleAction.LicensePlateChanged ->
        copy(licensePlate = action.value, licensePlateError = null)

    is EditVehicleAction.OwnerNameChanged ->
        copy(ownerName = action.value, ownerNameError = null)

    is EditVehicleAction.DepartmentChanged -> copy(department = action.value)
    is EditVehicleAction.PhoneNumberChanged -> copy(phoneNumber = action.value)
    is EditVehicleAction.CarModelChanged -> copy(carModel = action.value)
    is EditVehicleAction.MemoChanged -> copy(memo = action.value)

    EditVehicleAction.PhotoClicked -> copy(showPhotoSourceDialog = true)
    EditVehicleAction.PhotoSourceDialogDismissed -> copy(showPhotoSourceDialog = false)
    EditVehicleAction.GallerySelected -> copy(showPhotoSourceDialog = false)

    EditVehicleAction.AddCustomField ->
        copy(customFields = customFields + CustomField("", ""))

    is EditVehicleAction.RemoveCustomField ->
        copy(customFields = customFields.toMutableList().also { it.removeAt(action.index) })

    is EditVehicleAction.CustomFieldKeyChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(key = action.key)
        })

    is EditVehicleAction.CustomFieldValueChanged ->
        copy(customFields = customFields.toMutableList().also {
            it[action.index] = it[action.index].copy(value = action.value)
        })

    else -> this
}
