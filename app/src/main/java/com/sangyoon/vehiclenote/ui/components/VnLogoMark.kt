package com.sangyoon.vehiclenote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.ui.theme.VnEntry
import com.sangyoon.vehiclenote.ui.theme.VnInk

/**
 * VehicleNote 브랜드 마크: 번호판 프레임 + 4모서리 볼트 + 중앙 rule + 상단 tick.
 * 16~28dp 사이즈에서도 명료하게 렌더링됩니다.
 */
@Composable
fun VnLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = VnInk,
    accentColor: Color = VnEntry,
) {
    Canvas(modifier = modifier) {
        val s = size.toPx()
        val stroke = (s * 0.082f).coerceAtLeast(1.5f)
        val radius = s * 0.12f
        val boltR = (s * 0.062f).coerceAtLeast(1f)
        val pad = stroke / 2f

        // 번호판 외곽 프레임
        val rectL = pad + s * 0.04f
        val rectT = pad + s * 0.18f
        val rectR = s - pad - s * 0.04f
        val rectB = s - pad - s * 0.04f

        val framePath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset(rectL, rectT), Offset(rectR, rectB)),
                    cornerRadius = CornerRadius(radius, radius),
                )
            )
        }
        drawPath(framePath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))

        // 4모서리 볼트 점
        val boltOffset = s * 0.12f
        listOf(
            Offset(rectL + boltOffset, rectT + boltOffset),
            Offset(rectR - boltOffset, rectT + boltOffset),
            Offset(rectL + boltOffset, rectB - boltOffset),
            Offset(rectR - boltOffset, rectB - boltOffset),
        ).forEach { center ->
            drawCircle(color = tint, radius = boltR, center = center)
        }

        // 중앙 가로 rule
        val midY = (rectT + rectB) / 2f
        val ruleInset = s * 0.16f
        drawLine(
            color = tint,
            start = Offset(rectL + ruleInset, midY),
            end = Offset(rectR - ruleInset, midY),
            strokeWidth = stroke * 0.55f,
            cap = StrokeCap.Round,
        )

        // 상단 short tick (앰버 액센트 닷)
        val tickCx = s / 2f
        val tickR = (s * 0.07f).coerceAtLeast(1.5f)
        drawCircle(color = accentColor, radius = tickR, center = Offset(tickCx, rectT - tickR * 1.6f))
    }
}
