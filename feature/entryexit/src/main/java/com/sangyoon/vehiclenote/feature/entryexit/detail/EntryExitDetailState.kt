package com.sangyoon.vehiclenote.feature.entryexit.detail

import com.sangyoon.vehiclenote.domain.model.EntryExitRecord
import com.sangyoon.vehiclenote.domain.model.Vehicle

/**
 * 입출차 기록 상세 화면 UI 상태.
 *
 * @property record 표시할 입출차 기록. null이면 로딩 중이거나 조회 실패.
 * @property vehicle 해당 번호판의 등록 차량 정보. null이면 미등록 차량.
 * @property isLoading 데이터 로딩 중 여부.
 * @property error 에러 메시지. null이면 정상.
 */
data class EntryExitDetailState(
    val record: EntryExitRecord? = null,
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
