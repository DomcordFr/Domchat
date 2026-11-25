package me.lucid.tts.provider

import kotlinx.coroutines.flow.Flow
import me.lucid.tts.model.AudioChunk
import me.lucid.tts.model.TTSRequest

/** Provider-dispatch boundary shared by Android and iOS queue orchestration. */
interface TtsSpeechGenerator {
    fun generateSpeech(
        providerSetting: TTSProviderSetting,
        request: TTSRequest,
    ): Flow<AudioChunk>
}
