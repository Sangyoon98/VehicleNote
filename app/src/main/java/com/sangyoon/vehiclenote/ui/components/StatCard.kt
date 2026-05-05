package com.sangyoon.vehiclenote.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sangyoon.vehiclenote.ui.theme.VnTypeBodySm

// 통계 수치 전용: 32sp mono/700
private val StatValueStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    letterSpacing = 0.02.em,
    lineHeight = 36.sp,
)

/**
 * 홈 통계 2-카드 그리드용 컴포저블.
 * emphasized = false → paper/border 카드 (일반)
 * emphasized = true  → ink bg 카드 (반전 강조)
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor   = if (emphasized) colorScheme.primary else colorScheme.surface
    val textColor = if (emphasized) colorScheme.onPrimary else colorScheme.onSurface
    val subColor  = if (emphasized) colorScheme.onPrimary.copy(alpha = 0.65f)
                    else colorScheme.onSurfaceVariant
    val border    = if (emphasized) null else BorderStroke(1.dp, colorScheme.outline)
    val shape     = RoundedCornerShape(14.dp)

    val cardColors = CardDefaults.cardColors(containerColor = bgColor)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = VnTypeBodySm,
                color = subColor,
            )
            Text(
                text = value,
                style = StatValueStyle,
                color = textColor,
            )
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = border,
        ) { content() }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = border,
        ) { content() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun StatCardPreview() {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(label = "등록 차량", value = "247", modifier = Modifier.weight(1f))
        StatCard(label = "오늘 등록", value = "+6", emphasized = true, modifier = Modifier.weight(1f))
    }
}
