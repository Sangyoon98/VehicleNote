package com.sangyoon.vehiclenote.ui.detail

import com.sangyoon.vehiclenote.domain.model.Vehicle

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false  // isDeleted/userMessage 제거: SideEffect로 대체
)
