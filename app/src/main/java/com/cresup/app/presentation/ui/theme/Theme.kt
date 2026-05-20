package com.cresup.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Background,
    primaryContainer = NeonGreenDim,
    onPrimaryContainer = TextPrimary,
    secondary = NeonBlue,
    onSecondary = TextPrimary,
    secondaryContainer = NeonBlueDim,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonCyan,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = TextPrimary,
    outline = CardBorder,
    outlineVariant = GlassBorder,
)

@Immutable
data class CresUpColorScheme(
    val background: Color = Background,
    val backgroundSecondary: Color = BackgroundSecondary,
    val surface: Color = Surface,
    val surfaceVariant: Color = SurfaceVariant,
    val neonGreen: Color = NeonGreen,
    val neonBlue: Color = NeonBlue,
    val neonCyan: Color = NeonCyan,
    val accentYellow: Color = AccentYellow,
    val accentOrange: Color = AccentOrange,
    val accentRed: Color = AccentRed,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val cardBackground: Color = CardBackground,
    val cardBorder: Color = CardBorder,
    val glassBackground: Color = GlassBackground,
    val glassBorder: Color = GlassBorder,
)

val LocalCresUpColors = staticCompositionLocalOf { CresUpColorScheme() }

object CresUpTheme {
    val colors: CresUpColorScheme
        @Composable get() = LocalCresUpColors.current
}

@Composable
fun CresUpTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalCresUpColors provides CresUpColorScheme()
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
