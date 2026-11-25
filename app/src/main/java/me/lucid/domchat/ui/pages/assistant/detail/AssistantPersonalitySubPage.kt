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
 * Personality sub-page - Dedicated modular workspace for character personality & system prompt.
 * Powered by DomChatMarkdownEditor with live formatting toolbar, {{char}}/{{user}} macros,
 * image embedding, and live preview.
 */
@Composable
fun AssistantPersonalitySubPage(
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
            value = assistant.systemPrompt,
            onValueChange = { onUpdate(assistant.copy(systemPrompt = it)) },
            placeholder = "Describe your character's personality, speaking style, behavioral guidelines, thoughts, quirks, and lore in Markdown...",
            minLines = 14,
            showMacros = true,
            allowImageEmbed = true
        )

        Spacer(Modifier.height(32.dp))
    }
}
