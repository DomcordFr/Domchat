package me.lucid.domchat.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import me.lucid.domchat.ui.theme.OledLiquidGlassThemeId
import me.lucid.domchat.ui.theme.PresetTheme

val OledLiquidGlassThemePreset by lazy {
    PresetTheme(
        id = OledLiquidGlassThemeId,
        name = {
            Text("OLED Liquid Glass")
        },
        standardLight = liquidGlassLightScheme,
        standardDark = liquidGlassDarkScheme,
    )
}

// Light Glass Fallback
private val liquidGlassLightScheme = lightColorScheme(
    primary = Color(0xFF1C1C1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5EA),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA)
)

// True OLED Apple Liquid Glass Dark Scheme
private val liquidGlassDarkScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2C2C2E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFEBEBF5),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1C1C1E),
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000), // Pure OLED 0-lux
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF14141A),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF1E1E26),
    onSurfaceVariant = Color(0xFFA5A5B0),
    surfaceContainerLowest = Color(0xFF08080C),
    surfaceContainerLow = Color(0xFF121218),
    surfaceContainer = Color(0xFF1A1B22),
    surfaceContainerHigh = Color(0xFF24252E),
    surfaceContainerHighest = Color(0xFF2D2E3A),
    outline = Color(0xFF484856),
    outlineVariant = Color(0xFF323340),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000)
)
