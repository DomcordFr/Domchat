package me.lucid.domchat.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import me.lucid.domchat.ui.components.ui.AppToasterState

val LocalToaster = staticCompositionLocalOf<AppToasterState> { error("Not provided") }