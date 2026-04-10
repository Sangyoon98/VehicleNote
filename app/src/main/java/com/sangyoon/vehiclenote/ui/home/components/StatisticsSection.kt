package com.sangyoon.vehiclenote.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsSection(
    totalCount: Int,
    todayCount: Int,
    departmentStats: Map<String, Int>,
    modifier: Modifier = Modifier,
    onDepartmentClick: ((String) -> Unit)? = null,
    onTotalCountClick: (() -> Unit)? = null,
    onTodayCountClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "통계 현황",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total vehicles card
            StatisticCard(
                icon = Icons.Default.DirectionsCar,
                label = "전체",
                value = totalCount.toString(),
                unit = "대",
                modifier = Modifier.weight(1f),
                onClick = onTotalCountClick
            )

            // Today's entries card
            StatisticCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "오늘입차",
                value = todayCount.toString(),
                unit = "건",
                modifier = Modifier.weight(1f),
                onClick = onTodayCountClick
            )
        }

        if (departmentStats.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "부서별 현황",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    HorizontalDivider()

                    departmentStats.entries.take(5).forEach { (department, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (onDepartmentClick != null)
                                        Modifier.clickable { onDepartmentClick(department) }
                                    else Modifier
                                )
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = department,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${count}대",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    icon: ImageVector,
    value: String,
    label: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colors) { content() }
    } else {
        Card(modifier = modifier, colors = colors) { content() }
    }
}


@Preview
@Composable
fun StatisticsSectionPreview() {
    StatisticsSection(
        totalCount = 100,
        todayCount = 50,
        departmentStats = mapOf(
            "인사팀" to 1,
            "개발팀" to 14,
            "영업팀" to 134,
        )
    )
}

@Preview
@Composable
fun StatisticsCardPreview() {
    StatisticCard(
        icon = Icons.Default.DirectionsCar,
        value = "1,240",
        label = "전체",
        unit = "대"
    )
}