package me.lucid.domchat.ui.context

import androidx.compose.runtime.compositionLocalOf
import me.lucid.domchat.ui.hooks.CustomSttState

val LocalSTTState = compositionLocalOf<CustomSttState> { error("Not provided yet") }
