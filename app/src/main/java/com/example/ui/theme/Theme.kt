package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ArtisticAmberGold,
    onPrimary = ArtisticMaroonBg,
    primaryContainer = ArtisticAmberContainer,
    onPrimaryContainer = ArtisticCream,
    secondary = ArtisticAmberGlow,
    onSecondary = ArtisticMaroonBg,
    secondaryContainer = ArtisticMaroonSurface,
    onSecondaryContainer = ArtisticCream,
    tertiary = ArtisticAmberDeep,
    onTertiary = ArtisticCream,
    background = ArtisticMaroonBg,
    onBackground = ArtisticCream,
    surface = ArtisticMaroonCard,
    onSurface = ArtisticCream,
    surfaceVariant = ArtisticMaroonDark,
    onSurfaceVariant = ArtisticCreamSub,
    outline = ArtisticAmberGold,
    outlineVariant = FestiveCardBorder
)

private val LightColorScheme = darkColorScheme( // Keep warm rich dark artisanal maroon background consistent
    primary = ArtisticAmberGold,
    onPrimary = ArtisticMaroonBg,
    primaryContainer = ArtisticAmberContainer,
    onPrimaryContainer = ArtisticCream,
    secondary = ArtisticAmberGlow,
    onSecondary = ArtisticMaroonBg,
    secondaryContainer = ArtisticMaroonSurface,
    onSecondaryContainer = ArtisticCream,
    tertiary = ArtisticAmberDeep,
    onTertiary = ArtisticCream,
    background = ArtisticMaroonBg,
    onBackground = ArtisticCream,
    surface = ArtisticMaroonCard,
    onSurface = ArtisticCream,
    surfaceVariant = ArtisticMaroonDark,
    onSurfaceVariant = ArtisticCreamSub,
    outline = ArtisticAmberGold,
    outlineVariant = FestiveCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
