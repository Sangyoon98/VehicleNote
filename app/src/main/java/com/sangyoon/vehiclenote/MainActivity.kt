package com.sangyoon.vehiclenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sangyoon.vehiclenote.navigation.NavGraph
import com.sangyoon.vehiclenote.navigation.Screen
import com.sangyoon.vehiclenote.ui.components.BottomNavigationBar
import com.sangyoon.vehiclenote.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // BottomBar는 루트 탭 화면에서만 표시
                val rootRoutes = setOf(Screen.Home.route, Screen.EntryExit.route)
                val showBottomBar = currentRoute in rootRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                navController = navController
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
