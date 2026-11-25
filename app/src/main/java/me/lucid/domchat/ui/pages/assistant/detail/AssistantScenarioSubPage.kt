package me.lucid.domchat.ui.pages.assistant.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.ui.components.ui.DomChatMarkdownEditor

/**
 * Scenario sub-page - Dedicated modular workspace for character scenario setup.
 * Strictly distinct from Author's Notes. Scenario defines the scene, situation,
 * circumstances, and world state that the roleplay conversation begins in.
 */
@Composable
fun AssistantScenarioSubPage(
    assistant: Assistant,
    onUpdate: (Assistant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DomChatMarkdownEditor(
            value = assistant.scenario ?: "",
            onValueChange = { onUpdate(assistant.copy(scenario = it.takeIf { t -> t.isNotBlank() })) },
            placeholder = "Define the current scene, time, location, environment, and circumstances in which {{char}} and {{user}} meet...",
            minLines = 14,
            showMacros = true,
            allowImageEmbed = true
        )

        Spacer(Modifier.height(32.dp))
    }
}
