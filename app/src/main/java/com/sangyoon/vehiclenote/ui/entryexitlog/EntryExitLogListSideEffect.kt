package com.sangyoon.vehiclenote.ui.entryexitlog

import android.net.Uri

sealed interface EntryExitLogListSideEffect {
    data class NavigateToDetail(val recordId: Long) : EntryExitLogListSideEffect
    data class ShareFile(val uri: Uri, val fileName: String) : EntryExitLogListSideEffect
}
