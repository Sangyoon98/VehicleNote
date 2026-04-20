package com.sangyoon.vehiclenote.ui.detail

import com.sangyoon.vehiclenote.domain.model.Vehicle

/**
 * 차량 상세 화면 UI 상태.
 *
 * @property vehicle 표시할 차량 정보. null이면 로딩 중이거나 조회 실패.
 * @property isLoading 데이터 로딩 중 여부.
 * @property error 에러 메시지. null이면 정상.
 * @property showDeleteDialog 삭제 확인 다이얼로그 표시 여부.
 */
data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)
