package me.lucid.domchat.data.sync.importer

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import me.lucid.ai.core.MessageRole
import me.lucid.ai.ui.MessageNode
import me.lucid.ai.ui.UIMessage
import me.lucid.ai.ui.UIMessagePart
import me.lucid.common.http.jsonArrayOrNull
import me.lucid.common.http.jsonObjectOrNull
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.sanitizeDisplayText
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.utils.BoundedStreams
import me.lucid.domchat.utils.BoundedStreams.readBoundedBytes
import me.lucid.domchat.utils.JsonInstant
import me.lucid.domchat.utils.jsonPrimitiveOrNull
import java.io.File
import java.io.InputStream
import java.time.Instant
import kotlin.time.Clock
import kotlin.time.Instant as KotlinInstant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.Uuid
import java.util.UUID

object SillyTavernImporter {
    data class Result(
        val conversation: Conversation,
        val assistants: List<Assistant> = emptyList(),
    )

    fun import(file: File): Result {
        return file.inputStream().use { import(it, emptyList()) }
    }

    fun import(json: String): Result {
        return import(json, emptyList())
    }

    fun import(stream: InputStream, assistants: List<Assistant>): Result {
        return stream.use { input ->
            val json = input.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES).decodeToString()
            import(json, assistants)
        }
    }

    private fun import(json: String, assistants: List<Assistant>): Result {
        val root = JsonInstant.parseToJsonElement(json)
        val elements = when (root) {
            is JsonArray -> root
            is JsonObject -> root["messages"]?.jsonArrayOrNull
                ?: root["conversation"]?.jsonArrayOrNull
                ?: listOf(root)
            else -> emptyList()
        }

        var header: Header? = null
        val uiMessages = mutableListOf<UIMessage>()

        elements.forEach { element ->
            when {
                looksLikeHeader(element) -> header = parseHeader(element)
                else -> parseMessage(element)?.let { uiMessages.add(it) }
            }
        }

        val resolvedHeader = header ?: Header("", "", "SillyTavern Import")
        val assistant = Assistant(
            name = sanitizeDisplayText(resolvedHeader.characterName) ?: "Imported Character",
            creator = sanitizeDisplayText(resolvedHeader.userName),
            systemPrompt = "",
            tags = resolvedHeader.tags.mapNotNull { tagToUuid(it) },
        )
        val conversation = Conversation(
            id = Uuid.random(),
            assistantId = assistant.id,
            title = sanitizeDisplayText(resolvedHeader.title) ?: "SillyTavern Import",
            messageNodes = uiMessages.map { MessageNode.of(it) },
            createAt = Instant.now(),
            updateAt = Instant.now(),
        )

        return Result(
            conversation = conversation,
            assistants = assistants,
        )
    }

    private data class Header(
        val characterName: String,
        val userName: String,
        val title: String,
        val tags: List<String> = emptyList(),
    )

    private fun looksLikeHeader(element: JsonElement): Boolean {
        val obj = element.jsonObjectOrNull ?: return false
        // A header carries identity fields (title / character_name / user_name / chat_title)
        // but must never carry message body (mes / content). Regular messages with a name
        // field always carry mes or content, so they are never misclassified as a header.
        return (obj["mes"] == null && obj["content"] == null)
            && (obj["title"] != null || obj["character_name"] != null || obj["user_name"] != null || obj["chat_title"] != null)
    }

    private fun parseHeader(element: JsonElement): Header {
        val obj = element.jsonObjectOrNull ?: return Header("", "", "SillyTavern Import")
        val characterName = obj.string("character_name", "name")?.ifBlank { null } ?: ""
        val userName = obj.string("user_name")?.ifBlank { null } ?: ""
        val title = characterName.ifBlank { obj.string("title") ?: "SillyTavern Import" }
        val tags = obj.tags()
        return Header(characterName, userName, title, tags)
    }

    private fun parseMessage(element: JsonElement): UIMessage? {
        val obj = element.jsonObjectOrNull ?: return null
        val body = obj.string("mes", "content")?.takeIf { it.isNotBlank() }
            ?: return null

        val role = when (obj.string("role")?.lowercase()) {
            "assistant" -> MessageRole.ASSISTANT
            "system" -> MessageRole.SYSTEM
            else -> MessageRole.USER
        }

        val parts = mutableListOf<UIMessagePart>()
        parts.add(UIMessagePart.Text(text = body))

        parseSwipes(obj["swipes"]?.jsonArrayOrNull).forEach { swipeText ->
            parts.add(
                UIMessagePart.Text(
                    text = swipeText,
                    metadata = buildJsonObject { put("source", "sillytavern_swipe") },
                )
            )
        }

        if (parts.isEmpty()) return null

        return UIMessage(
            id = obj.string("id")?.toUuidOrNull() ?: Uuid.random(),
            role = role,
            parts = parts,
            createdAt = obj.long("timestamp", "time", "created", "createdAt")
                ?.let { KotlinInstant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        )
    }

    private fun parseSwipes(swipes: JsonArray?): List<String> {
        if (swipes == null) return emptyList()
        return swipes.mapNotNull { swipe ->
            val swipeText = when (swipe) {
                // JSON string alternative: ["swipe 1 text", "swipe 2 text"]
                is JsonPrimitive -> swipe.jsonPrimitiveOrNull?.contentOrNull as? String
                // JSON object alternative: { "mes": "swipe text", ... }
                is JsonObject -> swipe.string("mes")
                else -> null
            }
            swipeText?.takeIf { it.isNotBlank() }
        }
    }

    private fun JsonObject.string(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key ->
            (this[key]?.jsonPrimitiveOrNull?.contentOrNull as? String)
        }
    }

    private fun JsonObject.long(vararg keys: String): Long? {
        return keys.firstNotNullOfOrNull { key ->
            this[key]?.jsonPrimitiveOrNull?.longOrNull
                ?: ((this[key]?.jsonPrimitiveOrNull?.contentOrNull as? String)?.toLongOrNull())
        }
    }

    private fun JsonObject.tags(): List<String> {
        val tagsElement = this["data"]?.jsonObjectOrNull?.let { it["tags"] } ?: this["tags"]
        return tagsElement?.jsonArrayOrNull
            ?.mapNotNull { it.jsonPrimitiveOrNull?.contentOrNull as? String }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    private fun String?.toUuidOrNull(): Uuid? {
        return this?.let { runCatching { Uuid.parse(it) }.getOrNull() }
    }

    private fun tagToUuid(tag: String): Uuid? {
        val javaUuid = UUID.nameUUIDFromBytes(tag.toByteArray())
        return runCatching { Uuid.parse(javaUuid.toString()) }.getOrNull()
    }
}
