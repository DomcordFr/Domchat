package me.lucid.domchat.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import me.lucid.domchat.data.datastore.Settings

val LocalSettings = staticCompositionLocalOf<Settings> {
    error("No SettingsStore provided")
}
