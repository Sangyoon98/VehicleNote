package com.sangyoon.vehiclenote.ui.add

data class AddVehicleState(
    val licensePlate: String = "",
    val ownerName: String = "",
    val department: String = "",
    val phoneNumber: String = "",
    val carModel: String = "",
    val memo: String = "",

    // 유효성 검사 에러
    val licensePlateError: String? = null,
    val ownerNameError: String? = null,
    val departmentError: String? = null,

    // 상태
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
