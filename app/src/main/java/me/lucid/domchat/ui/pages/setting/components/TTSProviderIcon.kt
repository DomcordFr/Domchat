package me.lucid.domchat.ui.pages.setting.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.lucid.domchat.data.ai.models.ModelCatalogSnapshot
import me.lucid.domchat.data.ai.models.ttsProviderIconUri
import me.lucid.domchat.ui.components.ui.AutoAIIconWithUrl
import me.lucid.tts.provider.TTSProviderSetting

@Composable
fun TTSProviderIcon(
    provider: TTSProviderSetting,
    catalogSnapshot: ModelCatalogSnapshot?,
    modifier: Modifier = Modifier.size(40.dp),
    tint: Color = LocalContentColor.current
) {
    val isSystem = provider is TTSProviderSetting.SystemTTS ||
        provider.id == me.lucid.domchat.data.datastore.DEFAULT_SYSTEM_TTS_ID ||
        provider.name.contains("system", ignoreCase = true) ||
        provider.name.isEmpty()

    if (isSystem) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = if (tint != Color.Unspecified) tint else Color.White
            )
        }
    } else {
        AutoAIIconWithUrl(
            name = provider.name,
            customIconUri = provider.customIconUri ?: catalogSnapshot?.ttsProviderIconUri(provider.name),
            modifier = modifier
        )
    }
}
