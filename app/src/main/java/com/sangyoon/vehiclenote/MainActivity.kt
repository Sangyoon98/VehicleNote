package com.sangyoon.vehiclenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sangyoon.vehiclenote.domain.usecase.PurgeOldRecordsUseCase
import com.sangyoon.vehiclenote.navigation.NavGraph
import com.sangyoon.vehiclenote.navigation.Screen
import com.sangyoon.vehiclenote.ui.components.VnBottomTabs
import com.sangyoon.vehiclenote.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var purgeOldRecordsUseCase: PurgeOldRecordsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { purgeOldRecordsUseCase() }
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val rootRoutes = setOf(Screen.Home.route, Screen.EntryExit.route, Screen.Settings.route)
                val showBottomBar = currentRoute in rootRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(tween(300)) { it },
                            exit = slideOutVertically(tween(300)) { it },
                        ) {
                            VnBottomTabs(
                                currentRoute = currentRoute,
                                navController = navController,
                            )
                        }
                    }
                ) { innerPadding: PaddingValues ->
                    NavGraph(
                        navController = navController,
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }
}
