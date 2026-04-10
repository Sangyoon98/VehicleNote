package com.sangyoon.vehiclenote.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddVehicle : Screen("add_vehicle?licensePlate={licensePlate}") {
        fun createRoute(licensePlate: String = "") = "add_vehicle?licensePlate=$licensePlate"
    }

    data object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }

    data object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "edit_vehicle/$vehicleId"
    }

    data object EntryExit : Screen("entry_exit")
    data object EntryExitDetail : Screen("entry_exit_detail/{recordId}") {
        fun createRoute(recordId: Long) = "entry_exit_detail/$recordId"
    }
    data object EntryExitLogList : Screen("entry_exit_log_list?filterToday={filterToday}") {
        fun createRoute(filterToday: Boolean = false) = "entry_exit_log_list?filterToday=$filterToday"
    }
}
