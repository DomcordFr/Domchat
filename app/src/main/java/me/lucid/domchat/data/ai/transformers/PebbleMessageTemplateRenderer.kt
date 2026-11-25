package me.lucid.domchat.data.ai.transformers

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.Loader
import me.lucid.ai.core.MessageRole
import me.lucid.ai.util.MessageTemplateContext
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.getActivePersona
import me.lucid.domchat.data.model.UserPersona
import me.lucid.domchat.utils.toLocalDate
import me.lucid.domchat.utils.toLocalTime
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.time.Instant

interface MessageTemplateCache {
    fun invalidateAll()
}

interface MessageTemplateClock {
    fun now(): Instant
}

object AndroidMessageTemplateClock : MessageTemplateClock {
    override fun now(): Instant = Instant.now()
}

class AndroidMessageTemplateContextFactory(
    private val settingsStore: SettingsStore,
    private val clock: MessageTemplateClock = AndroidMessageTemplateClock,
) : MessageTemplateContextFactory {
    override fun build(message: String, role: MessageRole): Map<String, Any?> {
        val now = clock.now()
        val base = MessageTemplateContext.build(
            message = message,
            role = role,
            time = now.toLocalTime(),
            date = now.toLocalDate(),
        ).asMap().toMutableMap()

        // Add user persona pronoun macros to Pebble context: {{sub}}, {{obj}}, {{poss}}, {{poss_p}}, {{ref}}
        val activePersona: UserPersona = settingsStore.settingsFlow.value.displaySetting.getActivePersona()
        val pronouns = activePersona.pronouns
        if (pronouns.isNotBlank()) {
            val parts = pronouns.split("/").map { it.trim() }.filter { it.isNotEmpty() }
            val sub = parts.getOrElse(0) { "" }
            val obj = parts.getOrElse(1) { "" }
            val poss = parts.getOrElse(2) { obj }
            val possP = parts.getOrElse(3) { poss }
            val ref = parts.getOrElse(4) { reflexiveFor(obj) }
            base["sub"] = sub
            base["obj"] = obj
            base["poss"] = poss
            base["poss_p"] = possP
            base["ref"] = ref
        }

        return base
    }

    private fun reflexiveFor(obj: String): String {
        return when (obj.lowercase()) {
            "them" -> "themselves"
            else -> if (obj.isNotBlank()) obj + "self" else ""
        }
    }
}

class PebbleMessageTemplateRenderer(
    private val engine: PebbleEngine,
) : MessageTemplateRenderer, MessageTemplateCache {
    override fun render(templateId: String, context: Map<String, Any?>): String {
        val output = StringWriter()
        engine.getTemplate(templateId).evaluate(output, context)
        return output.toString()
    }

    override fun invalidateAll() {
        engine.templateCache.invalidateAll()
    }
}

class AssistantTemplateLoader(private val settingsStore: SettingsStore) : Loader<String> {
    override fun getReader(cacheKey: String?): Reader? {
        val content = settingsStore.settingsFlow.value.assistants
            .find { it.id.toString() == cacheKey }?.messageTemplate
            ?: return null
        return StringReader(content)
    }

    override fun setCharset(charset: String?) {}

    override fun setPrefix(prefix: String?) {}

    override fun setSuffix(suffix: String?) {}

    override fun resolveRelativePath(
        relativePath: String?,
        anchorPath: String?,
    ): String? {
        return relativePath
    }

    override fun createCacheKey(templateName: String?): String? {
        return templateName
    }

    override fun resourceExists(templateName: String?): Boolean {
        return settingsStore.settingsFlow.value.assistants.any { it.id.toString() == templateName }
    }
}
