package com.flowsim.flosasms.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlowSimColorScheme = darkColorScheme(
    primary = Violet700,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Violet300,
    secondary = BallBlue,
    onSecondary = BgDarkest,
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = BallBlue2,
    tertiary = BallPink,
    onTertiary = BgDarkest,
    tertiaryContainer = Color(0xFF9D174D),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = BgMain,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = DividerAlt,
    error = ColorError,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    inverseSurface = TextPrimary,
    inverseOnSurface = BgDarkest,
    inversePrimary = Violet700,
    surfaceTint = Violet500,
    scrim = Color(0xFF000000)
)

@Composable
fun FlowSimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlowSimColorScheme,
        typography = FlowSimTypography,
        content = content
    )
}
