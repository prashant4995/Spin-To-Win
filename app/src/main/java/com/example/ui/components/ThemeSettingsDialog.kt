package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ColorPalette
import com.example.data.local.ThemeMode
import com.example.ui.theme.AppTheme

@Composable
fun ThemeSettingsDialog(
    currentThemeMode: ThemeMode,
    currentColorPalette: ColorPalette,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onColorPaletteSelected: (ColorPalette) -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = AppTheme.customColors

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("theme_settings_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = customColors.cardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DiyaLamp(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Theme & Colors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = customColors.primaryAccent
                        )
                        Text(
                            text = "रंग आणि देखावा बदला",
                            fontSize = 11.sp,
                            color = customColors.textSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(customColors.surfaceDark)
                        .testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = customColors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Appearance Mode (Light / Dark / System)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "APPEARANCE / देखावा",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = customColors.primaryAccent,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(customColors.surfaceDark)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeModeOption(
                            title = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_light_button")
                        )
                        ThemeModeOption(
                            title = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = { onThemeModeSelected(ThemeMode.DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_dark_button")
                        )
                        ThemeModeOption(
                            title = "System",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_system_button")
                        )
                    }
                }

                // Section 2: Festival Accent Palette
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLOR ACCENT / सण रंगसंगती",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = customColors.primaryAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentColorPalette.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = customColors.textSecondary
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("color_palette_picker"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ColorPalette.values()) { palette ->
                            ColorPaletteItem(
                                palette = palette,
                                isSelected = currentColorPalette == palette,
                                onSelect = { onColorPaletteSelected(palette) }
                            )
                        }
                    }
                }

                // Section 3: Live Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = customColors.surfaceDark
                    ),
                    border = BorderStroke(1.dp, customColors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Preview: ${currentColorPalette.marathiTitle}",
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = customColors.primaryAccent,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = if (customColors.isDark) "DARK MODE" else "LIGHT MODE",
                                    color = customColors.textOnAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "|| श्री गणेशाय नमः || - गणेशोत्सव भाग्यशाली चक्र",
                            color = customColors.textSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.primaryAccent,
                    contentColor = customColors.textOnAccent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = customColors.textOnAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "APPLY & SAVE",
                    color = customColors.textOnAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    )
}

@Composable
private fun ThemeModeOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = AppTheme.customColors
    val animBg by animateColorAsState(
        targetValue = if (isSelected) customColors.primaryAccent else Color.Transparent,
        animationSpec = tween(250),
        label = "mode_bg"
    )
    val animContent by animateColorAsState(
        targetValue = if (isSelected) customColors.textOnAccent else customColors.textSecondary,
        animationSpec = tween(250),
        label = "mode_content"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = animBg
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = animContent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animContent
            )
        }
    }
}

@Composable
private fun ColorPaletteItem(
    palette: ColorPalette,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val customColors = AppTheme.customColors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(4.dp)
            .testTag("palette_item_${palette.name}")
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.accentGlow,
                            palette.primaryColor,
                            palette.secondaryColor
                        )
                    )
                )
                .then(
                    if (isSelected) {
                        Modifier.border(2.5.dp, customColors.textPrimary, CircleShape)
                    } else {
                        Modifier.border(1.dp, customColors.cardBorder, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = palette.marathiTitle,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) customColors.primaryAccent else customColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}
