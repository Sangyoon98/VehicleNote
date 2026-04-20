package com.sangyoon.vehiclenote.ui.home

import com.sangyoon.vehiclenote.domain.model.Vehicle

/** 홈 화면 사용자 액션. */
sealed interface HomeAction {
    /** 검색어가 변경됨. */
    data class SearchQueryChanged(val query: String) : HomeAction
    /** 검색창 활성/비활성 전환. 비활성화 시 검색어가 초기화됨. */
    data class SearchActiveChanged(val isActive: Boolean) : HomeAction
    /** 차량 삭제 요청. */
    data class DeleteVehicle(val vehicle: Vehicle) : HomeAction
    /** 차량 등록 버튼 클릭. */
    data object AddVehicleClicked : HomeAction
    /** 차량 목록 아이템 클릭. */
    data class VehicleClicked(val vehicleId: Long) : HomeAction
    /** 목록 새로고침 요청. */
    data object Refresh : HomeAction
    /** 부서 필터 칩 선택. null이면 전체 표시. */
    data class DepartmentFilterSelected(val department: String?) : HomeAction
}
