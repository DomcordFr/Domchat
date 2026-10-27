package me.lucid.ai.provider.providers.openai

import kotlinx.coroutines.flow.Flow
import me.lucid.ai.provider.ProviderSetting
import me.lucid.ai.provider.TextGenerationParams
import me.lucid.ai.ui.MessageChunk
import me.lucid.ai.ui.UIMessage

interface OpenAIImpl {
    suspend fun generateText(
        providerSetting: ProviderSetting.OpenAI,
        messages: List<UIMessage>,
        params: TextGenerationParams,
    ): MessageChunk

    suspend fun streamText(
        providerSetting: ProviderSetting.OpenAI,
        messages: List<UIMessage>,
        params: TextGenerationParams,
    ): Flow<MessageChunk>
}
