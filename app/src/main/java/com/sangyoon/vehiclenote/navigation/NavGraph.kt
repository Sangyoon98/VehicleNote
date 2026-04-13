package com.sangyoon.vehiclenote.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sangyoon.vehiclenote.ui.add.AddVehicleScreen
import com.sangyoon.vehiclenote.ui.detail.VehicleDetailScreen
import com.sangyoon.vehiclenote.ui.edit.EditVehicleScreen
import com.sangyoon.vehiclenote.ui.entryexit.EntryExitScreen
import com.sangyoon.vehiclenote.ui.entryexitdetail.EntryExitDetailScreen
import com.sangyoon.vehiclenote.ui.entryexitlog.EntryExitLogListScreen
import com.sangyoon.vehiclenote.ui.home.HomeScreen
import com.sangyoon.vehiclenote.ui.settings.SettingsScreen

private const val TRANSITION_DURATION = 300

@Composable
fun NavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(tween(TRANSITION_DURATION))
        },
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                parentContentPadding = contentPadding,
                onNavigateToAdd = { navController.navigate(Screen.AddVehicle.createRoute()) },
                onNavigateToDetail = { vehicleId ->
                    navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                },
                onNavigateToTodayRecords = {
                    navController.navigate(Screen.EntryExitLogList.createRoute(filterToday = true))
                }
            )
        }

        composable(
            route = Screen.AddVehicle.route,
            arguments = listOf(
                navArgument("licensePlate") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
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

        composable(Screen.EntryExit.route) {
            EntryExitScreen(
                parentContentPadding = contentPadding,
                onNavigateToDetail = { recordId ->
                    navController.navigate(Screen.EntryExitDetail.createRoute(recordId))
                },
                onNavigateToLogList = {
                    navController.navigate(Screen.EntryExitLogList.createRoute())
                }
            )
        }

        composable(
            route = Screen.EntryExitLogList.route,
            arguments = listOf(
                navArgument("filterToday") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            EntryExitLogListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { recordId ->
                    navController.navigate(Screen.EntryExitDetail.createRoute(recordId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(parentContentPadding = contentPadding)
        }

        composable(
            route = Screen.EntryExitDetail.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) {
            EntryExitDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddVehicle = { licensePlate ->
                    navController.navigate(Screen.AddVehicle.createRoute(licensePlate))
                }
            )
        }
    }
}
