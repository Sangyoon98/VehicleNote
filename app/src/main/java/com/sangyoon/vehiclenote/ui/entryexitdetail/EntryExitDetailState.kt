package com.sangyoon.vehiclenote.ui.entryexitdetail

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.Vehicle

data class EntryExitDetailState(
    val record: EntryExitRecord? = null,
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
