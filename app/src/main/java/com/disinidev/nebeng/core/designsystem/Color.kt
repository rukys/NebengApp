package com.disinidev.nebeng.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object NebengColor {
    // Primary (Brand)
    val Primary900 = Color(0xFF000000)
    val Primary50 = Color(0xFFF6F6F8)
    val Primary0 = Color(0xFFFFFFFF)

    val Black = Primary900
    val White = Primary0

    // Grayscale
    val Gray800 = Color(0xFF1A1A1A)
    val Gray600 = Color(0xFF545454)
    val Gray400 = Color(0xFF757575)
    val Gray200 = Color(0xFFE2E2E2)
    val Gray100 = Color(0xFFF6F6F6)
    val Gray50 = Color(0xFFFAFAFA)

    // Semantic
    val Success700 = Color(0xFF2E7D32)
    val Success100 = Color(0xFFE8F5E9)
    val Warning600 = Color(0xFFF57C00)
    val Warning100 = Color(0xFFFFF3E0)
    val Danger600 = Color(0xFFD32F2F)
    val Danger100 = Color(0xFFFFEBEE)
    val Info600 = Color(0xFF1565C0)
    val Info100 = Color(0xFFE3F2FD)

    // Special
    val QrisRed = Color(0xFFEE3124)
    val BackgroundPrimary = Color(0xFFF7F8FA)
    val DarkSurface = Color(0xFF121212)
    val PressedBlack = Color(0xFF333333)
    val DisabledGray = Color(0xFFA0A0A0)
}

@Immutable
data class NebengColorScheme(
    val primary: Color = NebengColor.Primary900,
    val onPrimary: Color = NebengColor.Primary0,
    val background: Color = NebengColor.Primary0,
    val onBackground: Color = NebengColor.Gray800,
    val surface: Color = NebengColor.Primary0,
    val onSurface: Color = NebengColor.Gray800,
    val surfaceVariant: Color = NebengColor.Primary50,
    val onSurfaceVariant: Color = NebengColor.Gray600,
    val outline: Color = NebengColor.Gray200,
    val outlineVariant: Color = NebengColor.Gray100,
    val error: Color = NebengColor.Danger600,
    val onError: Color = NebengColor.Primary0,
    val errorContainer: Color = NebengColor.Danger100,
    val onErrorContainer: Color = NebengColor.Danger600
)

val LightNebengColorScheme = NebengColorScheme()

val DarkNebengColorScheme = NebengColorScheme(
    primary = NebengColor.Primary0,
    onPrimary = NebengColor.Primary900,
    background = NebengColor.Primary900,
    onBackground = NebengColor.Primary0,
    surface = NebengColor.DarkSurface,
    onSurface = NebengColor.Primary0,
    surfaceVariant = NebengColor.Gray800,
    onSurfaceVariant = NebengColor.Gray400,
    outline = NebengColor.Gray600,
    outlineVariant = NebengColor.Gray800,
    error = NebengColor.Danger600,
    onError = NebengColor.Primary0,
    errorContainer = NebengColor.Danger100,
    onErrorContainer = NebengColor.Danger600
)
