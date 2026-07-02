package com.sangyoon.vehiclenote.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ─── VN Semantic Type Scales ──────────────────────────────────────────────────
// Maps directly to design tokens in design_handoff_visual_identity/README.md

/** 38sp / 700 / mono +0.02em — 번호판 히어로 (detail screen) */
val VnTypeDisplay = TextStyle(
    fontFamily = MonoFont,
    fontWeight = FontWeight.Bold,
    fontSize = 38.sp,
    letterSpacing = 0.02.em,
    lineHeight = 44.sp,
)

/** 28sp / 700 / -0.02em — 화면 타이틀, 통계 큰 수 */
val VnTypeHeadline = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    letterSpacing = (-0.02).em,
    lineHeight = 34.sp,
)

/** 22sp / 700 / -0.02em — 섹션 타이틀 */
val VnTypeTitle = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    letterSpacing = (-0.02).em,
    lineHeight = 28.sp,
)

/** 15sp / 600 — 리스트 headline */
val VnTypeBodyLg = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    letterSpacing = 0.em,
    lineHeight = 22.sp,
)

/** 14sp / 500 — 본문 */
val VnTypeBody = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    letterSpacing = 0.em,
    lineHeight = 20.sp,
)

/** 13sp / 500 — 보조 본문 */
val VnTypeBodySm = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    letterSpacing = 0.em,
    lineHeight = 18.sp,
)

/** 12sp / 600 / +0.04em uppercase — 섹션 레이블, 칩 */
val VnTypeLabel = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    letterSpacing = 0.04.em,
    lineHeight = 16.sp,
)

/** 11sp / 600 / +0.06em uppercase — 메타 레이블, 태그 */
val VnTypeCaption = TextStyle(
    fontFamily = SansFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    letterSpacing = 0.06.em,
    lineHeight = 14.sp,
)

/** 13sp / 600 / mono +0.02em — 타임스탬프, 전화번호 */
val VnTypeMonoTime = TextStyle(
    fontFamily = MonoFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    letterSpacing = 0.02.em,
    lineHeight = 18.sp,
)

// ─── Material 3 Typography Mapping ───────────────────────────────────────────
val AppTypography = Typography(
    // display: 번호판 히어로
    displayLarge  = VnTypeDisplay,
    displayMedium = VnTypeDisplay.copy(fontSize = 32.sp, lineHeight = 38.sp),
    displaySmall  = VnTypeDisplay.copy(fontSize = 28.sp, lineHeight = 34.sp),

    // headline: 화면 타이틀, 통계
    headlineLarge  = VnTypeHeadline,
    headlineMedium = VnTypeHeadline.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall  = VnTypeTitle,

    // title: 섹션 및 컴포넌트 타이틀
    titleLarge  = VnTypeBodyLg.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = VnTypeBodyLg,
    titleSmall  = VnTypeBody.copy(fontWeight = FontWeight.SemiBold),

    // body: 본문
    bodyLarge  = VnTypeBody.copy(fontSize = 16.sp),
    bodyMedium = VnTypeBody,
    bodySmall  = VnTypeBodySm,

    // label: 레이블, 칩, 태그
    labelLarge  = VnTypeLabel.copy(fontSize = 13.sp),
    labelMedium = VnTypeLabel,
    labelSmall  = VnTypeCaption,
)
