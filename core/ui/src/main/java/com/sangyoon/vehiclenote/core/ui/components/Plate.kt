package com.sangyoon.vehiclenote.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sangyoon.vehiclenote.core.ui.theme.VnEntry
import com.sangyoon.vehiclenote.core.ui.theme.VnEntryBg
import com.sangyoon.vehiclenote.core.ui.theme.VnEntryInk
import com.sangyoon.vehiclenote.core.ui.theme.VnExit
import com.sangyoon.vehiclenote.core.ui.theme.VnExitBg
import com.sangyoon.vehiclenote.core.ui.theme.VnExitInk
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeMonoTime
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

enum class PlateSize { Sm, Md, Lg, Xl }

enum class PlateVariant {
    Default,  // ink bg, white text
    Entry,    // entryBg, entryInk text
    Exit,     // exitBg, exitInk text
    Neutral,  // white bg + line border, ink text — for use on dark surfaces
}

private data class PlateTokens(
    val fontSize: Float,
    val radius: Dp,
    val paddingH: Dp,
    val paddingV: Dp,
)

private fun tokensFor(size: PlateSize) = when (size) {
    PlateSize.Sm -> PlateTokens(fontSize = 12f, radius = 4.dp,  paddingH = 6.dp,  paddingV = 3.dp)
    PlateSize.Md -> PlateTokens(fontSize = 14f, radius = 6.dp,  paddingH = 8.dp,  paddingV = 4.dp)
    PlateSize.Lg -> PlateTokens(fontSize = 18f, radius = 8.dp,  paddingH = 10.dp, paddingV = 6.dp)
    PlateSize.Xl -> PlateTokens(fontSize = 38f, radius = 8.dp,  paddingH = 14.dp, paddingV = 10.dp)
}

private data class PlateColors(
    val bg: Color,
    val text: Color,
    val border: Color = Color.Transparent,
)

@Composable
private fun colorsFor(variant: PlateVariant): PlateColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (variant) {
        PlateVariant.Default -> PlateColors(bg = colorScheme.primary, text = colorScheme.onPrimary)
        PlateVariant.Entry   -> PlateColors(bg = VnEntryBg, text = VnEntryInk)
        PlateVariant.Exit    -> PlateColors(bg = VnExitBg,  text = VnExitInk)
        PlateVariant.Neutral -> PlateColors(
            bg = colorScheme.surface,
            text = colorScheme.onSurface,
            border = colorScheme.outline,
        )
    }
}

/**
 * 앱 전체의 번호판 렌더링 단일 진입점.
 * 번호판, 시각, 전화번호 등 모든 코드값은 반드시 이 컴포저블 또는 VnTypeMonoTime 스타일을 사용.
 *
 * @param animated true이면 첫 등장 시 fade + letter-spacing 0.06em→0.02em 300ms 애니메이션.
 *                 detail 화면의 Xl 번호판 히어로 전용. 리스트 아이템에선 false.
 */
@Composable
fun Plate(
    value: String,
    modifier: Modifier = Modifier,
    size: PlateSize = PlateSize.Md,
    variant: PlateVariant = PlateVariant.Default,
    animated: Boolean = false,
) {
    val t = tokensFor(size)
    val c = colorsFor(variant)
    val shape = RoundedCornerShape(t.radius)

    val letterSpacingEm = remember { Animatable(if (animated) 0.06f else 0.02f) }
    val alpha = remember { Animatable(if (animated) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (animated) {
            val spec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
            launch { letterSpacingEm.animateTo(0.02f, animationSpec = spec) }
            launch { alpha.animateTo(1f, animationSpec = spec) }
        }
    }

    val baseModifier = modifier
        .graphicsLayer { this.alpha = alpha.value }
        .background(c.bg, shape)
        .then(
            if (c.border != Color.Transparent) Modifier.border(1.dp, c.border, shape)
            else Modifier
        )
        .padding(horizontal = t.paddingH, vertical = t.paddingV)

    Text(
        text = value,
        modifier = baseModifier,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = if (size == PlateSize.Xl) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = t.fontSize.sp,
            letterSpacing = letterSpacingEm.value.em,
        ),
        color = c.text,
        maxLines = 1,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun PlatePreview() {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Plate("12가1234", size = PlateSize.Sm)
        Plate("12가1234", size = PlateSize.Md)
        Plate("12가1234", size = PlateSize.Lg)
        Plate("12가1234", size = PlateSize.Xl)
        Plate("12가1234", size = PlateSize.Md, variant = PlateVariant.Entry)
        Plate("12가1234", size = PlateSize.Md, variant = PlateVariant.Exit)
        Plate("12가1234", size = PlateSize.Md, variant = PlateVariant.Neutral)
    }
}
