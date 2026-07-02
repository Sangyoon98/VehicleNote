package com.sangyoon.vehiclenote.feature.entryexit.log

import android.net.Uri

/** 입출차 로그 목록 화면 단발성 이벤트 (네비게이션, 파일 공유 등). */
sealed interface EntryExitLogListSideEffect {
    /** 입출차 기록 상세 화면으로 이동. */
    data class NavigateToDetail(val recordId: Long) : EntryExitLogListSideEffect
    /** 생성된 CSV 파일을 공유 인텐트로 전달. */
    data class ShareFile(val uri: Uri, val fileName: String) : EntryExitLogListSideEffect
}
