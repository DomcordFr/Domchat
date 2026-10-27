package me.lucid.domchat.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import me.lucid.domchat.R
import me.lucid.domchat.ui.theme.PresetTheme
import me.lucid.domchat.ui.theme.sakuraColorScheme

val SakuraThemePreset by lazy {
    PresetTheme(
        id = "sakura",
        name = { Text(stringResource(id = R.string.theme_name_sakura)) },
        standardLight = sakuraColorScheme(dark = false),
        standardDark = sakuraColorScheme(dark = true),
    )
}
