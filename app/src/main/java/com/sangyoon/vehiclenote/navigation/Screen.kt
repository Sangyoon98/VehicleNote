package com.sangyoon.vehiclenote.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddVehicle : Screen("add_vehicle")
    data object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }
    data object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "edit_vehicle/$vehicleId"
    }
}