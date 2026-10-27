package me.lucid.domchat.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import me.lucid.domchat.R
import me.lucid.domchat.ui.theme.PresetTheme
import me.lucid.domchat.ui.theme.SeafoamMintThemeId
import me.lucid.domchat.ui.theme.seafoamMintColorScheme

val SeafoamMintThemePreset by lazy {
    PresetTheme(
        id = SeafoamMintThemeId,
        name = { Text(stringResource(id = R.string.theme_name_seafoam_mint)) },
        standardLight = seafoamMintColorScheme(dark = false),
        standardDark = seafoamMintColorScheme(dark = true),
    )
}
