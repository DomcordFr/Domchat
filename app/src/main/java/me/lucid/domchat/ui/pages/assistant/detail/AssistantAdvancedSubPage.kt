package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.lucid.domchat.R
import me.lucid.domchat.data.datastore.TtsAutoplayMode
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.context.LocalSettings
import me.lucid.domchat.ui.components.ui.Select
import me.lucid.domchat.ui.pages.setting.components.SettingsGroup
import me.lucid.domchat.ui.pages.setting.components.SettingGroupItem
import me.lucid.domchat.ui.theme.AppShapes

/**
 * Advanced tab - TTS autoplay, message formatting, and custom request settings.
 */
@Composable
fun AssistantAdvancedSubPage(
    assistant: Assistant,
    onUpdate: (Assistant) -> Unit
) {
    val settings = LocalSettings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SettingsGroup(title = "TTS") {
            val autoplayOptions: List<TtsAutoplayMode?> = listOf(
                null,
                TtsAutoplayMode.WHILE_GENERATING,
                TtsAutoplayMode.OFF,
            )
            SettingGroupItem(
                title = "TTS Autoplay",
                subtitle = "Override automatic reading for this character",
                trailing = {
                    Select(
                        options = autoplayOptions,
                        selectedOption = assistant.ttsAutoplayMode,
                        onOptionSelected = { mode -> onUpdate(assistant.copy(ttsAutoplayMode = mode)) },
                        optionToString = { mode ->
                            when (mode) {
                                null -> if (settings.ttsAutoplayMode == TtsAutoplayMode.OFF) {
                                    "Use global: Off"
                                } else {
                                    "Use global: On"
                                }
                                TtsAutoplayMode.OFF -> "Off"
                                TtsAutoplayMode.AFTER_GENERATION,
                                TtsAutoplayMode.WHILE_GENERATING -> "On"
                            }
                        },
                    )
                }
            )
        }

        SettingsGroup(title = stringResource(R.string.assistant_advanced_message_formatting)) {
            MessageTemplateSettingsCard(
                assistant = assistant,
                onUpdate = onUpdate
            )

            MessageRegexSettingsCard(
                assistant = assistant,
                onUpdate = onUpdate
            )
        }

        // Custom request settings
        SettingsGroup(title = stringResource(R.string.assistant_advanced_custom_request)) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = AppShapes.CardMedium
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    CustomHeaders(
                        headers = assistant.customHeaders,
                        onUpdate = { onUpdate(assistant.copy(customHeaders = it)) }
                    )
                }
            }

            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = AppShapes.CardMedium
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    CustomBodies(
                        customBodies = assistant.customBodies,
                        onUpdate = { onUpdate(assistant.copy(customBodies = it)) }
                    )
                }
            }
        }
    }
}
