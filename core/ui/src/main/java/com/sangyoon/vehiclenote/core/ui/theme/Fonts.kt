package com.sangyoon.vehiclenote.core.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.sangyoon.vehiclenote.core.ui.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val interSpec = GoogleFont("Inter")
private val jetBrainsMonoSpec = GoogleFont("JetBrains Mono")

// Public so Type.kt and any other theme file can reference them.
val SansFont: FontFamily = FontFamily(
    Font(googleFont = interSpec, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interSpec, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interSpec, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interSpec, fontProvider = provider, weight = FontWeight.Bold),
)

val MonoFont: FontFamily = FontFamily(
    Font(googleFont = jetBrainsMonoSpec, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = jetBrainsMonoSpec, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = jetBrainsMonoSpec, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = jetBrainsMonoSpec, fontProvider = provider, weight = FontWeight.Bold),
)
