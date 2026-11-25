package me.lucid.domchat.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.IOException
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ProviderSetting
import me.lucid.ai.provider.withComfyDefaults
import me.lucid.domchat.AppScope
import me.lucid.domchat.data.ai.transformers.MessageTemplateCache
import me.lucid.domchat.data.ai.mcp.McpServerConfig
import me.lucid.domchat.data.ai.prompts.DEFAULT_LEARNING_MODE_PROMPT
import me.lucid.domchat.data.ai.prompts.DEFAULT_OCR_PROMPT
import me.lucid.domchat.data.ai.prompts.DEFAULT_SUGGESTION_PROMPT
import me.lucid.domchat.data.ai.prompts.DEFAULT_TITLE_PROMPT
import me.lucid.domchat.data.ai.prompts.DEFAULT_TRANSLATION_PROMPT
import me.lucid.domchat.data.ai.prompts.normalizeSuggestionPrompt
import me.lucid.domchat.data.datastore.migration.PreferenceStoreV1Migration
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.ChatStorageSettings
import me.lucid.domchat.data.model.Lorebook
import me.lucid.domchat.data.model.Mode
import me.lucid.domchat.data.model.Skill
import me.lucid.domchat.data.model.Tag
import me.lucid.domchat.data.model.AssistantOverlayConfig
import me.lucid.domchat.data.model.TextSelectionConfig
import me.lucid.domchat.ui.theme.PresetThemes
import me.lucid.domchat.utils.JsonInstant
import me.lucid.domchat.utils.toMutableStateFlow
import me.lucid.search.SearchCommonOptions
import me.lucid.search.SearchServiceOptions
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.tts.provider.withDefaultVoices
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.uuid.Uuid

private const val TAG = "PreferencesStore"

private val Context.settingsStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            PreferenceStoreV1Migration()
        )
    }
)

