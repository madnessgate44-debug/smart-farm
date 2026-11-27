package com.ahmedpasha.smartfarm.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = SurfaceLight,
    primaryContainer = EmeraldGreenLight,
    onPrimaryContainer = CharcoalTeal,
    secondary = ForestGold,
    tertiary = FarmOlive,
    onTertiary = SurfaceLight,
    tertiaryContainer = Color(0xFFDDE2C8),
    onTertiaryContainer = CharcoalTeal,
    onSecondary = CharcoalTeal,
    secondaryContainer = ForestGoldLight,
    onSecondaryContainer = CharcoalTeal,
    background = FarmCanvas,
    onBackground = FarmInk,
    surface = FarmSurface,
    onSurface = FarmInk,
    surfaceVariant = Color(0xFFECE7DA),
    onSurfaceVariant = FarmMuted,
    outline = FarmBorder,
    error = HighPriorityRed
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreenLight,
    onPrimary = CharcoalTeal,
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = Color(0xFFEAF3E9),
    secondary = ForestGoldLight,
    tertiary = Color(0xFFC4C99B),
    onTertiary = CharcoalTeal,
    tertiaryContainer = Color(0xFF515B3C),
    onTertiaryContainer = Color(0xFFF0F1E9),
    onSecondary = CharcoalTeal,
    secondaryContainer = ForestGoldDark,
    onSecondaryContainer = Color(0xFFFFF2D5),
    background = CharcoalTeal,
    onBackground = Color(0xFFF0F1E9),
    surface = SurfaceDark,
    onSurface = Color(0xFFF0F1E9),
    surfaceVariant = Color(0xFF35443A),
    onSurfaceVariant = Color(0xFFC2CBBF),
    outline = Color(0xFF59685C),
    error = HighPriorityRed
)

@Composable
fun SmartFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FarmTypography,
        content = content
    )
}