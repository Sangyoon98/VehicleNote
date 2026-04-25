package com.sangyoon.vehiclenote.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.sangyoon.vehiclenote.ui.theme.VnInk
import com.sangyoon.vehiclenote.ui.theme.VnPaper
import com.sangyoon.vehiclenote.ui.theme.VnTypeBodyLg

/**
 * 앱 전체 공통 상단 바. 15sp/700 타이틀, VnPaper 배경, 0 elevation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VnTopBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = VnTypeBodyLg.copy(fontWeight = FontWeight.Bold),
                color = VnInk,
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = VnInk,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VnPaper,
            scrolledContainerColor = VnPaper,
            navigationIconContentColor = VnInk,
            titleContentColor = VnInk,
            actionIconContentColor = VnInk,
        ),
    )
}
