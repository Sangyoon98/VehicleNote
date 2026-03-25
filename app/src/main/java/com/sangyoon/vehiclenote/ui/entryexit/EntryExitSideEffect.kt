package com.sangyoon.vehiclenote.ui.entryexit

sealed interface EntryExitSideEffect {
    data class NavigateToDetail(val recordId: Long) : EntryExitSideEffect
    data object NavigateToLogList : EntryExitSideEffect
    data class ShowSnackbar(val message: String) : EntryExitSideEffect
}
