package me.lucid.domchat.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import me.lucid.domchat.ui.theme.OledBlackWhiteThemeId
import me.lucid.domchat.ui.theme.PresetTheme

val OledBlackWhiteThemePreset by lazy {
    PresetTheme(
        id = OledBlackWhiteThemeId,
        name = {
            Text("OLED Black + White")
        },
        standardLight = oledLightScheme,
        standardDark = oledDarkScheme,
    )
}

// Light Fallback (Minimalist High-Contrast Paper)
private val oledLightScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE5E5E5)
)

// True OLED Zero-Lux Pitch Black
private val oledDarkScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1A1A1A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFEDEDED),
    background = Color(0xFF000000), // Pure OLED 0-lux
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF080808),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFA0A0A0),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainer = Color(0xFF0E0E0E),
    surfaceContainerHigh = Color(0xFF151515),
    surfaceContainerHighest = Color(0xFF1C1C1C),
    outline = Color(0xFF262626),
    outlineVariant = Color(0xFF181818),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000)
)
