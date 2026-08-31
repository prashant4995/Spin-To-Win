package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String, val marathiTitle: String) {
    SYSTEM("System Default", "सिस्टम डीफॉल्ट"),
    LIGHT("Festive Light", "लाइट थीम"),
    DARK("Artisanal Dark", "डार्क थीम")
}

enum class ColorPalette(
    val title: String,
    val marathiTitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentGlow: Color
) {
    GOLD(
        title = "Festive Gold",
        marathiTitle = "सुनेहरा",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFF92400E),
        accentGlow = Color(0xFFF59E0B)
    ),
    SAFFRON(
        title = "Kesari Saffron",
        marathiTitle = "केशरी",
        primaryColor = Color(0xFFFF9800),
        secondaryColor = Color(0xFFE65100),
        accentGlow = Color(0xFFFFB74D)
    ),
    RUBY(
        title = "Royal Crimson",
        marathiTitle = "शाही लाल",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFB91C1C),
        accentGlow = Color(0xFFF87171)
    ),
    EMERALD(
        title = "Sacred Emerald",
        marathiTitle = "पाचू हिरवा",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF047857),
        accentGlow = Color(0xFF34D399)
    ),
    PEACOCK(
        title = "Peacock Blue",
        marathiTitle = "मोरपंखी निळा",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFF1D4ED8),
        accentGlow = Color(0xFF60A5FA)
    )
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.LIGHT,
    val palette: ColorPalette = ColorPalette.PEACOCK
)

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences? = try {
        val appContext = context.applicationContext ?: context
        appContext.getSharedPreferences("festive_stall_theme_prefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    private val _themeSettings = MutableStateFlow(loadSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private fun loadSettings(): ThemeSettings {
        val modeName = prefs?.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val paletteName = prefs?.getString(KEY_COLOR_PALETTE, ColorPalette.PEACOCK.name) ?: ColorPalette.PEACOCK.name

        val mode = try {
            ThemeMode.valueOf(modeName)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }

        val palette = try {
            ColorPalette.valueOf(paletteName)
        } catch (e: Exception) {
            ColorPalette.PEACOCK
        }

        return ThemeSettings(mode = mode, palette = palette)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
        _themeSettings.value = _themeSettings.value.copy(mode = mode)
    }

    fun setColorPalette(palette: ColorPalette) {
        prefs?.edit()?.putString(KEY_COLOR_PALETTE, palette.name)?.apply()
        _themeSettings.value = _themeSettings.value.copy(palette = palette)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_COLOR_PALETTE = "key_color_palette"

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context.applicationContext ?: context).also { INSTANCE = it }
            }
        }
    }
}
