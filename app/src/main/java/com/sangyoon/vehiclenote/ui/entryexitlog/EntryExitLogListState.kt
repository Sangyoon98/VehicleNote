package com.sangyoon.vehiclenote.ui.entryexitlog

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord

data class EntryExitLogListState(
    val records: List<EntryExitRecord> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
)
