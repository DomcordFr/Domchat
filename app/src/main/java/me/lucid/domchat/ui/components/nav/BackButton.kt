package me.lucid.domchat.ui.components.nav

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.ui.context.LocalNavController
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics

val LocalBackButtonVisible = compositionLocalOf { true }

@Composable
fun BackButton(modifier: Modifier = Modifier) {
    if (!LocalBackButtonVisible.current) {
        Spacer(modifier = modifier.size(48.dp))
        return
    }

    val navController = LocalNavController.current
    val haptics = rememberPremiumHaptics()

    DomChatBackButton(
        onClick = { navController.popBackStack() },
        contentDescription = stringResource(R.string.back),
        modifier = modifier,
        onHaptic = { haptics.perform(HapticPattern.Pop) },
    )
}
