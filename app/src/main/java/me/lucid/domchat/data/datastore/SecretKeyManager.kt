package me.lucid.domchat.data.datastore

import me.lucid.asr.ASRProviderSetting
import me.lucid.ai.provider.ApiKeyEntry
import me.lucid.ai.provider.ProviderSetting
import me.lucid.ai.provider.withApiKeyPool
import me.lucid.ai.util.PooledKey
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.search.SearchServiceOptions
import me.lucid.domchat.data.ai.mcp.McpServerConfig
import me.lucid.domchat.data.ai.mcp.McpCommonOptions
import kotlin.uuid.Uuid

/**
 * Manages secrets for providers and other sensitive data.
 * Handles migration from plaintext DataStore to encrypted SecureStore.
 * 
 * Key naming conventions:
 * - Provider API key: "provider_apikey_{providerId}"
 * - Provider API key pool: "provider_apikey_pool_{providerId}_{entryId}"
 * - Provider private key (Vertex AI): "provider_privatekey_{providerId}"
 */
class SecretKeyManager(
    private val secureStore: SecureStore
) {
    companion object {
        private const val PROVIDER_APIKEY_PREFIX = "provider_apikey_"
        private const val PROVIDER_APIKEY_POOL_PREFIX = "provider_apikey_pool_"
        private const val PROVIDER_PRIVATEKEY_PREFIX = "provider_privatekey_"
        private const val TTS_PROVIDER_APIKEY_PREFIX = "tts_provider_apikey_"
        private const val STT_PROVIDER_APIKEY_PREFIX = "stt_provider_apikey_"
        private const val WEB_SERVER_ACCESS_PASSWORD_KEY = "web_server_access_password"
        private const val HUGGINGFACE_TOKEN_KEY = "huggingface_token"
        private const val MCP_OAUTH_PREFIX = "mcp_oauth_"
        private const val SEARCH_SERVICE_KEY_PREFIX = "search_service_apikey_"
        private const val SEARCH_SERVICE_USER_PREFIX = "search_service_user_"
        private const val SEARCH_SERVICE_PASS_PREFIX = "search_service_pass_"
        private const val MCP_SERVER_HEADER_PREFIX = "mcp_header_"
    }

    // ========== Provider API Key Management ==========

    /**
     * Get the API key for a provider. First checks SecureStore, then falls back
     * to the plaintext value (for migration).
     */
    fun getApiKey(providerId: Uuid, plaintextFallback: String): String {
        val key = "$PROVIDER_APIKEY_PREFIX$providerId"
        return secureStore.getSecret(key) ?: plaintextFallback
    }

    /**
     * Store an API key securely for a provider.
     */
    fun setApiKey(providerId: Uuid, apiKey: String) {
        val key = "$PROVIDER_APIKEY_PREFIX$providerId"
        if (apiKey.isNotBlank()) {
            secureStore.putSecret(key, apiKey)
        } else {
            secureStore.removeSecret(key)
        }
    }

    /**
     * Get the private key for a Google Vertex AI provider.
     */
    fun getPrivateKey(providerId: Uuid, plaintextFallback: String): String {
        val key = "$PROVIDER_PRIVATEKEY_PREFIX$providerId"
        return secureStore.getSecret(key) ?: plaintextFallback
    }

    /**
     * Store a private key securely for a Google Vertex AI provider.
     */
    fun setPrivateKey(providerId: Uuid, privateKey: String) {
        val key = "$PROVIDER_PRIVATEKEY_PREFIX$providerId"
        if (privateKey.isNotBlank()) {
            secureStore.putSecret(key, privateKey)
        } else {
            secureStore.removeSecret(key)
        }
    }

    /**
     * Get an API key from the pool for a provider.
     */
    fun getPoolApiKey(providerId: Uuid, entryId: Uuid, plaintextFallback: String = ""): String {
        val key = "$PROVIDER_APIKEY_POOL_PREFIX${providerId}_$entryId"
        return secureStore.getSecret(key) ?: plaintextFallback
    }

    /**
     * Store an API key in the pool securely for a provider.
     */
    fun setPoolApiKey(providerId: Uuid, entryId: Uuid, apiKey: String) {
        val key = "$PROVIDER_APIKEY_POOL_PREFIX${providerId}_$entryId"
        if (apiKey.isNotBlank()) {
            secureStore.putSecret(key, apiKey)
        } else {
            secureStore.removeSecret(key)
        }
    }

    /**
     * Remove a single pool API key for a provider.
     */
    fun removePoolApiKey(providerId: Uuid, entryId: Uuid) {
        secureStore.removeSecret("$PROVIDER_APIKEY_POOL_PREFIX${providerId}_$entryId")
    }

    /**
     * Remove all pool API keys for a provider.
     */
    fun removeAllPoolApiKeys(providerId: Uuid) {
        val prefix = "$PROVIDER_APIKEY_POOL_PREFIX${providerId}_"
        secureStore.getAllKeys()
            .filter { it.startsWith(prefix) }
            .forEach(secureStore::removeSecret)
    }

    /**
     * Remove all secrets for a provider (when provider is deleted).
     */
    fun removeProviderSecrets(providerId: Uuid) {
        secureStore.removeSecret("$PROVIDER_APIKEY_PREFIX$providerId")
        secureStore.removeSecret("$PROVIDER_PRIVATEKEY_PREFIX$providerId")
        removeAllPoolApiKeys(providerId)
    }

    // ========== TTS API Key Management ==========

    fun getTtsApiKey(providerId: Uuid, plaintextFallback: String): String {
        val key = "$TTS_PROVIDER_APIKEY_PREFIX$providerId"
        return secureStore.getSecret(key) ?: plaintextFallback
    }

    fun setTtsApiKey(providerId: Uuid, apiKey: String) {
        val key = "$TTS_PROVIDER_APIKEY_PREFIX$providerId"
        if (apiKey.isNotBlank()) {
            secureStore.putSecret(key, apiKey)
        } else {
            secureStore.removeSecret(key)
        }
    }

    fun removeTtsProviderSecrets(providerId: Uuid) {
        secureStore.removeSecret("$TTS_PROVIDER_APIKEY_PREFIX$providerId")
    }

    // ========== STT API Key Management ==========

    fun getSttApiKey(providerId: Uuid, plaintextFallback: String): String {
        val key = "$STT_PROVIDER_APIKEY_PREFIX$providerId"
        return secureStore.getSecret(key) ?: plaintextFallback
    }

    fun setSttApiKey(providerId: Uuid, apiKey: String) {
        val key = "$STT_PROVIDER_APIKEY_PREFIX$providerId"
        if (apiKey.isNotBlank()) {
            secureStore.putSecret(key, apiKey)
        } else {
            secureStore.removeSecret(key)
        }
    }

    // ========== Web Server Access Password Management ==========

    fun getWebServerAccessPassword(plaintextFallback: String): String {
        return secureStore.getSecret(WEB_SERVER_ACCESS_PASSWORD_KEY) ?: plaintextFallback
    }

    fun setWebServerAccessPassword(password: String) {
        if (password.isNotBlank()) {
            secureStore.putSecret(WEB_SERVER_ACCESS_PASSWORD_KEY, password)
        } else {
            secureStore.removeSecret(WEB_SERVER_ACCESS_PASSWORD_KEY)
        }
    }

    // ========== HuggingFace Token Management ==========

    fun getHuggingFaceToken(): String? {
        return secureStore.getSecret(HUGGINGFACE_TOKEN_KEY)
    }

    fun setHuggingFaceToken(token: String) {
        if (token.isNotBlank()) {
            secureStore.putSecret(HUGGINGFACE_TOKEN_KEY, token)
        } else {
            secureStore.removeSecret(HUGGINGFACE_TOKEN_KEY)
        }
    }

    // ========== MCP OAuth Management ==========

    fun getMcpOAuthSecret(serverId: Uuid, name: String): String? {
        return secureStore.getSecret("$MCP_OAUTH_PREFIX${serverId}_$name")
    }

    fun setMcpOAuthSecret(serverId: Uuid, name: String, value: String?) {
        val key = "$MCP_OAUTH_PREFIX${serverId}_$name"
        if (value.isNullOrBlank()) {
            secureStore.removeSecret(key)
        } else {
            secureStore.putSecret(key, value)
        }
    }

    fun removeMcpOAuthSecrets(serverId: Uuid) {
        secureStore.getAllKeys()
            .filter { it.startsWith("$MCP_OAUTH_PREFIX${serverId}_") }
            .forEach(secureStore::removeSecret)
    }

    // ========== Migration Logic ==========

    /**
     * Migrate secrets from plaintext Settings to SecureStore.
     * Returns updated Settings with credentials cleared (moved to SecureStore).
     * 
     * This should be called once when settings are loaded to ensure migration.
     */
    fun migrateSecretsFromSettings(settings: Settings): Settings {
        var migrated = false
        
        // Migrate provider API keys
        val migratedProviders = settings.providers.map { provider ->
            migrateProviderSecrets(provider).also { 
                if (it != provider) migrated = true
            }
        }

        // Clear any residual plaintext WebDAV password
        val migratedWebDav = if (settings.webDavConfig.password.isNotBlank()) {
            migrated = true
            settings.webDavConfig.copy(password = "")
        } else {
            settings.webDavConfig
        }

        // Migrate Web Server Access Password
        val migratedWebServerAccessPassword = if (settings.webServerAccessPassword.isNotBlank()) {
            setWebServerAccessPassword(settings.webServerAccessPassword)
            migrated = true
            "" // Clear plaintext
        } else {
            ""
        }

        // Migrate TTS provider API keys
        val migratedTtsProviders = settings.ttsProviders.map { provider ->
            migrateTtsProviderSecrets(provider).also {
                if (it != provider) migrated = true
            }
        }

        // Migrate HuggingFace token
        if (settings.huggingFaceToken.isNotBlank()) {
            setHuggingFaceToken(settings.huggingFaceToken)
            migrated = true
        }

        // Migrate MCP OAuth tokens
        if (settings.mcpOAuthTokens.isNotEmpty()) {
            settings.mcpOAuthTokens.forEach { (k, v) ->
                if (k.startsWith(MCP_OAUTH_PREFIX) && v.isNotBlank()) {
                    secureStore.putSecret(k, v)
                    migrated = true
                }
            }
        }

        // Migrate Search Service secrets
        val migratedSearchServices = settings.searchServices.map { service ->
            migrateSearchServiceSecrets(service).also {
                if (it != service) migrated = true
            }
        }

        // Migrate MCP Server secrets
        val migratedMcpServers = settings.mcpServers.map { server ->
            migrateMcpServerSecrets(server).also {
                if (it != server) migrated = true
            }
        }

        return if (migrated) {
            settings.copy(
                providers = migratedProviders,
                webDavConfig = migratedWebDav,
                ttsProviders = migratedTtsProviders,
                webServerAccessPassword = migratedWebServerAccessPassword,
                searchServices = migratedSearchServices,
                mcpServers = migratedMcpServers,
                huggingFaceToken = "",
                mcpOAuthTokens = emptyMap(),
            )
        } else {
            settings
        }
    }

    /**
     * Handle explicit secret deletions.
     * When a user clears a secret field (changes from non-empty to empty),
     * we need to remove it from SecureStore so it doesn't get re-populated.
     * 
     * This should be called BEFORE migrateSecretsFromSettings() in the update flow.
     */
    fun handleExplicitSecretDeletions(oldSettings: Settings, newSettings: Settings) {
        if (oldSettings.webServerAccessPassword.isNotBlank() && newSettings.webServerAccessPassword.isBlank()) {
            setWebServerAccessPassword("")
        }
        // Provider presets have stable IDs. If a deleted provider's secrets remain in
        // SecureStore, adding the same preset again silently restores those credentials.
        // Treat removal from Settings as deletion of every secret owned by that provider.
        val newProviderIds = newSettings.providers.asSequence().map { it.id }.toHashSet()
        oldSettings.providers
            .asSequence()
            .filterNot { newProviderIds.contains(it.id) }
            .forEach { oldProvider ->
                removeProviderSecrets(oldProvider.id)
                oldProvider.models.forEach { model ->
                    model.providerOverwrite?.id?.let(::removeProviderSecrets)
                }
            }

        // Same for TTS providers: deleting a provider must wipe SecureStore keys
        // or re-adding the same preset silently restores the old API key.
        val newTtsProviderIds = newSettings.ttsProviders.asSequence().map { it.id }.toHashSet()
        oldSettings.ttsProviders
            .asSequence()
            .map { it.id }
            .filterNot(newTtsProviderIds::contains)
            .forEach(::removeTtsProviderSecrets)

        // Same for Search services
        val newSearchIds = newSettings.searchServices.asSequence().map { it.id }.toHashSet()
        oldSettings.searchServices
            .asSequence()
            .map { it.id }
            .filterNot(newSearchIds::contains)
            .forEach(::removeSearchServiceSecrets)

        // Same for MCP servers
        val newMcpIds = newSettings.mcpServers.asSequence().map { it.id }.toHashSet()
        oldSettings.mcpServers
            .asSequence()
            .map { it.id }
            .filterNot(newMcpIds::contains)
            .forEach(::removeMcpServerSecrets)

        // Handle provider secrets (API keys, private keys, pools, and nested model overwrites)
        for (newProvider in newSettings.providers) {
            val oldProvider = oldSettings.providers.find { it.id == newProvider.id } ?: continue
            checkProviderCredentialDeletions(oldProvider, newProvider)

            // Recurse into model.providerOverwrite
            val newModelsMap = newProvider.models.associateBy { it.id }
            for (oldModel in oldProvider.models) {
                val oldOverwrite = oldModel.providerOverwrite
                if (oldOverwrite != null) {
                    val newModel = newModelsMap[oldModel.id]
                    val newOverwrite = newModel?.providerOverwrite
                    if (newOverwrite == null || newOverwrite.id != oldOverwrite.id) {
                        removeProviderSecrets(oldOverwrite.id)
                    } else {
                        checkProviderCredentialDeletions(oldOverwrite, newOverwrite)
                    }
                }
            }
        }

        // Handle Search service secrets clearing
        for (newSearch in newSettings.searchServices) {
            val oldSearch = oldSettings.searchServices.find { it.id == newSearch.id } ?: continue
            if (oldSearch is SearchServiceOptions.SearXNGOptions && newSearch is SearchServiceOptions.SearXNGOptions) {
                if (oldSearch.username.isNotBlank() && newSearch.username.isBlank()) {
                    secureStore.removeSecret("$SEARCH_SERVICE_USER_PREFIX${newSearch.id}")
                }
                if (oldSearch.password.isNotBlank() && newSearch.password.isBlank()) {
                    secureStore.removeSecret("$SEARCH_SERVICE_PASS_PREFIX${newSearch.id}")
                }
            } else {
                val oldKey = getSearchServiceApiKey(oldSearch)
                val newKey = getSearchServiceApiKey(newSearch)
                if (oldKey?.isNotBlank() == true && newKey.isNullOrBlank()) {
                    secureStore.removeSecret("$SEARCH_SERVICE_KEY_PREFIX${newSearch.id}")
                }
            }
        }

        // Handle MCP Server auth headers clearing/removal
        for (newMcp in newSettings.mcpServers) {
            val oldMcp = oldSettings.mcpServers.find { it.id == newMcp.id } ?: continue
            val newHeadersMap = newMcp.commonOptions.headers.toMap()
            for ((oldK, oldV) in oldMcp.commonOptions.headers) {
                if (isAuthHeader(oldK)) {
                    val newV = newHeadersMap[oldK]
                    if (newV == null || (oldV.isNotBlank() && newV.isBlank())) {
                        secureStore.removeSecret("$MCP_SERVER_HEADER_PREFIX${newMcp.id}_$oldK")
                    }
                }
            }
        }

        // Handle TTS provider secrets
        for (newTtsProvider in newSettings.ttsProviders) {
            val oldTtsProvider = oldSettings.ttsProviders.find { it.id == newTtsProvider.id } ?: continue
            
            val oldKey = when (oldTtsProvider) {
                is TTSProviderSetting.OpenAI -> oldTtsProvider.apiKey
                is TTSProviderSetting.Gemini -> oldTtsProvider.apiKey
                is TTSProviderSetting.MiniMax -> oldTtsProvider.apiKey
                is TTSProviderSetting.ElevenLabs -> oldTtsProvider.apiKey
                is TTSProviderSetting.Qwen -> oldTtsProvider.apiKey
                is TTSProviderSetting.Cartesia -> oldTtsProvider.apiKey
                is TTSProviderSetting.FishAudio -> oldTtsProvider.apiKey
                is TTSProviderSetting.PlayHT -> oldTtsProvider.apiKey
                is TTSProviderSetting.SystemTTS -> ""
            }
            val newKey = when (newTtsProvider) {
                is TTSProviderSetting.OpenAI -> newTtsProvider.apiKey
                is TTSProviderSetting.Gemini -> newTtsProvider.apiKey
                is TTSProviderSetting.MiniMax -> newTtsProvider.apiKey
                is TTSProviderSetting.ElevenLabs -> newTtsProvider.apiKey
                is TTSProviderSetting.Qwen -> newTtsProvider.apiKey
                is TTSProviderSetting.Cartesia -> newTtsProvider.apiKey
                is TTSProviderSetting.FishAudio -> newTtsProvider.apiKey
                is TTSProviderSetting.PlayHT -> newTtsProvider.apiKey
                is TTSProviderSetting.SystemTTS -> ""
            }
            
            if (oldKey.isNotBlank() && newKey.isBlank()) {
                setTtsApiKey(newTtsProvider.id, "")
            }
        }
    }

    private fun checkProviderCredentialDeletions(oldProvider: ProviderSetting, newProvider: ProviderSetting) {
        val newEntryMap = newProvider.apiKeyPool.associateBy { it.id }
        for (oldEntry in oldProvider.apiKeyPool) {
            val surviving = newEntryMap[oldEntry.id]
            if (surviving == null) {
                removePoolApiKey(newProvider.id, oldEntry.id)
            } else if (oldEntry.key.isNotBlank() && surviving.key.isBlank()) {
                removePoolApiKey(newProvider.id, oldEntry.id)
            }
        }

        when {
            oldProvider is ProviderSetting.OpenAI && newProvider is ProviderSetting.OpenAI -> {
                if (oldProvider.apiKey.isNotBlank() && newProvider.apiKey.isBlank()) {
                    setApiKey(newProvider.id, "")
                }
            }
            oldProvider is ProviderSetting.Google && newProvider is ProviderSetting.Google -> {
                if (oldProvider.apiKey.isNotBlank() && newProvider.apiKey.isBlank()) {
                    setApiKey(newProvider.id, "")
                }
                if (oldProvider.privateKey.isNotBlank() && newProvider.privateKey.isBlank()) {
                    setPrivateKey(newProvider.id, "")
                }
            }
            oldProvider is ProviderSetting.Claude && newProvider is ProviderSetting.Claude -> {
                if (oldProvider.apiKey.isNotBlank() && newProvider.apiKey.isBlank()) {
                    setApiKey(newProvider.id, "")
                }
            }
        }
    }

    private fun getSearchServiceApiKey(service: SearchServiceOptions): String? = when (service) {
        is SearchServiceOptions.ZhipuOptions -> service.apiKey
        is SearchServiceOptions.TavilyOptions -> service.apiKey
        is SearchServiceOptions.ExaOptions -> service.apiKey
        is SearchServiceOptions.LinkUpOptions -> service.apiKey
        is SearchServiceOptions.BraveOptions -> service.apiKey
        is SearchServiceOptions.MetasoOptions -> service.apiKey
        is SearchServiceOptions.OllamaOptions -> service.apiKey
        is SearchServiceOptions.PerplexityOptions -> service.apiKey
        is SearchServiceOptions.FirecrawlOptions -> service.apiKey
        is SearchServiceOptions.JinaOptions -> service.apiKey
        is SearchServiceOptions.BochaOptions -> service.apiKey
        is SearchServiceOptions.NanoGPTOptions -> service.apiKey
        is SearchServiceOptions.GrokOptions -> service.apiKey
        is SearchServiceOptions.SearXNGOptions -> null
        is SearchServiceOptions.KeylessOptions -> null
        is SearchServiceOptions.BingLocalOptions -> null
    }

    /**
     * Migrate a single provider's secrets to SecureStore.
     * Returns provider with credentials cleared if migration occurred.
     */
    private fun migrateProviderSecrets(provider: ProviderSetting): ProviderSetting {
        var updated = provider

        // Store any plaintext entry keys in SecureStore and clear them
        if (updated.apiKeyPool.isNotEmpty()) {
            var poolModified = false
            val cleanedPool = updated.apiKeyPool.map { entry ->
                if (entry.key.isNotBlank()) {
                    setPoolApiKey(updated.id, entry.id, entry.key)
                    poolModified = true
                    entry.copy(key = "")
                } else {
                    entry
                }
            }
            if (poolModified) {
                updated = updated.withApiKeyPool(cleanedPool)
            }
        }

        val migratedModels = updated.models.map { model ->
            val overwrite = model.providerOverwrite
            if (overwrite != null) {
                model.copy(providerOverwrite = migrateProviderSecrets(overwrite))
            } else {
                model
            }
        }

        return when (updated) {
            is ProviderSetting.OpenAI -> {
                val legacyKey = getApiKey(updated.id, updated.apiKey)
                if (updated.apiKeyPool.isEmpty() && legacyKey.isNotBlank()) {
                    val entry = ApiKeyEntry(
                        name = "Primary",
                        enabled = true,
                        exportable = true,
                    )
                    setPoolApiKey(updated.id, entry.id, legacyKey)
                    setApiKey(updated.id, "")
                    updated.copy(apiKey = "", apiKeyPool = listOf(entry), models = migratedModels)
                } else if (updated.apiKey.isNotBlank()) {
                    setApiKey(updated.id, updated.apiKey)
                    updated.copy(apiKey = "", models = migratedModels)
                } else updated.copy(models = migratedModels)
            }

            is ProviderSetting.Google -> {
                var g = updated
                val legacyKey = getApiKey(g.id, g.apiKey)
                if (g.apiKeyPool.isEmpty() && legacyKey.isNotBlank()) {
                    val entry = ApiKeyEntry(
                        name = "Primary",
                        enabled = true,
                        exportable = true,
                    )
                    setPoolApiKey(g.id, entry.id, legacyKey)
                    setApiKey(g.id, "")
                    g = g.copy(apiKey = "", apiKeyPool = listOf(entry))
                } else if (g.apiKey.isNotBlank()) {
                    setApiKey(g.id, g.apiKey)
                    g = g.copy(apiKey = "")
                }
                if (g.privateKey.isNotBlank()) {
                    setPrivateKey(g.id, g.privateKey)
                    g = g.copy(privateKey = "")
                }
                g.copy(models = migratedModels)
            }

            is ProviderSetting.Claude -> {
                val legacyKey = getApiKey(updated.id, updated.apiKey)
                if (updated.apiKeyPool.isEmpty() && legacyKey.isNotBlank()) {
                    val entry = ApiKeyEntry(
                        name = "Primary",
                        enabled = true,
                        exportable = true,
                    )
                    setPoolApiKey(updated.id, entry.id, legacyKey)
                    setApiKey(updated.id, "")
                    updated.copy(apiKey = "", apiKeyPool = listOf(entry), models = migratedModels)
                } else if (updated.apiKey.isNotBlank()) {
                    setApiKey(updated.id, updated.apiKey)
                    updated.copy(apiKey = "", models = migratedModels)
                } else updated.copy(models = migratedModels)
            }

            is ProviderSetting.ComfyUI -> updated.copy(models = migratedModels)
            is ProviderSetting.LiteRtLocal -> updated.copy(models = migratedModels) // on-device, no secrets
        }
    }

    private fun migrateTtsProviderSecrets(provider: TTSProviderSetting): TTSProviderSetting {
        return when (provider) {
            is TTSProviderSetting.OpenAI -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.Gemini -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.MiniMax -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.ElevenLabs -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.Qwen -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.Cartesia -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.FishAudio -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.PlayHT -> {
                if (provider.apiKey.isNotBlank()) {
                    setTtsApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is TTSProviderSetting.SystemTTS -> provider
        }
    }

    private fun migrateSttProviderSecrets(provider: ASRProviderSetting): ASRProviderSetting {
        return when (provider) {
            is ASRProviderSetting.OpenAICompatible -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.OpenAIRealtime -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.DashScope -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.Volcengine -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.MiMo -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.Step -> {
                if (provider.apiKey.isNotBlank()) {
                    setSttApiKey(provider.id, provider.apiKey)
                    provider.copy(apiKey = "")
                } else provider
            }

            is ASRProviderSetting.SystemSTT -> provider
        }
    }

    // ========== Backup/Export Support ==========

    fun getAllMcpOAuthSecrets(): Map<String, String> {
        return secureStore.getAllKeys()
            .filter { it.startsWith(MCP_OAUTH_PREFIX) }
            .associateWith { secureStore.getSecret(it).orEmpty() }
    }

    /**
     * Populate settings with decrypted secrets for export.
     * This creates a copy with all secrets in plaintext for backup portability.
     */
    fun populateSecretsForExport(
        settings: Settings,
        selectedKeyIds: Set<Uuid>? = null,
        includeApiKeys: Boolean = true,
        includeSettings: Boolean = true,
    ): Settings {
        val providersWithSecrets = if (includeApiKeys) {
            settings.providers.map { provider ->
                populateProviderSecrets(provider, selectedKeyIds, forExport = (selectedKeyIds != null))
            }
        } else {
            settings.providers.map { provider ->
                stripProviderSecrets(provider)
            }
        }

        val webDavWithPassword = settings.webDavConfig.copy(password = "")

        val ttsProvidersWithSecrets = if (includeApiKeys) {
            settings.ttsProviders.map { provider ->
                populateTtsProviderSecrets(provider)
            }
        } else {
            settings.ttsProviders.map { stripTtsSecrets(it) }
        }

        val webServerAccessPassword = if (includeSettings && includeApiKeys) {
            getWebServerAccessPassword(settings.webServerAccessPassword)
        } else {
            ""
        }

        val hfToken = if (includeApiKeys) getHuggingFaceToken().orEmpty() else ""
        val mcpTokens = if (includeApiKeys) getAllMcpOAuthSecrets() else emptyMap()

        val searchServicesWithSecrets = if (includeApiKeys) {
            settings.searchServices.map { populateSearchServiceSecrets(it) }
        } else {
            settings.searchServices.map { stripSearchServiceSecrets(it) }
        }

        val mcpServersWithSecrets = if (includeApiKeys) {
            settings.mcpServers.map { populateMcpServerSecrets(it) }
        } else {
            settings.mcpServers.map { stripMcpServerSecrets(it) }
        }

        return settings.copy(
            providers = providersWithSecrets,
            webDavConfig = webDavWithPassword,
            ttsProviders = ttsProvidersWithSecrets,
            webServerAccessPassword = webServerAccessPassword,
            searchServices = searchServicesWithSecrets,
            mcpServers = mcpServersWithSecrets,
            huggingFaceToken = hfToken,
            mcpOAuthTokens = mcpTokens,
        )
    }

    private fun stripProviderSecrets(provider: ProviderSetting): ProviderSetting {
        val strippedPool = provider.apiKeyPool.map { it.copy(key = "") }
        val strippedModels = provider.models.map { model ->
            val overwrite = model.providerOverwrite
            if (overwrite != null) {
                model.copy(providerOverwrite = stripProviderSecrets(overwrite))
            } else {
                model
            }
        }
        val stripped = when (provider) {
            is ProviderSetting.OpenAI -> provider.copy(
                apiKey = "",
                apiKeyPool = strippedPool,
                models = strippedModels
            )
            is ProviderSetting.Google -> provider.copy(
                apiKey = "",
                privateKey = "",
                apiKeyPool = strippedPool,
                models = strippedModels
            )
            is ProviderSetting.Claude -> provider.copy(
                apiKey = "",
                apiKeyPool = strippedPool,
                models = strippedModels
            )
            is ProviderSetting.ComfyUI -> provider.copy(models = strippedModels)
            is ProviderSetting.LiteRtLocal -> provider.copy(models = strippedModels)
        }
        stripped.resolvedApiKeyPool = emptyList()
        return stripped
    }

    private fun stripTtsSecrets(provider: TTSProviderSetting): TTSProviderSetting {
        return when (provider) {
            is TTSProviderSetting.OpenAI -> provider.copy(apiKey = "")
            is TTSProviderSetting.Gemini -> provider.copy(apiKey = "")
            is TTSProviderSetting.MiniMax -> provider.copy(apiKey = "")
            is TTSProviderSetting.ElevenLabs -> provider.copy(apiKey = "")
            is TTSProviderSetting.Qwen -> provider.copy(apiKey = "")
            is TTSProviderSetting.Cartesia -> provider.copy(apiKey = "")
            is TTSProviderSetting.FishAudio -> provider.copy(apiKey = "")
            is TTSProviderSetting.PlayHT -> provider.copy(apiKey = "")
            is TTSProviderSetting.SystemTTS -> provider
        }
    }

    /**
     * Populate a single provider with its secrets.
     * When [forExport] is true, only exportable/selected keys are decrypted into entry.key.
     * When [forExport] is false, all pool keys are decrypted into entry.key.
     * In both cases, [resolvedApiKeyPool] is fully populated with all enabled keys.
     */
    fun populateProviderSecrets(
        provider: ProviderSetting,
        selectedKeyIds: Set<Uuid>? = null,
        forExport: Boolean = false,
    ): ProviderSetting {
        // Resolve pool keys from SecureStore
        val resolvedPool = provider.apiKeyPool
            .filter { it.enabled }
            .mapIndexed { index, entry ->
                val secret = getPoolApiKey(provider.id, entry.id, entry.key)
                PooledKey(
                    id = entry.id,
                    name = entry.name,
                    value = secret,
                    priority = index,
                    providerId = provider.id,
                    providerName = provider.name,
                )
            }

        // Populate entry.key
        val poolWithSecrets = provider.apiKeyPool.map { entry ->
            val secret = getPoolApiKey(provider.id, entry.id, entry.key)
            val shouldExport = if (!forExport) {
                true
            } else if (selectedKeyIds != null) {
                entry.id in selectedKeyIds
            } else {
                entry.exportable
            }
            entry.copy(key = if (shouldExport) secret else "")
        }

        val shouldExportPrimary = !forExport || selectedKeyIds == null || provider.id in selectedKeyIds
        val updated = when (provider) {
            is ProviderSetting.OpenAI -> {
                provider.copy(
                    apiKey = if (shouldExportPrimary) getApiKey(provider.id, provider.apiKey) else "",
                    apiKeyPool = poolWithSecrets,
                    models = provider.models.map { hydrateModelProviderOverwrite(it, selectedKeyIds, forExport) },
                )
            }

            is ProviderSetting.Google -> {
                provider.copy(
                    apiKey = if (shouldExportPrimary) getApiKey(provider.id, provider.apiKey) else "",
                    privateKey = if (shouldExportPrimary) getPrivateKey(provider.id, provider.privateKey) else "",
                    apiKeyPool = poolWithSecrets,
                    models = provider.models.map { hydrateModelProviderOverwrite(it, selectedKeyIds, forExport) },
                )
            }

            is ProviderSetting.Claude -> {
                provider.copy(
                    apiKey = if (shouldExportPrimary) getApiKey(provider.id, provider.apiKey) else "",
                    apiKeyPool = poolWithSecrets,
                    models = provider.models.map { hydrateModelProviderOverwrite(it, selectedKeyIds, forExport) },
                )
            }

            is ProviderSetting.ComfyUI -> provider.copy(
                models = provider.models.map { hydrateModelProviderOverwrite(it, selectedKeyIds, forExport) },
            )
            is ProviderSetting.LiteRtLocal -> provider.copy(
                models = provider.models.map { hydrateModelProviderOverwrite(it, selectedKeyIds, forExport) },
            )
        }

        updated.resolvedApiKeyPool = resolvedPool
        return updated
    }

    /**
     * Hydrate nested [me.lucid.ai.provider.Model.providerOverwrite] secrets.
     * Overwrites are stored on models and were previously skipped by populate,
     * so chat with an overwrite sent empty credentials after key-pool migration.
     */
    private fun hydrateModelProviderOverwrite(
        model: me.lucid.ai.provider.Model,
        selectedKeyIds: Set<Uuid>? = null,
        forExport: Boolean = false,
    ): me.lucid.ai.provider.Model {
        val overwrite = model.providerOverwrite ?: return model
        return model.copy(providerOverwrite = populateProviderSecrets(overwrite, selectedKeyIds, forExport))
    }

    private fun populateTtsProviderSecrets(provider: TTSProviderSetting): TTSProviderSetting {
        return when (provider) {
            is TTSProviderSetting.OpenAI -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.Gemini -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.MiniMax -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.ElevenLabs -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.Qwen -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.Cartesia -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.FishAudio -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.PlayHT -> {
                provider.copy(apiKey = getTtsApiKey(provider.id, provider.apiKey))
            }

            is TTSProviderSetting.SystemTTS -> provider
        }
    }

    // ========== Search Service Secrets Management ==========

    fun migrateSearchServiceSecrets(service: SearchServiceOptions): SearchServiceOptions {
        return when (service) {
            is SearchServiceOptions.ZhipuOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.TavilyOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.ExaOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.LinkUpOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.BraveOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.MetasoOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.OllamaOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.PerplexityOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.FirecrawlOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.JinaOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.BochaOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.NanoGPTOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.GrokOptions -> {
                if (service.apiKey.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}", service.apiKey)
                    service.copy(apiKey = "")
                } else service
            }
            is SearchServiceOptions.SearXNGOptions -> {
                var s = service
                if (service.username.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_USER_PREFIX${service.id}", service.username)
                    s = s.copy(username = "")
                }
                if (service.password.isNotBlank()) {
                    secureStore.putSecret("$SEARCH_SERVICE_PASS_PREFIX${service.id}", service.password)
                    s = s.copy(password = "")
                }
                s
            }
            is SearchServiceOptions.KeylessOptions,
            is SearchServiceOptions.BingLocalOptions -> service
        }
    }

    fun populateSearchServiceSecrets(service: SearchServiceOptions): SearchServiceOptions {
        val apiKey = secureStore.getSecret("$SEARCH_SERVICE_KEY_PREFIX${service.id}")
        return when (service) {
            is SearchServiceOptions.ZhipuOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.TavilyOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.ExaOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.LinkUpOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.BraveOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.MetasoOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.OllamaOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.PerplexityOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.FirecrawlOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.JinaOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.BochaOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.NanoGPTOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.GrokOptions -> if (!apiKey.isNullOrBlank()) service.copy(apiKey = apiKey) else service
            is SearchServiceOptions.SearXNGOptions -> {
                val user = secureStore.getSecret("$SEARCH_SERVICE_USER_PREFIX${service.id}") ?: service.username
                val pass = secureStore.getSecret("$SEARCH_SERVICE_PASS_PREFIX${service.id}") ?: service.password
                service.copy(username = user, password = pass)
            }
            is SearchServiceOptions.KeylessOptions,
            is SearchServiceOptions.BingLocalOptions -> service
        }
    }

    fun stripSearchServiceSecrets(service: SearchServiceOptions): SearchServiceOptions {
        return when (service) {
            is SearchServiceOptions.ZhipuOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.TavilyOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.ExaOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.LinkUpOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.BraveOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.MetasoOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.OllamaOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.PerplexityOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.FirecrawlOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.JinaOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.BochaOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.NanoGPTOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.GrokOptions -> service.copy(apiKey = "")
            is SearchServiceOptions.SearXNGOptions -> service.copy(username = "", password = "")
            is SearchServiceOptions.KeylessOptions,
            is SearchServiceOptions.BingLocalOptions -> service
        }
    }

    private fun removeSearchServiceSecrets(serviceId: Uuid) {
        secureStore.removeSecret("$SEARCH_SERVICE_KEY_PREFIX$serviceId")
        secureStore.removeSecret("$SEARCH_SERVICE_USER_PREFIX$serviceId")
        secureStore.removeSecret("$SEARCH_SERVICE_PASS_PREFIX$serviceId")
    }

    // ========== MCP Server Secrets Management ==========

    private fun isAuthHeader(key: String): Boolean {
        val lower = key.trim().lowercase()
        return lower == "authorization" || lower.contains("auth") || lower.contains("token") || lower.contains("key") || lower.contains("secret")
    }

    fun migrateMcpServerSecrets(server: McpServerConfig): McpServerConfig {
        var modified = false
        val newHeaders = server.commonOptions.headers.map { (k, v) ->
            if (isAuthHeader(k) && v.isNotBlank()) {
                secureStore.putSecret("$MCP_SERVER_HEADER_PREFIX${server.id}_$k", v)
                modified = true
                k to ""
            } else {
                k to v
            }
        }
        return if (modified) {
            server.clone(commonOptions = server.commonOptions.copy(headers = newHeaders))
        } else {
            server
        }
    }

    fun populateMcpServerSecrets(server: McpServerConfig): McpServerConfig {
        var modified = false
        val newHeaders = server.commonOptions.headers.map { (k, v) ->
            if (isAuthHeader(k)) {
                val secret = secureStore.getSecret("$MCP_SERVER_HEADER_PREFIX${server.id}_$k")
                if (!secret.isNullOrBlank() && secret != v) {
                    modified = true
                    k to secret
                } else {
                    k to v
                }
            } else {
                k to v
            }
        }
        return if (modified) {
            server.clone(commonOptions = server.commonOptions.copy(headers = newHeaders))
        } else {
            server
        }
    }

    fun stripMcpServerSecrets(server: McpServerConfig): McpServerConfig {
        val stripped = server.commonOptions.headers.map { (k, v) ->
            if (isAuthHeader(k)) k to "" else k to v
        }
        return server.clone(commonOptions = server.commonOptions.copy(headers = stripped))
    }

    private fun removeMcpServerSecrets(serverId: Uuid) {
        val prefix = "$MCP_SERVER_HEADER_PREFIX${serverId}_"
        secureStore.getAllKeys()
            .filter { it.startsWith(prefix) }
            .forEach { secureStore.removeSecret(it) }
    }

    /**
     * Import secrets from backup settings and store them encrypted.
     * This should be called after restoring settings from a backup file.
     */
    fun importSecretsFromBackup(settings: Settings): Settings {
        // Same as migration - store secrets and clear plaintext
        return migrateSecretsFromSettings(settings)
    }
}
private fun ASRProviderSetting.apiKeyOrBlank(): String {
    return when (this) {
        is ASRProviderSetting.OpenAICompatible -> apiKey
        is ASRProviderSetting.OpenAIRealtime -> apiKey
        is ASRProviderSetting.DashScope -> apiKey
        is ASRProviderSetting.Volcengine -> apiKey
        is ASRProviderSetting.MiMo -> apiKey
        is ASRProviderSetting.Step -> apiKey
        is ASRProviderSetting.SystemSTT -> ""
    }
}
