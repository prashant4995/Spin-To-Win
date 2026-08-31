package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.local.ColorPalette
import com.example.data.local.ThemeMode

data class CustomThemeColors(
    val bg: Color,
    val bgSurface: Color,
    val cardBg: Color,
    val cardBgSubtle: Color,
    val surfaceDark: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val accentGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        bg = AppWhiteBg,
        bgSurface = AppWhiteBgSurface,
        cardBg = AppCardBg,
        cardBgSubtle = AppCardBgSubtle,
        surfaceDark = AppSurfaceSoft,
        primaryAccent = AppPrimaryBlue,
        secondaryAccent = AppBlueDark,
        accentGlow = AppBlueLight,
        textPrimary = AppTextDark,
        textSecondary = AppTextSecondary,
        textOnAccent = AppTextOnButton,
        cardBorder = AppCardBorder,
        isDark = false
    )
}

object AppTheme {
    val customColors: CustomThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current
}

fun createDarkColorScheme(palette: ColorPalette): ColorScheme = lightColorScheme(
    primary = AppPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = AppBlueLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = AppBlueDark,
    onTertiary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF475569),
    outline = AppPrimaryBlue,
    outlineVariant = Color(0xFFE2E8F0)
)

fun createLightColorScheme(palette: ColorPalette): ColorScheme {
    val primary = AppPrimaryBlue
    val primaryContainer = Color(0xFFDBEAFE)
    val onPrimaryContainer = Color(0xFF1E40AF)

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = AppBlueLight,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEFF6FF),
        onSecondaryContainer = Color(0xFF1E3A8A),
        tertiary = AppBlueDark,
        onTertiary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFF475569),
        outline = primary,
        outlineVariant = Color(0xFFE2E8F0)
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    colorPalette: ColorPalette = ColorPalette.PEACOCK,
    content: @Composable () -> Unit
) {
    val colorScheme = createLightColorScheme(colorPalette)

    val customColors = CustomThemeColors(
        bg = Color(0xFFFFFFFF),
        bgSurface = Color(0xFFF8FAFC),
        cardBg = Color(0xFFFFFFFF),
        cardBgSubtle = Color(0xFFF1F5F9),
        surfaceDark = Color(0xFFEFF6FF),
        primaryAccent = AppPrimaryBlue,
        secondaryAccent = AppBlueDark,
        accentGlow = AppBlueLight,
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569),
        textOnAccent = Color.White,
        cardBorder = Color(0xFFE2E8F0),
        isDark = false
    )

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
