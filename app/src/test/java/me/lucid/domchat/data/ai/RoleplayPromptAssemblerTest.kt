package me.lucid.domchat.data.ai

import me.lucid.ai.core.MessageRole
import me.lucid.ai.ui.UIMessage
import me.lucid.domchat.data.model.Assistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleplayPromptAssemblerTest {

    @Test
    fun testSinglePassNonRecursiveMacroReplacement() {
        // If charName contains "{{user}}", single-pass evaluation must NOT substitute {{user}} inside charName!
        val text = "Hello {{char}}, your companion is {{user}}."
        val charName = "Dragon{{user}}Lord"
        val userName = "Alice"

        val result = RoleplayPromptAssembler.evaluateMacros(
            text = text,
            charName = charName,
            userName = userName
        )

        // Result should be: "Hello Dragon{{user}}Lord, your companion is Alice."
        // Under recursive replacement it would have expanded to "Hello DragonAliceLord..."
        assertEquals("Hello Dragon{{user}}Lord, your companion is Alice.", result)
    }

    @Test
    fun testSillyTavernMacroAliases() {
        val text = "<CHAR> welcomes <USER> with <BOT>."
        val result = RoleplayPromptAssembler.evaluateMacros(
            text = text,
            charName = "Seraphina",
            userName = "Bob"
        )
        assertEquals("Seraphina welcomes Bob with Seraphina.", result)
    }

    @Test
    fun testRandomMacroEvaluation() {
        val text = "The weather is {{random::sunny,rainy,cloudy}} today."
        val result = RoleplayPromptAssembler.evaluateMacros(
            text = text,
            charName = "Bot",
            userName = "User"
        )
        assertTrue(
            result == "The weather is sunny today." ||
            result == "The weather is rainy today." ||
            result == "The weather is cloudy today."
        )
    }

    @Test
    fun testPresetMessageDepthInjection() {
        val messages = listOf(
            UIMessage.user("Message 1"),
            UIMessage.assistant("Message 2"),
            UIMessage.user("Message 3"),
            UIMessage.assistant("Message 4")
        )

        val injected = RoleplayPromptAssembler.injectPresetMessage(
            messages = messages,
            content = "Keep responses in character.",
            depth = 1,
            role = MessageRole.SYSTEM
        )

        assertEquals(5, injected.size)
        // Injected 1 before the end -> index 3
        assertEquals("Keep responses in character.", injected[3].toContentText())
        assertEquals(MessageRole.SYSTEM, injected[3].role)
        assertEquals("Message 4", injected[4].toContentText())
    }
}
