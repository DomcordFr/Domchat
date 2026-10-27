package me.lucid.domchat.service

import me.lucid.domchat.data.ai.transformers.DocumentAsPromptTransformer
import me.lucid.domchat.data.ai.transformers.OcrTransformer
import me.lucid.domchat.data.ai.transformers.PlaceholderTransformer
import me.lucid.domchat.data.ai.transformers.UnsupportedFileTransformer
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatGenerationTransformersTest {
    @Test
    fun `default input transformers run OCR before unsupported file fallback`() {
        assertEquals(
            listOf(
                PlaceholderTransformer,
                DocumentAsPromptTransformer,
                OcrTransformer,
                UnsupportedFileTransformer,
            ),
            defaultChatInputTransformers,
        )
    }
}
