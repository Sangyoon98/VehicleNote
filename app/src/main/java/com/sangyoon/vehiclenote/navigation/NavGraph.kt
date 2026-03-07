package com.sangyoon.vehiclenote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sangyoon.vehiclenote.ui.add.AddVehicleScreen
import com.sangyoon.vehiclenote.ui.detail.VehicleDetailScreen
import com.sangyoon.vehiclenote.ui.edit.EditVehicleScreen
import com.sangyoon.vehiclenote.ui.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddVehicle.route) },
                onNavigateToDetail = { vehicleId ->
                    navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                }
            )
        }

        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.VehicleDetail.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) {
            VehicleDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditVehicle.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
        ) {
            EditVehicleScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
