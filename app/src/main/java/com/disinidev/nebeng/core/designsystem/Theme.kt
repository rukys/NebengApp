package com.disinidev.nebeng.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = NebengColor.Primary900,
    onPrimary = NebengColor.Primary0,
    primaryContainer = NebengColor.Primary50,
    onPrimaryContainer = NebengColor.Primary900,
    secondary = NebengColor.Gray800,
    onSecondary = NebengColor.Primary0,
    background = NebengColor.Primary0,
    onBackground = NebengColor.Gray800,
    surface = NebengColor.Primary0,
    onSurface = NebengColor.Gray800,
    surfaceVariant = NebengColor.Primary50,
    onSurfaceVariant = NebengColor.Gray600,
    outline = NebengColor.Gray200,
    outlineVariant = NebengColor.Gray100,
    error = NebengColor.Danger600,
    onError = NebengColor.Primary0,
    errorContainer = NebengColor.Danger100,
    onErrorContainer = NebengColor.Danger600
)

private val DarkColorScheme = darkColorScheme(
    primary = NebengColor.Primary0,
    onPrimary = NebengColor.Primary900,
    primaryContainer = NebengColor.Gray800,
    onPrimaryContainer = NebengColor.Primary0,
    secondary = NebengColor.Gray200,
    onSecondary = NebengColor.Primary900,
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

val LocalNebengColors = staticCompositionLocalOf { NebengColorScheme() }
val LocalNebengExtendedTypography = staticCompositionLocalOf { NebengExtendedTypography() }

object NebengTheme {
    val colors: NebengColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalNebengColors.current

    val extendedTypography: NebengExtendedTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNebengExtendedTypography.current

    val spacing: NebengSpacing
        get() = NebengSpacing

    val radius: NebengRadius
        get() = NebengRadius
}

@Composable
fun NebengTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkNebengColorScheme else LightNebengColorScheme
    val extendedTypography = NebengExtendedTypography()

    CompositionLocalProvider(
        LocalNebengColors provides customColors,
        LocalNebengExtendedTypography provides extendedTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NebengTypography,
            shapes = NebengShapes,
            content = content
        )
    }
}
