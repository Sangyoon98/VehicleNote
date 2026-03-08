package com.sangyoon.vehiclenote.ui.entryexit

sealed interface EntryExitSideEffect {
    data class NavigateToDetail(val recordId: Long) : EntryExitSideEffect
    data class ShowSnackbar(val message: String) : EntryExitSideEffect
}
