package me.lucid.tts.provider.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.lucid.common.platform.PlatformHttpClient
import me.lucid.common.platform.PlatformHttpRequest
import me.lucid.common.platform.PlatformLog
import me.lucid.tts.model.AudioChunk
import me.lucid.tts.model.AudioFormat
import me.lucid.tts.model.TTSModelInfo
import me.lucid.tts.model.TTSRequest
import me.lucid.tts.provider.TTSProvider
import me.lucid.tts.provider.TTSProviderSetting

private const val TAG = "FishAudioTTSProvider"

class FishAudioTTSProvider(
    private val httpClient: PlatformHttpClient,
) : TTSProvider<TTSProviderSetting.FishAudio> {

    override fun generateSpeech(
        providerSetting: TTSProviderSetting.FishAudio,
        request: TTSRequest
    ): Flow<AudioChunk> = flow {
        val requestBody = buildJsonObject {
            put("text", request.text)
            if (providerSetting.referenceId.isNotBlank()) {
                put("reference_id", providerSetting.referenceId)
            }
            put("temperature", providerSetting.temperature.toTtsJsonNumber())
            put("top_p", providerSetting.topP.toDouble())
            put("format", providerSetting.format)
            put("prosody", buildJsonObject {
                put("speed", providerSetting.speed.toTtsJsonNumber())
            })
            put("normalize", true)
            put("chunk_length", 300)
        }

        PlatformLog.i(
            TAG,
            "generateSpeech: model=${providerSetting.model}, " +
                "referenceId=${providerSetting.referenceId}, textLength=${request.text.length}"
        )

        val cleanBaseUrl = providerSetting.baseUrl.trim().trimEnd('/')
        val response = httpClient.execute(
            PlatformHttpRequest(
                method = "POST",
                url = "$cleanBaseUrl/v1/tts",
                headers = mapOf(
                    "Authorization" to "Bearer ${providerSetting.apiKey.trim()}",
                    "Content-Type" to "application/json",
                    "model" to providerSetting.model.trim(),
                ),
                body = requestBody.toString().encodeToByteArray(),
                mediaType = "application/json",
            )
        )

        if (response.statusCode !in 200..299) {
            val errorBody = response.body.decodeToString()
            PlatformLog.e(TAG, "TTS request failed: ${response.statusCode} $errorBody")
            throw Exception("Fish Audio TTS failed: $errorBody")
        }

        val format = when (providerSetting.format.lowercase()) {
            "wav" -> AudioFormat.WAV
            "pcm" -> AudioFormat.PCM
            "opus" -> AudioFormat.OPUS
            else -> AudioFormat.MP3
        }

        emit(
            AudioChunk(
                data = response.body,
                format = format,
                isLast = true,
                metadata = mapOf(
                    "provider" to "fishaudio",
                    "model" to providerSetting.model,
                    "voice" to providerSetting.referenceId
                )
            )
        )
    }

    override suspend fun listModels(
        providerSetting: TTSProviderSetting.FishAudio
    ): List<TTSModelInfo> = listOf(
        TTSModelInfo("s2.1-pro-free", "Fish Audio S2.1 Pro (Free)"),
        TTSModelInfo("s2-pro", "Fish Audio S2 Pro"),
        TTSModelInfo("s1", "Fish Audio S1"),
    )
}
