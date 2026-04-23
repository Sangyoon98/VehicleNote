package com.sangyoon.vehiclenote.ui.theme

import androidx.compose.ui.graphics.Color

// ─── VehicleNote Design Tokens ───────────────────────────────────────────────
// Source: design_handoff_visual_identity/README.md

// Ink neutrals
val VnInk        = Color(0xFF22262E)  // primary text, CTA bg, top surface
val VnInkSoft    = Color(0xFF4A4F58)  // secondary text
val VnInkMute    = Color(0xFF7B8088)  // tertiary / labels

// Borders
val VnLine       = Color(0xFFDEDFE2)  // 1dp border
val VnLineSoft   = Color(0xFFEBECEE)  // row dividers

// Backgrounds
val VnPaper      = Color(0xFFFAF9F5)  // app bg (warm white)
val VnPaperDeep  = Color(0xFFF3F1EA)  // elevated neutral
val VnSurface    = Color(0xFFFFFFFF)  // cards

// Entry (amber) — 입차
val VnEntry      = Color(0xFFE0A343)
val VnEntryInk   = Color(0xFF5A3E13)
val VnEntryBg    = Color(0xFFFAEDD0)

// Exit (teal) — 출차
val VnExit       = Color(0xFF3B9BAE)
val VnExitInk    = Color(0xFF1E4555)
val VnExitBg     = Color(0xFFDDEEF0)

// Danger
val VnDanger     = Color(0xFFC84434)
val VnDangerBg   = Color(0xFFFBE4DE)

// ─── Material 3 Light Scheme Roles ───────────────────────────────────────────
val primaryLight                    = VnInk
val onPrimaryLight                  = VnSurface
val primaryContainerLight           = VnPaperDeep
val onPrimaryContainerLight         = VnInk
val secondaryLight                  = VnInkSoft
val onSecondaryLight                = VnSurface
val secondaryContainerLight         = VnLineSoft
val onSecondaryContainerLight       = VnInkSoft
val tertiaryLight                   = VnEntry
val onTertiaryLight                 = VnEntryInk
val tertiaryContainerLight          = VnEntryBg
val onTertiaryContainerLight        = VnEntryInk
val errorLight                      = VnDanger
val onErrorLight                    = VnSurface
val errorContainerLight             = VnDangerBg
val onErrorContainerLight           = VnDanger
val backgroundLight                 = VnPaper
val onBackgroundLight               = VnInk
val surfaceLight                    = VnSurface
val onSurfaceLight                  = VnInk
val surfaceVariantLight             = VnPaperDeep
val onSurfaceVariantLight           = VnInkSoft
val outlineLight                    = VnLine
val outlineVariantLight             = VnLineSoft
val scrimLight                      = Color(0xFF000000)
val inverseSurfaceLight             = VnInk
val inverseOnSurfaceLight           = VnPaper
val inversePrimaryLight             = VnPaperDeep
val surfaceDimLight                 = VnPaperDeep
val surfaceBrightLight              = VnSurface
val surfaceContainerLowestLight     = VnSurface
val surfaceContainerLowLight        = VnPaper
val surfaceContainerLight           = VnPaperDeep
val surfaceContainerHighLight       = Color(0xFFEAE8E0)
val surfaceContainerHighestLight    = VnLine

// ─── Material 3 Dark Scheme Roles ────────────────────────────────────────────
// 다크 모드: ink 계열 반전, 따뜻한 표면 유지
val primaryDark                     = Color(0xFFD4C9B8)  // warm off-white
val onPrimaryDark                   = Color(0xFF1A1D23)
val primaryContainerDark            = Color(0xFF2E3239)
val onPrimaryContainerDark          = Color(0xFFD4C9B8)
val secondaryDark                   = Color(0xFFADB1B8)
val onSecondaryDark                 = Color(0xFF1A1D23)
val secondaryContainerDark          = Color(0xFF2A2D33)
val onSecondaryContainerDark        = Color(0xFFADB1B8)
val tertiaryDark                    = VnEntry
val onTertiaryDark                  = VnEntryInk
val tertiaryContainerDark           = Color(0xFF3D2E10)
val onTertiaryContainerDark         = VnEntry
val errorDark                       = Color(0xFFFF8A7A)
val onErrorDark                     = Color(0xFF3A0E08)
val errorContainerDark              = Color(0xFF5C1A12)
val onErrorContainerDark            = Color(0xFFFF8A7A)
val backgroundDark                  = Color(0xFF14161A)
val onBackgroundDark                = Color(0xFFE2DDD5)
val surfaceDark                     = Color(0xFF1C1F24)
val onSurfaceDark                   = Color(0xFFE2DDD5)
val surfaceVariantDark              = Color(0xFF262A30)
val onSurfaceVariantDark            = Color(0xFFADB1B8)
val outlineDark                     = Color(0xFF3D4148)
val outlineVariantDark              = Color(0xFF2E3239)
val scrimDark                       = Color(0xFF000000)
val inverseSurfaceDark              = VnPaper
val inverseOnSurfaceDark            = VnInk
val inversePrimaryDark              = VnInk
val surfaceDimDark                  = Color(0xFF0E1014)
val surfaceBrightDark               = Color(0xFF2A2D33)
val surfaceContainerLowestDark      = Color(0xFF0A0C10)
val surfaceContainerLowDark         = Color(0xFF1A1D22)
val surfaceContainerDark            = Color(0xFF20242A)
val surfaceContainerHighDark        = Color(0xFF272B31)
val surfaceContainerHighestDark     = Color(0xFF2E3239)

// ─── VN Button Colors ─────────────────────────────────────────────────────────
val vnBtnPrimaryBg        = VnInk
val vnBtnPrimaryBgDark    = Color(0xFF2E3239)
val vnBtnPrimaryText      = VnSurface

val vnBtnSecondaryBg      = VnPaperDeep
val vnBtnSecondaryBgDark  = Color(0xFF262A30)
val vnBtnSecondaryText    = VnInk
val vnBtnSecondaryTextDark= Color(0xFFD4C9B8)

val vnBtnOutlineBorder    = VnLine
val vnBtnOutlineBorderDark= Color(0xFF3D4148)
val vnBtnOutlineText      = VnInk
val vnBtnOutlineTextDark  = Color(0xFFD4C9B8)

val vnBtnDangerBg         = VnDanger
val vnBtnDangerBgDark     = Color(0xFF8C2E22)
val vnBtnDangerText       = VnSurface

val vnBtnDisabledBg       = VnLineSoft
val vnBtnDisabledBgDark   = Color(0xFF262A30)
val vnBtnDisabledText     = VnInkMute
