package com.sangyoon.vehiclenote.feature.vehicle.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.feature.vehicle.components.StatCard
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeBodySm
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeCaption
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeMonoTime

@Composable
fun StatisticsSection(
    totalCount: Int,
    todayCount: Int,
    departmentStats: Map<String, Int>,
    modifier: Modifier = Modifier,
    onDepartmentClick: ((String) -> Unit)? = null,
    onTotalCountClick: (() -> Unit)? = null,
    onTodayCountClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 2-카드 그리드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                label = "등록 차량",
                value = totalCount.toString(),
                modifier = Modifier.weight(1f),
                onClick = onTotalCountClick,
            )
            StatCard(
                label = "오늘 입차",
                value = "+$todayCount",
                emphasized = true,
                modifier = Modifier.weight(1f),
                onClick = onTodayCountClick,
            )
        }

        // 부서별 바 차트
        if (departmentStats.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "부서별 현황",
                    style = VnTypeCaption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val maxVal = departmentStats.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                departmentStats.entries.forEach { (dept, count) ->
                    DeptBarRow(
                        label = dept,
                        count = count,
                        fraction = count.toFloat() / maxVal,
                        onClick = onDepartmentClick?.let { { it(dept) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeptBarRow(
    label: String,
    count: Int,
    fraction: Float,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 레이블 60dp 고정
        Text(
            text = label,
            style = VnTypeBodySm,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.width(60.dp),
        )

        // flex 바 트랙
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }

        // 수치 28dp 고정
        Text(
            text = count.toString(),
            style = VnTypeMonoTime,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun StatisticsSectionPreview() {
    StatisticsSection(
        totalCount = 247,
        todayCount = 6,
        departmentStats = mapOf(
            "개발팀" to 14,
            "영업팀" to 32,
            "인사팀" to 8,
        ),
        modifier = Modifier.padding(16.dp),
    )
}