class SettingsStore(
    context: Context,
    scope: AppScope,
    private val quickCache: QuickSettingsCache,
    private val secretKeyManager: SecretKeyManager,
) : KoinComponent {
    companion object {
        // 版本号
        val VERSION = intPreferencesKey("data_version")

        // UI设置
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val THEME_ID = stringPreferencesKey("theme_id")
        val DISPLAY_SETTING = stringPreferencesKey("display_setting")
        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
        val ENABLE_RAG_LOGGING = booleanPreferencesKey("enable_rag_logging")

        // 模型选择
        val ENABLE_WEB_SEARCH = booleanPreferencesKey("enable_web_search")
        val FAVORITE_MODELS = stringPreferencesKey("favorite_models")
        val RECENT_MODEL_IDS = stringPreferencesKey("recent_model_ids")
        val SELECT_MODEL = stringPreferencesKey("chat_model")
        val TITLE_MODEL = stringPreferencesKey("title_model")
        val TITLE_THINKING_BUDGET = intPreferencesKey("title_thinking_budget")
        val SUMMARIZER_MODEL = stringPreferencesKey("summarizer_model")
        val SUMMARIZER_THINKING_BUDGET = intPreferencesKey("summarizer_thinking_budget")
        val SUBAGENT_MODEL = stringPreferencesKey("subagent_model")
        val SUBAGENT_THINKING_BUDGET = intPreferencesKey("subagent_thinking_budget")
        val TRANSLATE_MODEL = stringPreferencesKey("translate_model")
        val SUGGESTION_MODEL = stringPreferencesKey("suggestion_model")
        val SUGGESTION_THINKING_BUDGET = intPreferencesKey("suggestion_thinking_budget")
        val IMAGE_GENERATION_MODEL = stringPreferencesKey("image_generation_model")
        val TITLE_PROMPT = stringPreferencesKey("title_prompt")
        val TRANSLATION_PROMPT = stringPreferencesKey("translation_prompt")
        val SUGGESTION_PROMPT = stringPreferencesKey("suggestion_prompt")
        val LEARNING_MODE_PROMPT = stringPreferencesKey("learning_mode_prompt")
        val OCR_MODEL = stringPreferencesKey("ocr_model")
        val OCR_THINKING_BUDGET = intPreferencesKey("ocr_thinking_budget")
        val OCR_PROMPT = stringPreferencesKey("ocr_prompt")
        val EMBEDDING_MODEL = stringPreferencesKey("embedding_model")

        // 提供商
        val PROVIDERS = stringPreferencesKey("providers")

        // 助手
        val SELECT_ASSISTANT = stringPreferencesKey("select_assistant")
        val ASSISTANTS = stringPreferencesKey("assistants")
        val ASSISTANT_TAGS = stringPreferencesKey("assistant_tags")
        val PROVIDER_TAGS = stringPreferencesKey("provider_tags")
        val RECENTLY_USED_ASSISTANTS = stringPreferencesKey("recently_used_assistants")

        // 搜索
        val SEARCH_SERVICES = stringPreferencesKey("search_services")
        val SEARCH_COMMON = stringPreferencesKey("search_common")
        val SEARCH_SELECTED = intPreferencesKey("search_selected")

        // MCP
        val MCP_SERVERS = stringPreferencesKey("mcp_servers")

        // WebDAV
        val WEBDAV_CONFIG = stringPreferencesKey("webdav_config")

        // TTS
        val TTS_PROVIDERS = stringPreferencesKey("tts_providers")
        val SELECTED_TTS_PROVIDER = stringPreferencesKey("selected_tts_provider")
        val SELECTED_TTS_VOICE = stringPreferencesKey("selected_tts_voice")
        val TTS_AUTOPLAY_MODE = stringPreferencesKey("tts_autoplay_mode")

        // STT
        val STT_MODEL = stringPreferencesKey("stt_model")
        val STT_THINKING_BUDGET = intPreferencesKey("stt_thinking_budget")
        val STT_PROMPT = stringPreferencesKey("stt_prompt")

        // Web Server
        val WEB_SERVER_ENABLED = booleanPreferencesKey("web_server_enabled")
        val WEB_SERVER_PORT = intPreferencesKey("web_server_port")
        val WEB_SERVER_JWT_ENABLED = booleanPreferencesKey("web_server_jwt_enabled")
        val WEB_SERVER_ACCESS_PASSWORD = stringPreferencesKey("web_server_access_password")
        val WEB_SERVER_BACKGROUND_SETUP_SHOWN = booleanPreferencesKey("web_server_background_setup_shown")

        // Prompt Injections
        val MODES = stringPreferencesKey("modes")
        val LOREBOOKS = stringPreferencesKey("lorebooks")
        val SKILLS = stringPreferencesKey("skills")
        val CHAT_STORAGE = stringPreferencesKey("chat_storage")

        // Android Integration
        val TEXT_SELECTION_CONFIG = stringPreferencesKey("text_selection_config")
        val ASSISTANT_OVERLAY_CONFIG = stringPreferencesKey("assistant_overlay_config")

        // Local Models
        val DEVICE_PERFORMANCE_PROFILE = stringPreferencesKey("device_performance_profile")
    }

    private val dataStore = context.settingsStore
    private val updateMutex = Mutex()

    // One-time migration flags
    private var hasPersistedLegacyModeMigration = false
    private var hasPersistedLegacySummarizerMigration = false

    // Memoized JSON decodes to avoid full-tree deserialization on unchanged preference keys
    private var lastProvidersJson: String? = null
    private var cachedProviders: List<ProviderSetting> = emptyList()

    private var lastAssistantsJson: String? = null
    private var cachedAssistants: List<Assistant> = emptyList()

    private var lastDisplaySettingJson: String? = null
    private var cachedDisplaySetting: DisplaySetting = DisplaySetting()

    private var lastLorebooksJson: String? = null
    private var cachedLorebooks: List<Lorebook> = emptyList()

    private var lastSkillsJson: String? = null
    private var cachedSkills: List<Skill> = emptyList()

    private var lastTtsProvidersJson: String? = null
    private var cachedTtsProviders: List<TTSProviderSetting> = DEFAULT_TTS_PROVIDERS

    private var lastModesJson: String? = null
    private var cachedModes: List<Mode> = emptyList()

    private var lastMcpServersJson: String? = null
    private var cachedMcpServers: List<McpServerConfig> = emptyList()

    private var lastTemplateSignature: Int? = null

    val settingsFlowRaw = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            runCatching {
                Settings(
                    setupCompleted = preferences[SETUP_COMPLETED] == true,
                    enableWebSearch = preferences[ENABLE_WEB_SEARCH] == true,
                    favoriteModels = decodePreferenceList(preferences[FAVORITE_MODELS]) ?: emptyList(),
                    recentModelIds = decodePreferenceList(preferences[RECENT_MODEL_IDS]) ?: emptyList(),
                    chatModelId = decodeUuid(preferences[SELECT_MODEL], GEMINI_2_5_FLASH_ID),
                    titleModelId = decodeUuid(preferences[TITLE_MODEL], GEMINI_2_5_FLASH_ID),
                    titleThinkingBudget = preferences[TITLE_THINKING_BUDGET] ?: 0,
                    summarizerModelId = decodeUuidOrNull(preferences[SUMMARIZER_MODEL]),
                    summarizerThinkingBudget = preferences[SUMMARIZER_THINKING_BUDGET] ?: 0,
                    subagentModelId = decodeUuidOrNull(preferences[SUBAGENT_MODEL]),
                    subagentThinkingBudget = preferences[SUBAGENT_THINKING_BUDGET] ?: 0,
                    translateModeId = decodeUuid(preferences[TRANSLATE_MODEL], GEMINI_2_5_FLASH_ID),
                    suggestionModelId = decodeUuid(preferences[SUGGESTION_MODEL], GEMINI_2_5_FLASH_ID),
                    suggestionThinkingBudget = preferences[SUGGESTION_THINKING_BUDGET] ?: 0,
                    imageGenerationModelId = decodeUuidOrNull(preferences[IMAGE_GENERATION_MODEL])
                        ?: Uuid.random(),
                    titlePrompt = preferences[TITLE_PROMPT] ?: DEFAULT_TITLE_PROMPT,
                    translatePrompt = preferences[TRANSLATION_PROMPT] ?: DEFAULT_TRANSLATION_PROMPT,
                    suggestionPrompt = normalizeSuggestionPrompt(
                        preferences[SUGGESTION_PROMPT] ?: DEFAULT_SUGGESTION_PROMPT
                    ),
                    learningModePrompt = preferences[LEARNING_MODE_PROMPT] ?: DEFAULT_LEARNING_MODE_PROMPT,
                    ocrModelId = decodeUuidOrNull(preferences[OCR_MODEL]) ?: Uuid.random(),
                    ocrThinkingBudget = preferences[OCR_THINKING_BUDGET] ?: 0,
                    ocrPrompt = preferences[OCR_PROMPT] ?: DEFAULT_OCR_PROMPT,
                    embeddingModelId = decodeUuidOrNull(preferences[EMBEDDING_MODEL]) ?: Uuid.random(),
                    assistantId = decodeUuid(preferences[SELECT_ASSISTANT], DEFAULT_ASSISTANT_ID),
                    assistantTags = decodePreferenceList(preferences[ASSISTANT_TAGS]) ?: emptyList(),
                    providerTags = decodePreferenceList(preferences[PROVIDER_TAGS]) ?: emptyList(),
                    providers = run {
                        val json = preferences[PROVIDERS]
                        if (json == lastProvidersJson && lastProvidersJson != null) {
                            cachedProviders
                        } else {
                            val decoded = decodePreferenceList<ProviderSetting>(json) ?: emptyList()
                            lastProvidersJson = json
                            cachedProviders = decoded
                            decoded
                        }
                    },
                    assistants = run {
                        val json = preferences[ASSISTANTS]
                        if (json == lastAssistantsJson && lastAssistantsJson != null) {
                            cachedAssistants
                        } else {
                            val decoded = decodePreferenceList<Assistant>(json) ?: emptyList()
                            lastAssistantsJson = json
                            cachedAssistants = decoded
                            decoded
                        }
                    },
                    recentlyUsedAssistants = decodePreferenceList(preferences[RECENTLY_USED_ASSISTANTS])
                        ?: emptyList(),
                    dynamicColor = preferences[DYNAMIC_COLOR] == true,
                    themeId = preferences[THEME_ID] ?: PresetThemes[0].id,
                    developerMode = preferences[DEVELOPER_MODE] == true,
                    enableRagLogging = preferences[ENABLE_RAG_LOGGING] == true,
                    displaySetting = run {
                        val json = preferences[DISPLAY_SETTING]
                        if (json == lastDisplaySettingJson && lastDisplaySettingJson != null) {
                            cachedDisplaySetting
                        } else {
                            val decoded = decodePreferenceValue(
                                json,
                                DisplaySetting(),
                            ).normalizeFontSettings()
                            lastDisplaySettingJson = json
                            cachedDisplaySetting = decoded
                            decoded
                        }
                    },
                    searchServices = decodePreferenceList<SearchServiceOptions>(preferences[SEARCH_SERVICES])
                        ?: listOf(SearchServiceOptions.DEFAULT),
                    searchCommonOptions = decodePreferenceValue(
                        preferences[SEARCH_COMMON],
                        SearchCommonOptions(),
                    ),
                    searchServiceSelected = preferences[SEARCH_SELECTED] ?: 0,
                    mcpServers = run {
                        val json = preferences[MCP_SERVERS]
                        if (json == lastMcpServersJson && lastMcpServersJson != null) {
                            cachedMcpServers
                        } else {
                            val decoded = decodePreferenceList<McpServerConfig>(json) ?: emptyList()
                            lastMcpServersJson = json
                            cachedMcpServers = decoded
                            decoded
                        }
                    },
                    webDavConfig = decodePreferenceValue(preferences[WEBDAV_CONFIG], WebDavConfig()).sanitize(),
                    ttsProviders = run {
                        val json = preferences[TTS_PROVIDERS]
                        if (json == lastTtsProvidersJson && lastTtsProvidersJson != null) {
                            cachedTtsProviders
                        } else {
                            val decoded = decodePreferenceList<TTSProviderSetting>(json) ?: DEFAULT_TTS_PROVIDERS
                            lastTtsProvidersJson = json
                            cachedTtsProviders = decoded
                            decoded
                        }
                    },
                    selectedTTSProviderId = decodeUuid(
                        preferences[SELECTED_TTS_PROVIDER],
                        DEFAULT_SYSTEM_TTS_ID,
                    ),
                    selectedTTSVoiceId = decodeUuid(
                        preferences[SELECTED_TTS_VOICE],
                        DEFAULT_SYSTEM_TTS_VOICE_ID,
                    ),
                    ttsAutoplayMode = decodePreferenceValue(
                        preferences[TTS_AUTOPLAY_MODE],
                        TtsAutoplayMode.OFF,
                    ),
                    sttModelId = decodeUuidOrNull(preferences[STT_MODEL]),
                    sttThinkingBudget = preferences[STT_THINKING_BUDGET] ?: 0,
                    sttPrompt = preferences[STT_PROMPT] ?: me.lucid.domchat.data.ai.prompts.DEFAULT_STT_PROMPT,
                    webServerEnabled = preferences[WEB_SERVER_ENABLED] == true,
                    webServerPort = preferences[WEB_SERVER_PORT] ?: 8080,
                    webServerJwtEnabled = preferences[WEB_SERVER_JWT_ENABLED] == true,
                    webServerAccessPassword = preferences[WEB_SERVER_ACCESS_PASSWORD] ?: "",
                    webServerBackgroundSetupShown = preferences[WEB_SERVER_BACKGROUND_SETUP_SHOWN] == true,
                    modes = run {
                        val json = preferences[MODES]
                        if (json == lastModesJson && lastModesJson != null) {
                            cachedModes
                        } else {
                            val decoded = decodePreferenceList<Mode>(json) ?: emptyList()
                            lastModesJson = json
                            cachedModes = decoded
                            decoded
                        }
                    },
                    lorebooks = run {
                        val json = preferences[LOREBOOKS]
                        if (json == lastLorebooksJson && lastLorebooksJson != null) {
                            cachedLorebooks
                        } else {
                            val decoded = decodePreferenceList<Lorebook>(json) ?: emptyList()
                            lastLorebooksJson = json
                            cachedLorebooks = decoded
                            decoded
                        }
                    },
                    skills = run {
                        val json = preferences[SKILLS]
                        if (json == lastSkillsJson && lastSkillsJson != null) {
                            cachedSkills
                        } else {
                            val decoded = decodePreferenceList<Skill>(json) ?: emptyList()
                            lastSkillsJson = json
                            cachedSkills = decoded
                            decoded
                        }
                    },
                    chatStorage = decodePreferenceValue(
                        preferences[CHAT_STORAGE],
                        ChatStorageSettings(),
                    ),
                    textSelectionConfig = decodePreferenceValue(
                        preferences[TEXT_SELECTION_CONFIG],
                        TextSelectionConfig(),
                    ),
                    assistantOverlayConfig = decodePreferenceValue(
                        preferences[ASSISTANT_OVERLAY_CONFIG],
                        AssistantOverlayConfig(),
                    ),
                ).normalizeThemeId()
            }.getOrElse {
                Log.e(TAG, "Failed to parse settings", it)
                // init=true so SettingsStore.update will refuse to persist dummy defaults
                Settings.dummy()
            }
        }
        .map {
            var providers = it.providers
                .ifEmpty { DEFAULT_PROVIDERS }
                .toMutableList()
            providers = providers.map { provider ->
                val defaultProvider = DEFAULT_PROVIDERS.find { it.id == provider.id }
                if (defaultProvider != null) {
                    provider.copyProvider(
                        builtIn = defaultProvider.builtIn,
                    )
                } else provider
            }.toMutableList()
            val assistants = it.assistants.ifEmpty { DEFAULT_ASSISTANTS }.toMutableList()
            val ttsProviders = it.ttsProviders.map { provider ->
                val defaultVoiceId = if (provider.id == DEFAULT_SYSTEM_TTS_ID) DEFAULT_SYSTEM_TTS_VOICE_ID else null
                provider.withDefaultVoices(defaultVoiceId)
            }.toMutableList()
            val selectedTtsVoiceId = ttsProviders
                .flatMap { provider -> provider.voices }
                .firstOrNull { voice -> voice.id == it.selectedTTSVoiceId }
                ?.id
                ?: ttsProviders.find { provider -> provider.id == it.selectedTTSProviderId }
                    ?.voices
                    ?.firstOrNull()
                    ?.id
                ?: DEFAULT_SYSTEM_TTS_VOICE_ID
            it.copy(
                providers = providers,
                assistants = assistants,
                ttsProviders = ttsProviders,
                selectedTTSVoiceId = selectedTtsVoiceId,
            ).normalizeWebServerSettings().normalizeFontSettings().normalizeTtsSettings()
                .normalizeLocalProvider().normalizeSearchServices().normalizeMemorySettings()
        }
        .map { settings ->
            // 去重并清理无效引用
            val validMcpServerIds = settings.mcpServers.map { it.id }.toSet()
            settings.copy(
                providers = settings.providers
                    .distinctBy { it.id }
                    .map { provider ->
                    when (provider) {
                        is ProviderSetting.OpenAI -> provider.copy(
                            models = provider.models.distinctBy { model -> model.id }
                        )

                        is ProviderSetting.Google -> provider.copy(
                            models = provider.models.distinctBy { model -> model.id }
                        )

                        is ProviderSetting.Claude -> provider.copy(
                            models = provider.models.distinctBy { model -> model.id }
                        )

                        is ProviderSetting.ComfyUI -> provider.copy(
                            models = provider.models.distinctBy { model -> model.id }
                                .map { model -> model.withComfyDefaults() }
                        )

                        is ProviderSetting.LiteRtLocal -> provider.copy(
                            models = provider.models.distinctBy { model -> model.id }
                        )
                    }
                },
                assistants = settings.assistants.distinctBy { it.id }.map { assistant ->
                    assistant.copy(
                        // 过滤掉不存在的 MCP 服务器 ID
                        mcpServers = assistant.mcpServers.filter { serverId ->
                            serverId in validMcpServerIds
                        }.toSet()
                    )
                },
                ttsProviders = settings.ttsProviders.distinctBy { it.id }.map { provider ->
                    val defaultVoiceId = if (provider.id == DEFAULT_SYSTEM_TTS_ID) DEFAULT_SYSTEM_TTS_VOICE_ID else null
                    provider.copyProvider(
                        voices = provider.withDefaultVoices(defaultVoiceId).voices.distinctBy { voice -> voice.id }
                    )
                },

                selectedTTSVoiceId = settings.ttsProviders
                    .flatMap { it.voices }
                    .firstOrNull { it.id == settings.selectedTTSVoiceId }
                    ?.id
                    ?: settings.ttsProviders.find { it.id == settings.selectedTTSProviderId }
                        ?.voices
                        ?.firstOrNull()
                        ?.id
                    ?: DEFAULT_SYSTEM_TTS_VOICE_ID,
                favoriteModels = settings.favoriteModels.filter { uuid ->
                    settings.providers.flatMap { it.models }.any { it.id == uuid }
                }
            )
        }
        .map { settings ->
            val migrated = settings.migrateAssistantSummarizerToGlobal()
            if (!hasPersistedLegacySummarizerMigration) {
                hasPersistedLegacySummarizerMigration = true
                if (migrated != settings) {
                    scope.launch(Dispatchers.IO) {
                        persistLegacySummarizerMigration(migrated)
                    }
                }
            }
            migrated.normalizeFontSettings()
        }
        .map { settings ->
            val migrated = settings.migrateLegacyModesToSkills()
            if (!hasPersistedLegacyModeMigration) {
                hasPersistedLegacyModeMigration = true
                if (migrated != settings) {
                    scope.launch(Dispatchers.IO) {
                        persistLegacyModeMigration(migrated)
                    }
                }
            }
            migrated.normalizeFontSettings()
        }
        .onEach { settings ->
            val signature = java.util.Objects.hash(
                settings.assistants,
                settings.titlePrompt,
                settings.translatePrompt,
                settings.suggestionPrompt,
                settings.learningModePrompt
            )
            if (lastTemplateSignature != signature) {
                lastTemplateSignature = signature
                get<MessageTemplateCache>().invalidateAll()
            }
        }
        .flowOn(Dispatchers.Default)

    // Track if we've done the one-time migration
    private var hasMigratedSecrets = false

    val settingsFlow = settingsFlowRaw
        .distinctUntilChanged()
        .map { settings ->
            // Perform one-time migration on first settings load
            if (!hasMigratedSecrets && !settings.init) {
                hasMigratedSecrets = true
                val migratedSettings = secretKeyManager.migrateSecretsFromSettings(settings)
                if (migratedSettings != settings) {
                    // Secrets were migrated - persist the cleared plaintext back to DataStore
                    // We do this synchronously to ensure consistency
                    scope.launch(Dispatchers.IO) {
                        persistMigratedSettings(migratedSettings)
                    }
                }
                migratedSettings.normalizeFontSettings()
            } else {
                settings.normalizeFontSettings()
            }
        }
        // Hydrate secrets (populate API keys from SecureStore) so they are available in memory/UI
        .map { settings ->
            secretKeyManager.populateSecretsForExport(settings).normalizeFontSettings()
        }
        .onEach { settings -> quickCache.updateCache(settings) }
        .toMutableStateFlow(scope, quickCache.createCachedSettings())

    /**
     * Persist settings after migration (to clear plaintext secrets from DataStore).
     */
    private suspend fun persistMigratedSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[PROVIDERS] = JsonInstant.encodeToString(settings.providers)
            preferences[WEBDAV_CONFIG] = JsonInstant.encodeToString(settings.webDavConfig)
            preferences[TTS_PROVIDERS] = JsonInstant.encodeToString(settings.ttsProviders)
            preferences[SEARCH_SERVICES] = JsonInstant.encodeToString(settings.searchServices)
            preferences[MCP_SERVERS] = JsonInstant.encodeToString(settings.mcpServers)
        }
    }

    /**
     * Persist one-time legacy mode -> skill migration.
     */
    private suspend fun persistLegacyModeMigration(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[ASSISTANTS] = JsonInstant.encodeToString(settings.assistants)
            preferences[MODES] = JsonInstant.encodeToString(settings.modes)
            preferences[SKILLS] = JsonInstant.encodeToString(settings.skills)
        }
    }

    /**
     * Persist one-time assistant summarizer -> global summarizer migration.
     */
    private suspend fun persistLegacySummarizerMigration(settings: Settings) {
        dataStore.edit { preferences ->
            settings.summarizerModelId?.let {
                preferences[SUMMARIZER_MODEL] = it.toString()
            } ?: preferences.remove(SUMMARIZER_MODEL)
            preferences[ASSISTANTS] = JsonInstant.encodeToString(settings.assistants)
        }
    }


    suspend fun update(settings: Settings) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            updateInternal(settings)
        }
    }

    private suspend fun awaitInitializedSettings(caller: String): Settings? {
        val current = if (settingsFlow.value.init) {
            withTimeoutOrNull(5000L) {
                settingsFlow.first { !it.init }
            } ?: run {
                Log.e(TAG, "$caller: settingsFlow failed to initialize within timeout, aborting")
                return null
            }
        } else {
            settingsFlow.value
        }
        if (current.init) {
            Log.e(TAG, "$caller: settings are uninitialized, aborting write")
            return null
        }
        return current
    }

    private suspend fun updateInternal(settings: Settings) {
        val current = awaitInitializedSettings("updateInternal") ?: return
        val effectiveSettings = if (settings.init) {
            current.copy(
                displaySetting = settings.displaySetting,
                assistantId = if (settings.assistantId != DEFAULT_ASSISTANT_ID) settings.assistantId else current.assistantId,
                providers = if (settings.providers.isNotEmpty()) settings.providers else current.providers,
                assistants = if (settings.assistants.isNotEmpty()) settings.assistants else current.assistants,
                init = false
            )
        } else {
            settings
        }
        
        // Auto-update recently used assistants when assistant changes
        val settingsToSave = if (effectiveSettings.assistantId != settingsFlow.value.assistantId && 
            !settingsFlow.value.init &&
            effectiveSettings.assistants.any { it.id == effectiveSettings.assistantId && !it.isDeleted }) {
            val updatedList = buildList {
                add(effectiveSettings.assistantId)
                effectiveSettings.recentlyUsedAssistants
                    .filter { it != effectiveSettings.assistantId }
                    .take(2)
                    .forEach { add(it) }
            }
            effectiveSettings.copy(recentlyUsedAssistants = updatedList)
        } else {
            effectiveSettings
        }
        
        val normalizedSettings = settingsToSave
            .normalizeWebServerSettings()
            .migrateLegacyModesToSkills()
            .normalizeFontSettings()
            .normalizeThemeId()
            .normalizeTtsSettings()
            .normalizeLocalProvider()
            .normalizeSearchServices()
            .normalizeMemorySettings()
            .clearMissingModelReferences()

        // Handle explicit secret deletions (user cleared a field that had a value)
        // This must be called BEFORE migration to remove deleted secrets from SecureStore
        secretKeyManager.handleExplicitSecretDeletions(current, normalizedSettings)
        
        // Migrate secrets from plaintext to SecureStore if needed
        val migratedSettings = secretKeyManager.migrateSecretsFromSettings(normalizedSettings)
            .normalizeFontSettings()

        val nextExportSettings = secretKeyManager.populateSecretsForExport(migratedSettings)
            .normalizeFontSettings()
            .normalizeTtsSettings()
        dataStore.edit { preferences ->
            val isFirstSave = preferences.asMap().isEmpty()
            if (isFirstSave || current.dynamicColor != normalizedSettings.dynamicColor) {
                preferences[DYNAMIC_COLOR] = normalizedSettings.dynamicColor
            }
            if (isFirstSave || current.setupCompleted != normalizedSettings.setupCompleted) {
                preferences[SETUP_COMPLETED] = normalizedSettings.setupCompleted
            }
            if (isFirstSave || current.themeId != normalizedSettings.themeId) {
                preferences[THEME_ID] = normalizedSettings.themeId
            }
            if (isFirstSave || current.developerMode != normalizedSettings.developerMode) {
                preferences[DEVELOPER_MODE] = normalizedSettings.developerMode
            }
            if (isFirstSave || current.enableRagLogging != normalizedSettings.enableRagLogging) {
                preferences[ENABLE_RAG_LOGGING] = normalizedSettings.enableRagLogging
            }
            if (isFirstSave || current.displaySetting != normalizedSettings.displaySetting) {
                preferences[DISPLAY_SETTING] = JsonInstant.encodeToString(normalizedSettings.displaySetting)
            }
            if (isFirstSave || current.enableWebSearch != normalizedSettings.enableWebSearch) {
                preferences[ENABLE_WEB_SEARCH] = normalizedSettings.enableWebSearch
            }
            if (isFirstSave || current.favoriteModels != normalizedSettings.favoriteModels) {
                preferences[FAVORITE_MODELS] = JsonInstant.encodeToString(normalizedSettings.favoriteModels)
            }
            if (isFirstSave || current.recentModelIds != normalizedSettings.recentModelIds) {
                preferences[RECENT_MODEL_IDS] = JsonInstant.encodeToString(normalizedSettings.recentModelIds)
            }
            if (isFirstSave || current.chatModelId != normalizedSettings.chatModelId) {
                preferences[SELECT_MODEL] = normalizedSettings.chatModelId.toString()
            }
            if (isFirstSave || current.titleModelId != normalizedSettings.titleModelId) {
                preferences[TITLE_MODEL] = normalizedSettings.titleModelId.toString()
            }
            if (isFirstSave || current.titleThinkingBudget != normalizedSettings.titleThinkingBudget) {
                preferences[TITLE_THINKING_BUDGET] = normalizedSettings.titleThinkingBudget
            }
            if (isFirstSave || current.summarizerModelId != normalizedSettings.summarizerModelId) {
                normalizedSettings.summarizerModelId?.let {
                    preferences[SUMMARIZER_MODEL] = it.toString()
                } ?: preferences.remove(SUMMARIZER_MODEL)
            }
            if (isFirstSave || current.summarizerThinkingBudget != normalizedSettings.summarizerThinkingBudget) {
                preferences[SUMMARIZER_THINKING_BUDGET] = normalizedSettings.summarizerThinkingBudget
            }
            if (isFirstSave || current.subagentModelId != normalizedSettings.subagentModelId) {
                normalizedSettings.subagentModelId?.let {
                    preferences[SUBAGENT_MODEL] = it.toString()
                } ?: preferences.remove(SUBAGENT_MODEL)
            }
            if (isFirstSave || current.subagentThinkingBudget != normalizedSettings.subagentThinkingBudget) {
                preferences[SUBAGENT_THINKING_BUDGET] = normalizedSettings.subagentThinkingBudget
            }
            if (isFirstSave || current.translateModeId != normalizedSettings.translateModeId) {
                preferences[TRANSLATE_MODEL] = normalizedSettings.translateModeId.toString()
            }
            if (isFirstSave || current.suggestionModelId != normalizedSettings.suggestionModelId) {
                preferences[SUGGESTION_MODEL] = normalizedSettings.suggestionModelId.toString()
            }
            if (isFirstSave || current.suggestionThinkingBudget != normalizedSettings.suggestionThinkingBudget) {
                preferences[SUGGESTION_THINKING_BUDGET] = normalizedSettings.suggestionThinkingBudget
            }
            if (isFirstSave || current.imageGenerationModelId != normalizedSettings.imageGenerationModelId) {
                preferences[IMAGE_GENERATION_MODEL] = normalizedSettings.imageGenerationModelId.toString()
            }
            if (isFirstSave || current.titlePrompt != normalizedSettings.titlePrompt) {
                preferences[TITLE_PROMPT] = normalizedSettings.titlePrompt
            }
            if (isFirstSave || current.translatePrompt != normalizedSettings.translatePrompt) {
                preferences[TRANSLATION_PROMPT] = normalizedSettings.translatePrompt
            }
            if (isFirstSave || current.suggestionPrompt != normalizedSettings.suggestionPrompt) {
                preferences[SUGGESTION_PROMPT] = normalizedSettings.suggestionPrompt
            }
            if (isFirstSave || current.learningModePrompt != normalizedSettings.learningModePrompt) {
                preferences[LEARNING_MODE_PROMPT] = normalizedSettings.learningModePrompt
            }
            if (isFirstSave || current.ocrModelId != normalizedSettings.ocrModelId) {
                preferences[OCR_MODEL] = normalizedSettings.ocrModelId.toString()
            }
            if (isFirstSave || current.ocrThinkingBudget != normalizedSettings.ocrThinkingBudget) {
                preferences[OCR_THINKING_BUDGET] = normalizedSettings.ocrThinkingBudget
            }
            if (isFirstSave || current.ocrPrompt != normalizedSettings.ocrPrompt) {
                preferences[OCR_PROMPT] = normalizedSettings.ocrPrompt
            }
            if (isFirstSave || current.embeddingModelId != normalizedSettings.embeddingModelId) {
                preferences[EMBEDDING_MODEL] = normalizedSettings.embeddingModelId.toString()
            }
            if (isFirstSave || current.providers != normalizedSettings.providers) {
                preferences[PROVIDERS] = JsonInstant.encodeToString(migratedSettings.providers)
            }
            if (isFirstSave || current.assistants != normalizedSettings.assistants) {
                preferences[ASSISTANTS] = JsonInstant.encodeToString(normalizedSettings.assistants)
            }
            if (isFirstSave || current.assistantId != normalizedSettings.assistantId) {
                preferences[SELECT_ASSISTANT] = normalizedSettings.assistantId.toString()
            }
            if (isFirstSave || current.assistantTags != normalizedSettings.assistantTags) {
                preferences[ASSISTANT_TAGS] = JsonInstant.encodeToString(normalizedSettings.assistantTags)
            }
            if (isFirstSave || current.providerTags != normalizedSettings.providerTags) {
                preferences[PROVIDER_TAGS] = JsonInstant.encodeToString(normalizedSettings.providerTags)
            }
            if (isFirstSave || current.recentlyUsedAssistants != normalizedSettings.recentlyUsedAssistants) {
                preferences[RECENTLY_USED_ASSISTANTS] = JsonInstant.encodeToString(normalizedSettings.recentlyUsedAssistants)
            }
            if (isFirstSave || current.searchServices != normalizedSettings.searchServices) {
                preferences[SEARCH_SERVICES] = JsonInstant.encodeToString(migratedSettings.searchServices)
            }
            if (isFirstSave || current.searchCommonOptions != normalizedSettings.searchCommonOptions) {
                preferences[SEARCH_COMMON] = JsonInstant.encodeToString(normalizedSettings.searchCommonOptions)
            }
            if (isFirstSave || current.searchServiceSelected != normalizedSettings.searchServiceSelected || current.searchServices != normalizedSettings.searchServices) {
                preferences[SEARCH_SELECTED] = if (normalizedSettings.searchServices.isEmpty()) {
                    0
                } else {
                    normalizedSettings.searchServiceSelected.coerceIn(0, normalizedSettings.searchServices.size - 1)
                }
            }
            if (isFirstSave || current.mcpServers != normalizedSettings.mcpServers) {
                preferences[MCP_SERVERS] = JsonInstant.encodeToString(migratedSettings.mcpServers)
            }
            if (isFirstSave || current.webDavConfig != normalizedSettings.webDavConfig) {
                preferences[WEBDAV_CONFIG] = JsonInstant.encodeToString(migratedSettings.webDavConfig)
            }
            if (isFirstSave || current.ttsProviders != normalizedSettings.ttsProviders) {
                preferences[TTS_PROVIDERS] = JsonInstant.encodeToString(migratedSettings.ttsProviders)
            }
            if (isFirstSave || current.selectedTTSProviderId != normalizedSettings.selectedTTSProviderId) {
                normalizedSettings.selectedTTSProviderId?.let {
                    preferences[SELECTED_TTS_PROVIDER] = it.toString()
                } ?: preferences.remove(SELECTED_TTS_PROVIDER)
            }
            if (isFirstSave || current.selectedTTSVoiceId != normalizedSettings.selectedTTSVoiceId) {
                preferences[SELECTED_TTS_VOICE] = normalizedSettings.selectedTTSVoiceId.toString()
            }
            if (isFirstSave || current.ttsAutoplayMode != normalizedSettings.ttsAutoplayMode) {
                preferences[TTS_AUTOPLAY_MODE] = JsonInstant.encodeToString(normalizedSettings.ttsAutoplayMode)
            }
            if (isFirstSave || current.sttModelId != normalizedSettings.sttModelId) {
                normalizedSettings.sttModelId?.let {
                    preferences[STT_MODEL] = it.toString()
                } ?: preferences.remove(STT_MODEL)
            }
            if (isFirstSave || current.sttThinkingBudget != normalizedSettings.sttThinkingBudget) {
                preferences[STT_THINKING_BUDGET] = normalizedSettings.sttThinkingBudget
            }
            if (isFirstSave || current.sttPrompt != normalizedSettings.sttPrompt) {
                preferences[STT_PROMPT] = normalizedSettings.sttPrompt
            }
            if (isFirstSave || current.webServerEnabled != normalizedSettings.webServerEnabled) {
                preferences[WEB_SERVER_ENABLED] = normalizedSettings.webServerEnabled
            }
            if (isFirstSave || current.webServerPort != normalizedSettings.webServerPort) {
                preferences[WEB_SERVER_PORT] = normalizedSettings.webServerPort
            }
            if (isFirstSave || current.webServerJwtEnabled != normalizedSettings.webServerJwtEnabled) {
                preferences[WEB_SERVER_JWT_ENABLED] = normalizedSettings.webServerJwtEnabled
            }
            if (isFirstSave || current.webServerAccessPassword != normalizedSettings.webServerAccessPassword) {
                preferences[WEB_SERVER_ACCESS_PASSWORD] = migratedSettings.webServerAccessPassword
            }
            if (isFirstSave || current.webServerBackgroundSetupShown != normalizedSettings.webServerBackgroundSetupShown) {
                preferences[WEB_SERVER_BACKGROUND_SETUP_SHOWN] = normalizedSettings.webServerBackgroundSetupShown
            }
            if (isFirstSave || current.modes != normalizedSettings.modes) {
                preferences[MODES] = JsonInstant.encodeToString(normalizedSettings.modes)
            }
            if (isFirstSave || current.lorebooks != normalizedSettings.lorebooks) {
                preferences[LOREBOOKS] = JsonInstant.encodeToString(normalizedSettings.lorebooks)
            }
            if (isFirstSave || current.skills != normalizedSettings.skills) {
                preferences[SKILLS] = JsonInstant.encodeToString(normalizedSettings.skills)
            }
            if (isFirstSave || current.chatStorage != normalizedSettings.chatStorage) {
                preferences[CHAT_STORAGE] = JsonInstant.encodeToString(normalizedSettings.chatStorage)
            }
            if (isFirstSave || current.textSelectionConfig != normalizedSettings.textSelectionConfig) {
                preferences[TEXT_SELECTION_CONFIG] = JsonInstant.encodeToString(normalizedSettings.textSelectionConfig)
            }
            if (isFirstSave || current.assistantOverlayConfig != normalizedSettings.assistantOverlayConfig) {
                preferences[ASSISTANT_OVERLAY_CONFIG] = JsonInstant.encodeToString(normalizedSettings.assistantOverlayConfig)
            }
        }
        settingsFlow.value = nextExportSettings
    }

    suspend fun update(fn: (Settings) -> Settings) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            val current = awaitInitializedSettings("update(fn)") ?: return@withContext
            updateInternal(fn(current))
        }
    }

    suspend fun updateDisplaySetting(displaySetting: DisplaySetting) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            val current = awaitInitializedSettings("updateDisplaySetting") ?: return@withContext
            if (current.displaySetting != displaySetting) {
                dataStore.edit { preferences ->
                    preferences[DISPLAY_SETTING] = JsonInstant.encodeToString(displaySetting)
                }
                settingsFlow.value = current.copy(displaySetting = displaySetting)
            }
        }
    }

    suspend fun updateRecentlyUsedAssistants(assistants: List<Uuid>) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            val current = awaitInitializedSettings("updateRecentlyUsedAssistants") ?: return@withContext
            if (current.recentlyUsedAssistants != assistants) {
                dataStore.edit { preferences ->
                    preferences[RECENTLY_USED_ASSISTANTS] = JsonInstant.encodeToString(assistants)
                }
                settingsFlow.value = current.copy(recentlyUsedAssistants = assistants)
            }
        }
    }

    suspend fun updateAssistant(assistantId: Uuid) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            val current = awaitInitializedSettings("updateAssistant") ?: return@withContext
            val exists = current.assistants.any { it.id == assistantId && !it.isDeleted }
            if (!exists && assistantId != DEFAULT_ASSISTANT_ID) return@withContext

            val updatedRecentlyUsed: List<Uuid>? = if (exists) {
                buildList<Uuid> {
                    add(assistantId)
                    current.recentlyUsedAssistants
                        .filter { it != assistantId }
                        .take(2)
                        .forEach { add(it) }
                }
            } else null

            // Persist to DataStore first
            dataStore.edit { preferences ->
                preferences[SELECT_ASSISTANT] = assistantId.toString()
                if (updatedRecentlyUsed != null) {
                    preferences[RECENTLY_USED_ASSISTANTS] = JsonInstant.encodeToString(updatedRecentlyUsed)
                }
            }

            // Always update in-memory state after successful persistence (including DEFAULT_ASSISTANT_ID)
            settingsFlow.value = current.copy(
                assistantId = assistantId,
                recentlyUsedAssistants = updatedRecentlyUsed ?: current.recentlyUsedAssistants
            )
        }
    }

    /**
     * Mark an assistant as recently used for app shortcuts.
     * Moves it to the front of the list and keeps only the 3 most recent.
     */
    suspend fun markAssistantUsed(assistantId: Uuid) = updateMutex.withLock {
        withContext(Dispatchers.IO) {
            val current = awaitInitializedSettings("markAssistantUsed") ?: return@withContext
            // Only add if the assistant exists
            if (current.assistants.none { it.id == assistantId && !it.isDeleted }) return@withContext

            val updatedList = buildList {
                add(assistantId)
                current.recentlyUsedAssistants
                    .filter { it != assistantId }
                    .take(2)
                    .forEach { add(it) }
            }

            if (updatedList != current.recentlyUsedAssistants) {
                dataStore.edit { preferences ->
                    preferences[RECENTLY_USED_ASSISTANTS] = JsonInstant.encodeToString(updatedList)
                }
                settingsFlow.value = current.copy(recentlyUsedAssistants = updatedList)
            }
        }
    }
}

