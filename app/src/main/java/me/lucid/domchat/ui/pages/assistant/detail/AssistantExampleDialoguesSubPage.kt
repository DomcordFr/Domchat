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
 * Example Dialogues sub-page - Dedicated modular workspace for entering character example dialogues (mes_example).
 * Teaches the model speech patterns, formatting, formatting tropes, and interaction cadence.
 */
@Composable
fun AssistantExampleDialoguesSubPage(
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
            value = assistant.exampleDialogue ?: "",
            onValueChange = { onUpdate(assistant.copy(exampleDialogue = it.takeIf { t -> t.isNotBlank() })) },
            placeholder = "<START>\n{{user}}: What brings you here?\n{{char}}: *leans back against the counter, crossing arms* Just keeping an eye on things.",
            minLines = 14,
            showMacros = true,
            allowImageEmbed = true
        )

        Spacer(Modifier.height(32.dp))
    }
}
