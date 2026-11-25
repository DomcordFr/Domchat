package me.lucid.domchat.ui.context

import androidx.compose.runtime.compositionLocalOf
import me.lucid.domchat.ui.hooks.CustomTtsState

val LocalTTSState = compositionLocalOf<CustomTtsState> { error("Not provided yet") }
