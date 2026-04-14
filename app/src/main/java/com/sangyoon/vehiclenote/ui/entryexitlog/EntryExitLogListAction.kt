package com.sangyoon.vehiclenote.ui.entryexitlog

sealed interface EntryExitLogListAction {
    data class SearchQueryChanged(val query: String) : EntryExitLogListAction
    data class SearchActiveChanged(val active: Boolean) : EntryExitLogListAction
    data class RecordClicked(val recordId: Long) : EntryExitLogListAction
    data object OnExportClicked : EntryExitLogListAction
}
