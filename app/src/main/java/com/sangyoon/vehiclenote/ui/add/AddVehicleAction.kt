package com.sangyoon.vehiclenote.ui.add

import android.net.Uri

sealed interface AddVehicleAction {
    data class LicensePlateChanged(val value: String) : AddVehicleAction
    data class OwnerNameChanged(val value: String) : AddVehicleAction
    data class DepartmentChanged(val value: String) : AddVehicleAction
    data class PhoneNumberChanged(val value: String) : AddVehicleAction
    data class CarModelChanged(val value: String) : AddVehicleAction
    data class MemoChanged(val value: String) : AddVehicleAction
    data object SaveClicked : AddVehicleAction

    // 사진 관련
    data object PhotoClicked : AddVehicleAction
    data object CameraSelected : AddVehicleAction
    data object GallerySelected : AddVehicleAction
    data class CameraResultReceived(val success: Boolean) : AddVehicleAction
    data class GalleryResultReceived(val uri: Uri?) : AddVehicleAction
    data object PhotoSourceDialogDismissed : AddVehicleAction
    data object PhotoRemoved : AddVehicleAction
    data object CameraPermissionDenied : AddVehicleAction  // 권한 거부 시

    // 커스텀 필드 관련
    data object AddCustomField : AddVehicleAction
    data class RemoveCustomField(val index: Int) : AddVehicleAction
    data class CustomFieldKeyChanged(val index: Int, val key: String) : AddVehicleAction
    data class CustomFieldValueChanged(val index: Int, val value: String) : AddVehicleAction
}
