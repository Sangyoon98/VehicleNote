package com.sangyoon.vehiclenote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sangyoon.vehiclenote.navigation.Screen
import com.sangyoon.vehiclenote.ui.theme.VnInk
import com.sangyoon.vehiclenote.ui.theme.VnInkMute
import com.sangyoon.vehiclenote.ui.theme.VnLine
import com.sangyoon.vehiclenote.ui.theme.VnPaperDeep
import com.sangyoon.vehiclenote.ui.theme.VnSurface
import com.sangyoon.vehiclenote.ui.theme.VnTypeCaption

private data class TabItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
)

private val tabs = listOf(
    TabItem(Screen.Home.route,      Icons.Filled.DirectionsCar, "차량"),
    TabItem(Screen.EntryExit.route, Icons.Filled.History,       "입출차"),
    TabItem(Screen.Settings.route,  Icons.Filled.Settings,      "설정"),
)

/**
 * VN 브랜드 하단 탭 바.
 * 선택 탭: VnPaperDeep pill + VnLine 1dp 테두리. 레이블 11sp/600.
 * 비선택 탭: 아이콘 + 레이블 VnInkMute, 패딩만.
 */
@Composable
fun VnBottomTabs(
    currentRoute: String?,
    navController: NavController,
) {
    NavigationBar(
        containerColor = VnSurface,
        contentColor = VnInk,
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            // 상단 1dp 구분선 (elevation 대신 border)
            val stroke = 1.dp.toPx()
            drawLine(
                color = VnLine,
                start = Offset(0f, stroke / 2),
                end = Offset(size.width, stroke / 2),
                strokeWidth = stroke,
            )
        },
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "pill_alpha_${tab.route}",
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                VnPaperDeep.copy(alpha = pillAlpha),
                                RoundedCornerShape(999.dp),
                            )
                            .border(
                                1.dp,
                                VnLine.copy(alpha = pillAlpha),
                                RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        style = VnTypeCaption,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,  // M3 기본 oval 인디케이터 제거
                    selectedIconColor = VnInk,
                    unselectedIconColor = VnInkMute,
                    selectedTextColor = VnInk,
                    unselectedTextColor = VnInkMute,
                ),
            )
        }
    }
}
