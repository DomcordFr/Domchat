package me.lucid.domchat.ui.context

import androidx.compose.runtime.compositionLocalOf
import me.lucid.domchat.data.datastore.DisplaySetting
import me.lucid.domchat.data.datastore.Settings

val LocalSettings = compositionLocalOf<Settings> {
    error("No SettingsStore provided")
}

val LocalDisplaySetting = compositionLocalOf<DisplaySetting> {
    DisplaySetting()
}
