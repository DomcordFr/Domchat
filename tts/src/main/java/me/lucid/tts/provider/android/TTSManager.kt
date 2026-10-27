package me.lucid.tts.provider.android

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.lucid.common.platform.android.OkHttpPlatformHttpClient
import me.lucid.tts.model.AudioChunk
import me.lucid.tts.model.TTSModelInfo
import me.lucid.tts.model.TTSRequest
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.tts.provider.TtsSpeechGenerator
import me.lucid.tts.provider.providers.CartesiaTTSProvider
import me.lucid.tts.provider.providers.ElevenLabsTTSProvider
import me.lucid.tts.provider.providers.FishAudioTTSProvider
import me.lucid.tts.provider.providers.GeminiTTSProvider
import me.lucid.tts.provider.providers.MiniMaxTTSProvider
import me.lucid.tts.provider.providers.OpenAITTSProvider
import me.lucid.tts.provider.providers.PlayHTTTSProvider
import me.lucid.tts.provider.providers.QwenTTSProvider
import me.lucid.tts.provider.providers.android.SystemTTSProvider
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.seconds

class TTSManager(private val context: Context) : TtsSpeechGenerator {
    private val openAIProvider = OpenAITTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(30.seconds)
                .build()
        )
    )
    private val geminiProvider = GeminiTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(30.seconds)
                .build()
        )
    )
    private val systemProvider = SystemTTSProvider(context)
    private val miniMaxProvider = MiniMaxTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(60.seconds)
                .build()
        )
    )
    private val elevenLabsProvider = ElevenLabsTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(60.seconds)
                .build()
        )
    )
    private val qwenProvider = QwenTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(120.seconds)
                .build()
        )
    )
    private val fishAudioProvider = FishAudioTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(60.seconds)
                .build()
        )
    )
    private val cartesiaProvider = CartesiaTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(60.seconds)
                .build()
        )
    )
    private val playHTProvider = PlayHTTTSProvider(
        httpClient = OkHttpPlatformHttpClient(
            OkHttpClient.Builder()
                .readTimeout(90.seconds)
                .build()
        )
    )

    override fun generateSpeech(
        providerSetting: TTSProviderSetting,
        request: TTSRequest
    ): Flow<AudioChunk> {
        return when (providerSetting) {
            is TTSProviderSetting.OpenAI -> openAIProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.Gemini -> geminiProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.SystemTTS -> systemProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.MiniMax -> miniMaxProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.ElevenLabs -> elevenLabsProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.Qwen -> qwenProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.FishAudio -> fishAudioProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.Cartesia -> cartesiaProvider.generateSpeech(providerSetting, request)
            is TTSProviderSetting.PlayHT -> playHTProvider.generateSpeech(providerSetting, request)
        }
    }

    suspend fun listModels(providerSetting: TTSProviderSetting): List<TTSModelInfo> =
        withContext(Dispatchers.IO) {
            when (providerSetting) {
                is TTSProviderSetting.OpenAI -> openAIProvider.listModels(providerSetting)
                is TTSProviderSetting.Gemini -> geminiProvider.listModels(providerSetting)
                is TTSProviderSetting.SystemTTS -> emptyList()
                is TTSProviderSetting.MiniMax -> emptyList()
                is TTSProviderSetting.ElevenLabs -> elevenLabsProvider.listModels(providerSetting)
                is TTSProviderSetting.Qwen -> emptyList()
                is TTSProviderSetting.FishAudio -> fishAudioProvider.listModels(providerSetting)
                is TTSProviderSetting.Cartesia -> cartesiaProvider.listModels(providerSetting)
                is TTSProviderSetting.PlayHT -> playHTProvider.listModels(providerSetting)
            }
        }
}
