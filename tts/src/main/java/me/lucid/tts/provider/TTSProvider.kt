package me.lucid.tts.provider

import kotlinx.coroutines.flow.Flow
import me.lucid.tts.model.AudioChunk
import me.lucid.tts.model.TTSModelInfo
import me.lucid.tts.model.TTSRequest

interface TTSProvider<T : TTSProviderSetting> {
    fun generateSpeech(
        providerSetting: T,
        request: TTSRequest
    ): Flow<AudioChunk>

    suspend fun listModels(providerSetting: T): List<TTSModelInfo> = emptyList()
}
