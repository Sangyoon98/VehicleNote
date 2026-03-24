package com.sangyoon.vehiclenote.ui.entryexitlog

sealed interface EntryExitLogListSideEffect {
    data class NavigateToDetail(val recordId: Long) : EntryExitLogListSideEffect
}
