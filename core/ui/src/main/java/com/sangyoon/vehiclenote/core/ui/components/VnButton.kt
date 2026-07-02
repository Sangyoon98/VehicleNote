package com.sangyoon.vehiclenote.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangyoon.vehiclenote.core.ui.theme.AppTheme
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDangerBg
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDangerBgDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDangerText
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDisabledBg
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDisabledBgDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnDisabledText
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnOutlineBorder
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnOutlineBorderDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnOutlineText
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnOutlineTextDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnPrimaryBg
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnPrimaryBgDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnPrimaryText
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnSecondaryBg
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnSecondaryBgDark
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnSecondaryText
import com.sangyoon.vehiclenote.core.ui.theme.vnBtnSecondaryTextDark

/**
 * VehicleNote 디자인 시스템 버튼 스타일
 */
enum class VnButtonStyle {
    /** 주 액션 — 채워진 파란 버튼 */
    Primary,

    /** 보조 액션 — 연한 파란 채움 */
    Secondary,

    /** 테두리만 있는 버튼 */
    Outline,

    /** 삭제 등 위험 액션 — 빨간 채움 */
    Danger,

    /** 배경 없는 투명 버튼 */
    Ghost,
}

/**
 * VehicleNote 버튼 크기
 */
enum class VnButtonSize {
    /** 높이 36dp, 텍스트 13sp */
    Small,

    /** 높이 48dp, 텍스트 15sp (기본값) */
    Medium,

    /** 높이 56dp, 텍스트 17sp */
    Large,

    /** 높이 64dp, 텍스트 18sp — 화면 하단 전체 너비 CTA */
    Cta,
}

/**
 * VehicleNote 공통 버튼 컴포넌트
 *
 * Stitch 디자인 기반: rounded-xl (12dp), 프레스 시 scale(0.98), 라이트/다크 자동 대응
 *
 * @param text 버튼 텍스트
 * @param onClick 클릭 콜백
 * @param modifier Modifier
 * @param style 버튼 스타일 (Primary / Secondary / Outline / Danger / Ghost)
 * @param size 버튼 크기 (Small / Medium / Large / Cta)
 * @param leadingIcon 텍스트 앞에 표시할 아이콘
 * @param trailingIcon 텍스트 뒤에 표시할 아이콘
 * @param enabled 활성화 여부
 * @param fullWidth true면 가로 최대 너비 (size=Cta는 항상 fullWidth)
 * @param isLoading true면 텍스트/아이콘 대신 로딩 인디케이터 표시 (버튼 자동 비활성화)
 */
@Composable
fun VnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: VnButtonStyle = VnButtonStyle.Primary,
    size: VnButtonSize = VnButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    isLoading: Boolean = false,
) {
    val isDark = isSystemInDarkTheme()
    val isActuallyEnabled = enabled && !isLoading

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isActuallyEnabled) 0.98f else 1f,
        label = "vn_btn_scale",
    )

    // 크기별 속성
    val height: Dp
    val fontSize: Int
    val fontWeight: FontWeight
    val horizontalPadding: Dp
    val iconSize: Dp
    val cornerRadius: Dp

    when (size) {
        VnButtonSize.Small -> {
            height = 36.dp; fontSize = 13; fontWeight = FontWeight.SemiBold
            horizontalPadding = 14.dp; iconSize = 16.dp; cornerRadius = 8.dp
        }
        VnButtonSize.Medium -> {
            height = 48.dp; fontSize = 15; fontWeight = FontWeight.SemiBold
            horizontalPadding = 20.dp; iconSize = 18.dp; cornerRadius = 12.dp
        }
        VnButtonSize.Large -> {
            height = 56.dp; fontSize = 17; fontWeight = FontWeight.Bold
            horizontalPadding = 24.dp; iconSize = 20.dp; cornerRadius = 12.dp
        }
        VnButtonSize.Cta -> {
            height = 64.dp; fontSize = 18; fontWeight = FontWeight.Bold
            horizontalPadding = 24.dp; iconSize = 22.dp; cornerRadius = 12.dp
        }
    }

    // 스타일별 색상
    val containerColor: Color
    val contentColor: Color
    val border: BorderStroke?
    val elevation: Dp

    if (!isActuallyEnabled) {
        containerColor = if (isDark) vnBtnDisabledBgDark else vnBtnDisabledBg
        contentColor = vnBtnDisabledText
        border = null
        elevation = 0.dp
    } else {
        when (style) {
            VnButtonStyle.Primary -> {
                containerColor = if (isDark) vnBtnPrimaryBgDark else vnBtnPrimaryBg
                contentColor = vnBtnPrimaryText
                border = null
                elevation = if (size == VnButtonSize.Cta) 6.dp else 4.dp
            }
            VnButtonStyle.Secondary -> {
                containerColor = if (isDark) vnBtnSecondaryBgDark else vnBtnSecondaryBg
                contentColor = if (isDark) vnBtnSecondaryTextDark else vnBtnSecondaryText
                border = null
                elevation = 0.dp
            }
            VnButtonStyle.Outline -> {
                containerColor = Color.Transparent
                contentColor = if (isDark) vnBtnOutlineTextDark else vnBtnOutlineText
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isDark) vnBtnOutlineBorderDark else vnBtnOutlineBorder,
                )
                elevation = 0.dp
            }
            VnButtonStyle.Danger -> {
                containerColor = if (isDark) vnBtnDangerBgDark else vnBtnDangerBg
                contentColor = vnBtnDangerText
                border = null
                elevation = 2.dp
            }
            VnButtonStyle.Ghost -> {
                containerColor = Color.Transparent
                contentColor = if (isDark) vnBtnOutlineTextDark else vnBtnOutlineText
                border = null
                elevation = 0.dp
            }
        }
    }

    val widthModifier = if (fullWidth || size == VnButtonSize.Cta) Modifier.fillMaxWidth() else Modifier

    Surface(
        onClick = onClick,
        modifier = modifier
            .then(widthModifier)
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = isActuallyEnabled,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = elevation,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    color = contentColor,
                    strokeWidth = 2.dp,
                )
            } else {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = fontSize.sp,
                    fontWeight = fontWeight,
                    lineHeight = (fontSize + 4).sp,
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7F8)
@Composable
private fun VnButtonLightPreview() {
    AppTheme(darkTheme = false, dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VnButton("Primary", onClick = {}, style = VnButtonStyle.Primary)
            VnButton("Secondary", onClick = {}, style = VnButtonStyle.Secondary)
            VnButton("Outline", onClick = {}, style = VnButtonStyle.Outline)
            VnButton("Danger", onClick = {}, style = VnButtonStyle.Danger)
            VnButton("Ghost", onClick = {}, style = VnButtonStyle.Ghost)
            VnButton("Disabled", onClick = {}, enabled = false)
            VnButton("차량 등록하기", onClick = {}, style = VnButtonStyle.Primary, size = VnButtonSize.Cta)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14171E)
@Composable
private fun VnButtonDarkPreview() {
    AppTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VnButton("Primary", onClick = {}, style = VnButtonStyle.Primary)
            VnButton("Secondary", onClick = {}, style = VnButtonStyle.Secondary)
            VnButton("Outline", onClick = {}, style = VnButtonStyle.Outline)
            VnButton("Danger", onClick = {}, style = VnButtonStyle.Danger)
            VnButton("Ghost", onClick = {}, style = VnButtonStyle.Ghost)
            VnButton("Disabled", onClick = {}, enabled = false)
            VnButton("차량 등록하기", onClick = {}, style = VnButtonStyle.Primary, size = VnButtonSize.Cta)
        }
    }
}
