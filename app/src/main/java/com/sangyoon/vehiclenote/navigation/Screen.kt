package com.sangyoon.vehiclenote.navigation

/**
 * 앱 내 네비게이션 화면 정의.
 *
 * [NavGraph]의 NavHost에서 사용되는 라우트 문자열을 캡슐화한다.
 * 인수가 있는 화면은 `createRoute()` 함수로 실제 이동 경로를 생성한다.
 */
sealed class Screen(val route: String) {

    /** 홈 화면 (차량 목록 + 통계 대시보드). */
    data object Home : Screen("home")

    /** 차량 등록 화면. [licensePlate]는 입출차 화면에서 미등록 번호판으로 진입 시 전달된다. */
    data object AddVehicle : Screen("add_vehicle?licensePlate={licensePlate}") {
        /**
         * @param licensePlate 초기 번호판 값. 기본값 빈 문자열.
         */
        fun createRoute(licensePlate: String = "") = "add_vehicle?licensePlate=$licensePlate"
    }

    /** 차량 상세 화면. */
    data object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        /**
         * @param vehicleId 조회할 차량 PK.
         */
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }

    /** 차량 수정 화면. */
    data object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        /**
         * @param vehicleId 수정할 차량 PK.
         */
        fun createRoute(vehicleId: Long) = "edit_vehicle/$vehicleId"
    }

    /** 입출차 카메라 화면 (OCR + 수동 입력). */
    data object EntryExit : Screen("entry_exit")

    /** 앱 설정 화면 (보관 기간, CSV 내보내기/가져오기). */
    data object Settings : Screen("settings")

    /** 입출차 기록 상세 화면. */
    data object EntryExitDetail : Screen("entry_exit_detail/{recordId}") {
        /**
         * @param recordId 조회할 기록 PK.
         */
        fun createRoute(recordId: Long) = "entry_exit_detail/$recordId"
    }

    /** 입출차 기록 전체 목록 화면. */
    data object EntryExitLogList : Screen("entry_exit_log_list?filterToday={filterToday}") {
        /**
         * @param filterToday true이면 오늘 기록만 표시.
         */
        fun createRoute(filterToday: Boolean = false) = "entry_exit_log_list?filterToday=$filterToday"
    }
}
