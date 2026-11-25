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
 * Creator Notes sub-page - Dedicated modular workspace for character creator notes.
 * Supports Markdown text authoring, native gallery image embedding,
 * and live Markdown preview.
 */
@Composable
fun AssistantCreatorNotesSubPage(
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
            value = assistant.creatorNotes ?: "",
            onValueChange = { onUpdate(assistant.copy(creatorNotes = it.takeIf { t -> t.isNotBlank() })) },
            placeholder = "Write character lore, creator notes, instructions, and embed native images directly into the notes...",
            minLines = 14,
            showMacros = true,
            allowImageEmbed = true
        )

        Spacer(Modifier.height(32.dp))
    }
}
