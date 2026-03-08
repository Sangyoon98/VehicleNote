package com.sangyoon.vehiclenote.ui.edit

import android.net.Uri

sealed interface EditVehicleAction {
    data class LicensePlateChanged(val value: String) : EditVehicleAction
    data class OwnerNameChanged(val value: String) : EditVehicleAction
    data class DepartmentChanged(val value: String) : EditVehicleAction
    data class PhoneNumberChanged(val value: String) : EditVehicleAction
    data class CarModelChanged(val value: String) : EditVehicleAction
    data class MemoChanged(val value: String) : EditVehicleAction
    data object SaveClicked : EditVehicleAction

    // 사진 관련
    data object PhotoClicked : EditVehicleAction
    data object CameraSelected : EditVehicleAction
    data object GallerySelected : EditVehicleAction
    data class CameraResultReceived(val success: Boolean) : EditVehicleAction
    data class GalleryResultReceived(val uri: Uri?) : EditVehicleAction
    data object PhotoSourceDialogDismissed : EditVehicleAction
    data object PhotoRemoved : EditVehicleAction
    data object CameraPermissionDenied : EditVehicleAction  // 권한 거부 시

    // 커스텀 필드 관련
    data object AddCustomField : EditVehicleAction
    data class RemoveCustomField(val index: Int) : EditVehicleAction
    data class CustomFieldKeyChanged(val index: Int, val key: String) : EditVehicleAction
    data class CustomFieldValueChanged(val index: Int, val value: String) : EditVehicleAction
}
