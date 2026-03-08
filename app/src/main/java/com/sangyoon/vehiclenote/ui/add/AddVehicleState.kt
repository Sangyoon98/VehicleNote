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
