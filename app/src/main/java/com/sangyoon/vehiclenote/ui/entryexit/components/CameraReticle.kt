package com.sangyoon.vehiclenote.ui.entryexit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.ui.theme.VnEntry
import com.sangyoon.vehiclenote.ui.theme.VnTypeBodySm
import com.sangyoon.vehiclenote.ui.theme.VnTypeMonoTime

/**
 * OCR 카메라 뷰포트 위에 표시되는 번호판 인식 리티클.
 * 250×130dp 사각형: 4모서리 amber 브래킷 + 수평 스캔라인 + 내부 점선 박스.
 */
@Composable
fun CameraReticle(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scan_y",
    )

    Canvas(
        modifier = modifier
            .width(250.dp)
            .height(130.dp),
    ) {
        val bracketLen = 20.dp.toPx()
        val strokeW = 2.5.dp.toPx()
        val accent = VnEntry

        // ── 4모서리 L-브래킷 ───────────────────────────────────────────────
        fun bracket(x: Float, y: Float, flipX: Boolean, flipY: Boolean) {
            val dx = if (flipX) -bracketLen else bracketLen
            val dy = if (flipY) -bracketLen else bracketLen
            drawLine(accent, Offset(x, y), Offset(x + dx, y), strokeW, StrokeCap.Round)
            drawLine(accent, Offset(x, y), Offset(x, y + dy), strokeW, StrokeCap.Round)
        }
        bracket(0f, 0f,            flipX = false, flipY = false)  // TL
        bracket(size.width, 0f,    flipX = true,  flipY = false)  // TR
        bracket(0f, size.height,   flipX = false, flipY = true)   // BL
        bracket(size.width, size.height, flipX = true, flipY = true) // BR

        // ── 내부 점선 박스 ──────────────────────────────────────────────────
        val inset = 8.dp.toPx()
        val dashPath = Path().apply {
            addRect(Rect(Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2)))
        }
        drawPath(
            path = dashPath,
            color = accent.copy(alpha = 0.35f),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(6.dp.toPx(), 5.dp.toPx()), 0f
                ),
            ),
        )

        // ── 스캔라인 (glow 그라디언트, top→bottom) ─────────────────────────
        val scanY = scanProgress * size.height
        val glowH = 32.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    accent.copy(alpha = 0.55f),
                    Color.Transparent,
                ),
                startY = (scanY - glowH / 2).coerceAtLeast(0f),
                endY   = (scanY + glowH / 2).coerceAtMost(size.height),
            ),
            size = size,
        )
    }
}

/**
 * 카메라 화면 상단 오버레이: 펄싱 앰버 닷 + "인식 중" + "OCR · REC".
 */
@Composable
fun CameraStatusBar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(VnEntry.copy(alpha = pulseAlpha), CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "인식 중", style = VnTypeBodySm, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "OCR · REC", style = VnTypeMonoTime, color = VnEntry.copy(alpha = 0.85f))
    }
}
