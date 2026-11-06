package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LavenderBlissLightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = LavenderPastelSurface,
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = LavenderSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F3FF),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = PlayfulAccentPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF9F1239),
    background = WarmBackground,
    onBackground = TextPrimary,
    surface = PureWhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = LavenderPastelSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderLight
)

private val LavenderBlissDarkColorScheme = darkColorScheme(
    primary = DarkNightPrimary,
    onPrimary = Color(0xFF2E1065),
    primaryContainer = DarkNightSurfaceVariant,
    onPrimaryContainer = DarkNightPrimary,
    secondary = DarkNightSecondary,
    onSecondary = DarkNightBackground,
    secondaryContainer = Color(0xFF342B47),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = PlayfulAccentPink,
    onTertiary = DarkNightBackground,
    tertiaryContainer = Color(0xFF4C1D30),
    onTertiaryContainer = Color(0xFFFFD1DC),
    background = DarkNightBackground,
    onBackground = DarkNightTextPrimary,
    surface = DarkNightSurface,
    onSurface = DarkNightTextPrimary,
    surfaceVariant = DarkNightSurfaceVariant,
    onSurfaceVariant = DarkNightTextSecondary,
    outline = DarkNightBorder,
    outlineVariant = Color(0xFF2E283D)
)

@Composable
fun LittleRoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep false to preserve curated Lavender Bliss design system
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LavenderBlissDarkColorScheme
        else -> LavenderBlissLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
