package me.lucid.domchat.ui.pages.assistant

import android.content.Context
import android.net.Uri
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.utils.AssistantExportImport

object AssistantExportHelper {
    suspend fun exportJson(
        context: Context,
        assistant: Assistant,
        uri: Uri,
        mode: String = "card_v2",
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = when (mode) {
                "card_v2" -> AssistantExportImport.exportToCharacterCardV2(assistant, context)
                "lastchat_memories" -> AssistantExportImport.exportToLastChatBundle(assistant, context, includeMemories = true, includeLorebooks = false)
                "lastchat_lorebooks" -> AssistantExportImport.exportToLastChatBundle(assistant, context, includeMemories = false, includeLorebooks = true)
                "lastchat_both" -> AssistantExportImport.exportToLastChatBundle(assistant, context, includeMemories = true, includeLorebooks = true)
                else -> AssistantExportImport.exportToLastChatBundle(assistant, context, includeMemories = false, includeLorebooks = false)
            }
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(jsonStr.toByteArray())
            } ?: error("Unable to open output stream for $uri")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportPng(
        context: Context,
        assistant: Assistant,
        uri: Uri,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val bytes = AssistantExportImport.exportToCharacterCardPng(assistant, context)
            val success = if (bytes != null && bytes.isNotEmpty()) {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(bytes)
                } ?: error("Unable to open output stream for $uri")
                true
            } else {
                false
            }
            Result.success(success)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
