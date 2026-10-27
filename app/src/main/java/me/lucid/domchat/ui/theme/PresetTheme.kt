package me.lucid.domchat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import me.lucid.domchat.ui.theme.presets.AutumnThemePreset
import me.lucid.domchat.ui.theme.presets.BlackThemePreset
import me.lucid.domchat.ui.theme.presets.OceanThemePreset
import me.lucid.domchat.ui.theme.presets.OledBlackWhiteThemePreset
import me.lucid.domchat.ui.theme.presets.OledLiquidGlassThemePreset
import me.lucid.domchat.ui.theme.presets.SakuraThemePreset
import me.lucid.domchat.ui.theme.presets.SeafoamMintThemePreset
import me.lucid.domchat.ui.theme.presets.SpringThemePreset

const val SeafoamMintThemeId = "seafoam_mint"
const val OledBlackWhiteThemeId = "oled_black_white"
const val OledLiquidGlassThemeId = "oled_liquid_glass"

private const val LegacyNightskyBlueThemeId = "nightsky_blue"

data class PresetTheme(
    val id: String,
    val name: @Composable () -> Unit,
    val standardLight: ColorScheme,
    val standardDark: ColorScheme,
) {
    fun getColorScheme(dark: Boolean): ColorScheme {
        return if (dark) standardDark else standardLight
    }
}

val PresetThemes by lazy {
    listOf(
        OledLiquidGlassThemePreset,
        OledBlackWhiteThemePreset,
        SeafoamMintThemePreset,
        OceanThemePreset,
        SakuraThemePreset,
        SpringThemePreset,
        AutumnThemePreset,
        BlackThemePreset,
    )
}

fun normalizePresetThemeId(id: String): String {
    return when (id) {
        LegacyNightskyBlueThemeId -> SeafoamMintThemeId
        else -> id
    }
}

fun findPresetTheme(id: String): PresetTheme {
    val normalizedId = normalizePresetThemeId(id)
    return PresetThemes.find { it.id == normalizedId } ?: OledLiquidGlassThemePreset
}
