package me.lucid.domchat.data.sync

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.lucid.ai.provider.ProviderSetting
import me.lucid.search.SearchServiceOptions
import me.lucid.tts.provider.TTSProviderSetting
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.SecretKeyManager
import me.lucid.domchat.data.datastore.sanitize
import me.lucid.domchat.data.db.AppDatabase
import me.lucid.domchat.utils.BoundedStreams
import me.lucid.domchat.utils.BoundedStreams.copyBounded
import me.lucid.domchat.utils.BoundedStreams.readBoundedBytes
import me.lucid.domchat.utils.LogUtil
import me.lucid.workspace.hasUsableRootfs
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "DataSync"

class WebdavSync(
    private val settingsStore: SettingsStore,
    private val json: Json,
    private val context: Context,
    private val secretKeyManager: SecretKeyManager,
    private val appDatabase: AppDatabase,
) {
    suspend fun restoreFromLocalFile(file: File): RestoreResult =
        withContext(Dispatchers.IO) {
            LogUtil.i(TAG, "restoreFromLocalFile: Starting restore from ${file.absolutePath}")

            if (!file.exists()) {
                throw Exception("Backup file does not exist")
            }

            if (!file.canRead()) {
                throw Exception("Cannot read backup file")
            }

            try {
                restoreFromBackupFile(file)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                LogUtil.e(TAG, "restoreFromLocalFile: Failed to restore from local file", e)
                throw Exception("Restore failed: ${e.message}")
            }
        }

    suspend fun prepareBackupFile(
        selectedContent: Set<BackupContentOption>? = null,
        selectedKeyIds: Set<kotlin.uuid.Uuid>? = null,
    ): File = AppMaintenanceLock.withLock {
        withContext(Dispatchers.IO) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File(context.cacheDir, "Domchat_backup_$timestamp.zip")
        if (backupFile.exists()) {
            backupFile.delete()
        }

        // Determine which managed dirs and DB table sets to include based on selectedContent.
        // When selectedContent is null (e.g., full backup), everything is included.
        val managedDirsToExport = if (selectedContent == null) {
            BackupArchiveFormat.MANAGED_FILE_DIRS
        } else {
            buildList {
                if (BackupContentOption.CHARACTERS in selectedContent) {
                    add("avatars")
                    add("assistant_backgrounds")
                }
                if (BackupContentOption.CHATS in selectedContent) {
                    add("upload")
                    add("chat_files")
                    add("tool_outputs")
                    add("images")
                }
                if (BackupContentOption.PROVIDERS in selectedContent) {
                    add("auto_icons")
                }
                if (BackupContentOption.SETTINGS in selectedContent) {
                    add("wallpapers")
                    add("backgrounds")
                    add("custom_fonts")
                    add("custom_icons")
                    add("auto_icons")
                }
                if (BackupContentOption.WORKSPACES in selectedContent) {
                    add(BackupArchiveFormat.WORKSPACES_DIR)
                }
                if (BackupContentOption.SKILLS in selectedContent) {
                    add("skills")
                }
                if (BackupContentOption.LOREBOOKS in selectedContent) {
                    add("lorebook_covers")
                    add("lorebook_attachments")
                }
            }.distinct().filter { BackupArchiveFormat.MANAGED_FILE_DIRS.contains(it) }
        }

        val allowedDbTables: Set<String>? = if (selectedContent == null) {
            null // null = all tables
        } else {
            buildSet {
                if (BackupContentOption.CHATS in selectedContent) addAll(DatabaseSanitizer.CHAT_TABLES)
                if (BackupContentOption.WORKSPACES in selectedContent) addAll(DatabaseSanitizer.WORKSPACE_TABLES)
            }
        }

        val includesDatabase = allowedDbTables == null || allowedDbTables.isNotEmpty()
        val includesFiles = managedDirsToExport.isNotEmpty()

        val manifest = BackupManifest(
            includesDatabase = includesDatabase,
            includesFiles = includesFiles,
            managedFileDirs = if (includesFiles) managedDirsToExport else emptyList(),
            sharedPrefsStores = BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES,
            selectedContent = selectedContent?.map { it.name } ?: emptyList(),
            workspacesMetadata = collectWorkspaceExportMetadata(
                include = selectedContent == null || BackupContentOption.WORKSPACES in selectedContent,
            ),
        )

        ZipOutputStream(backupFile.sink().buffer().outputStream()).use { zipOut ->
            val includeApiKeys = selectedContent == null || BackupContentOption.API_KEYS in selectedContent
            val includeSettings = selectedContent == null || BackupContentOption.SETTINGS in selectedContent
            val rawSettings = settingsStore.settingsFlow.value
            val settingsForExport = secretKeyManager.populateSecretsForExport(
                settings = rawSettings,
                selectedKeyIds = if (!includeApiKeys) emptySet() else selectedKeyIds,
                includeApiKeys = includeApiKeys,
                includeSettings = includeSettings,
            )

            // Apply content filtering to the Settings object
            val filteredSettings = if (selectedContent == null) {
                settingsForExport
            } else {
                filterSettingsForExport(settingsForExport, selectedContent)
            }

            addVirtualFileToZip(
                zipOut = zipOut,
                name = BackupArchiveFormat.SETTINGS_ENTRY,
                content = json.encodeToString(filteredSettings),
            )
            addVirtualFileToZip(
                zipOut = zipOut,
                name = BackupArchiveFormat.MANIFEST_ENTRY,
                content = json.encodeToString(manifest),
            )

            if (selectedContent == null || BackupContentOption.PROVIDERS in selectedContent || BackupContentOption.SETTINGS in selectedContent) {
                runCatching {
                    val installedLlm = me.lucid.locallm.LocalModelStore(context).current()
                    if (installedLlm.isNotEmpty()) {
                        addVirtualFileToZip(
                            zipOut = zipOut,
                            name = BackupArchiveFormat.LOCAL_LLM_MODELS_ENTRY,
                            content = json.encodeToString(installedLlm),
                        )
                    }
                }
                runCatching {
                    val installedStt = me.lucid.asr.local.SherpaModelStore(context).current()
                    if (installedStt.isNotEmpty()) {
                        addVirtualFileToZip(
                            zipOut = zipOut,
                            name = BackupArchiveFormat.LOCAL_STT_MODELS_ENTRY,
                            content = json.encodeToString(installedStt),
                        )
                    }
                }
            }

            BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES.forEach { storeName ->
                val snapshot = exportSharedPreferencesSnapshot(context, storeName)
                addVirtualFileToZip(
                    zipOut = zipOut,
                    name = BackupArchiveFormat.prefEntryName(storeName),
                    content = json.encodeToString(snapshot),
                )
            }

            if (includesDatabase) {
                if (!checkpointDatabase()) {
                    throw java.io.IOException("Database is busy and could not be checkpointed safely")
                }
                if (allowedDbTables != null && allowedDbTables.isEmpty()) {
                    // No DB tables selected — skip database entirely
                    LogUtil.i(TAG, "prepareBackupFile: Skipping database (no DB content selected)")
                } else if (allowedDbTables != null) {
                    // Export a filtered copy of the database
                    val filteredDb = DatabaseSanitizer.createFilteredExportDatabase(context, allowedDbTables)
                    try {
                        addFilteredDatabaseEntries(zipOut, filteredDb)
                    } finally {
                        // Clean up temp export db (use the sanitized name pattern)
                        context.deleteDatabase(filteredDb.name)
                    }
                } else {
                    addDatabaseEntries(zipOut)
                }
            }

            if (includesFiles) {
                addManagedFileEntries(zipOut, managedDirsToExport)
            }
        }

        backupFile
    }
}

    /**
     * Snapshot workspace environment metadata so a restore can reinstall the
     * (excluded-from-zip) Linux rootfs in the background. Filesystem checks
     * only — no proot execution — so this stays cheap during export.
     */
    private suspend fun collectWorkspaceExportMetadata(include: Boolean): List<WorkspaceExportMetadata> {
        if (!include) return emptyList()
        val entities = runCatching { appDatabase.workspaceDao().getAll() }
            .getOrDefault(emptyList())
        if (entities.isEmpty()) return emptyList()
        val workspacesRoot = File(context.filesDir, BackupArchiveFormat.WORKSPACES_DIR)
        return entities.map { ws ->
            val linuxDir = File(File(workspacesRoot, ws.root), "linux")
            val hasRootfs = runCatching { linuxDir.hasUsableRootfs() }.getOrDefault(false)
            val hasPython = if (hasRootfs) {
                File(linuxDir, "usr/bin/python3").isFile || File(linuxDir, "bin/python3").isFile
            } else {
                false
            }
            WorkspaceExportMetadata(
                id = ws.id,
                name = ws.name,
                root = ws.root,
                hasRootfs = hasRootfs,
                hasPython = hasPython,
                rootfsUrl = null,
            )
        }
    }

    /**
     * Filter a [Settings] object to only include fields corresponding to [selectedContent].
     * Non-selected fields are replaced with their empty/default equivalents so no
     * structural data (provider IDs, etc.) is lost, but content the user opted-out of is blank.
     */
    private fun filterSettingsForExport(
        settings: Settings,
        selectedContent: Set<BackupContentOption>,
    ): Settings {
        var result = settings
        if (BackupContentOption.CHARACTERS !in selectedContent) {
            result = result.copy(assistants = emptyList())
        }
        if (BackupContentOption.PROVIDERS !in selectedContent) {
            result = result.copy(providers = emptyList(), ttsProviders = emptyList())
        }
        if (BackupContentOption.API_KEYS !in selectedContent) {
            result = result.copy(
                searchServices = result.searchServices.map { svc -> stripSearchServiceSecrets(svc) },
                mcpServers = result.mcpServers.map { server ->
                    val strippedHeaders = server.commonOptions.headers.filter { (header, _) ->
                        val lower = header.lowercase()
                        !lower.contains("auth") &&
                        !lower.contains("key") &&
                        !lower.contains("token") &&
                        !lower.contains("bearer") &&
                        !lower.contains("secret") &&
                        !lower.contains("cookie")
                    }
                    server.clone(
                        commonOptions = server.commonOptions.copy(headers = strippedHeaders)
                    )
                }
            )
        }
        if (BackupContentOption.LOREBOOKS !in selectedContent) {
            result = result.copy(lorebooks = emptyList())
        }
        if (BackupContentOption.SKILLS !in selectedContent) {
            result = result.copy(skills = emptyList())
        }
        // If SETTINGS is not selected, reset general appearance/behavior preferences to defaults
        // so they don't override the importing device's settings. Data lists (providers, assistants,
        // lorebooks, skills) are already handled by their respective options above.
        if (BackupContentOption.SETTINGS !in selectedContent) {
            val defaults = Settings()
            result = result.copy(
                dynamicColor = defaults.dynamicColor,
                themeId = defaults.themeId,
                developerMode = defaults.developerMode,
                enableRagLogging = defaults.enableRagLogging,
                displaySetting = defaults.displaySetting,
                enableWebSearch = defaults.enableWebSearch,
                titlePrompt = defaults.titlePrompt,
                translatePrompt = defaults.translatePrompt,
                suggestionPrompt = defaults.suggestionPrompt,
                learningModePrompt = defaults.learningModePrompt,
                ocrPrompt = defaults.ocrPrompt,
                ttsAutoplayMode = defaults.ttsAutoplayMode,
                chatStorage = defaults.chatStorage,
                textSelectionConfig = defaults.textSelectionConfig,
                assistantOverlayConfig = defaults.assistantOverlayConfig,
                webServerEnabled = defaults.webServerEnabled,
                webServerPort = defaults.webServerPort,
                webServerJwtEnabled = defaults.webServerJwtEnabled,
                webServerAccessPassword = defaults.webServerAccessPassword,
                webDavConfig = defaults.webDavConfig,
            )
        }
        return result
    }

    private fun stripSearchServiceSecrets(svc: SearchServiceOptions): SearchServiceOptions = when (svc) {
        is SearchServiceOptions.KeylessOptions -> svc
        is SearchServiceOptions.BingLocalOptions -> svc
        is SearchServiceOptions.ZhipuOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.TavilyOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.ExaOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.SearXNGOptions -> svc.copy(username = "", password = "")
        is SearchServiceOptions.LinkUpOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.BraveOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.MetasoOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.OllamaOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.PerplexityOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.FirecrawlOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.JinaOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.BochaOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.NanoGPTOptions -> svc.copy(apiKey = "")
        is SearchServiceOptions.GrokOptions -> svc.copy(apiKey = "")
    }


    data class RestoreResult(
        val sanitization: DatabaseSanitizer.SanitizationResult,
        val settingsCleanup: BackupCleanupResult,
        val workspacesMetadata: List<WorkspaceExportMetadata> = emptyList(),
    )

    private suspend fun restoreFromBackupFile(backupFile: File): RestoreResult =
        AppMaintenanceLock.withLock {
            withContext(Dispatchers.IO) {
                LogUtil.i(TAG, "restoreFromBackupFile: Starting restore from ${backupFile.absolutePath}")

                var unsupportedZipEntriesBytes = 0L
                var settingsCleanupResult = BackupCleanupResult()
                var sanitizationResult = DatabaseSanitizer.SanitizationResult()
                var settingsJson: String? = null
                var manifest: BackupManifest? = null
                val stagedPrefs = linkedMapOf<String, SharedPreferencesSnapshot>()
                val stagedManagedDirs = linkedSetOf<String>()
                var stagedLlmModels: List<me.lucid.locallm.InstalledLocalModel>? = null
                var stagedSttModels: List<me.lucid.asr.local.InstalledSherpaModel>? = null
                var foundSupportedEntry = false
                var totalDecompressedBytes = 0L

                fun recordDecompressed(bytes: Long) {
                    totalDecompressedBytes += bytes
                    if (totalDecompressedBytes > BoundedStreams.MAX_ARCHIVE_TOTAL_BYTES) {
                        throw IOException("Backup archive decompressed size exceeded maximum budget of ${BoundedStreams.MAX_ARCHIVE_TOTAL_BYTES} bytes")
                    }
                }

                val restoreTempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}")
                val stagedFilesDir = File(restoreTempDir, "files")
                val stagedDbDir = File(restoreTempDir, "db")
                restoreTempDir.mkdirs()
                stagedFilesDir.mkdirs()
                stagedDbDir.mkdirs()

                try {
                    ZipInputStream(backupFile.source().buffer().inputStream()).use { zipIn ->
                        var entry: ZipEntry?
                        while (zipIn.nextEntry.also { entry = it } != null) {
                            val zipEntry = entry ?: continue
                            val normalizedName = BackupArchiveFormat.normalizeEntryName(zipEntry.name)
                            LogUtil.i(TAG, "restoreFromBackupFile: Processing entry $normalizedName")

                            when {
                                normalizedName == BackupArchiveFormat.SETTINGS_ENTRY -> {
                                    val bytes = zipIn.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES)
                                    recordDecompressed(bytes.size.toLong())
                                    settingsJson = bytes.toString(Charsets.UTF_8)
                                    foundSupportedEntry = true
                                }

                                normalizedName == BackupArchiveFormat.MANIFEST_ENTRY -> {
                                    val bytes = zipIn.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES)
                                    recordDecompressed(bytes.size.toLong())
                                    val manifestJson = bytes.toString(Charsets.UTF_8)
                                    manifest = json.decodeFromString<BackupManifest>(manifestJson)
                                    foundSupportedEntry = true
                                }

                                normalizedName == BackupArchiveFormat.LOCAL_LLM_MODELS_ENTRY -> {
                                    val bytes = zipIn.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES)
                                    recordDecompressed(bytes.size.toLong())
                                    val content = bytes.toString(Charsets.UTF_8)
                                    runCatching {
                                        stagedLlmModels = json.decodeFromString<List<me.lucid.locallm.InstalledLocalModel>>(content)
                                    }
                                    foundSupportedEntry = true
                                }

                                normalizedName == BackupArchiveFormat.LOCAL_STT_MODELS_ENTRY -> {
                                    val bytes = zipIn.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES)
                                    recordDecompressed(bytes.size.toLong())
                                    val content = bytes.toString(Charsets.UTF_8)
                                    runCatching {
                                        stagedSttModels = json.decodeFromString<List<me.lucid.asr.local.InstalledSherpaModel>>(content)
                                    }
                                    foundSupportedEntry = true
                                }

                                BackupArchiveFormat.isDatabaseEntry(normalizedName) -> {
                                    val written = stageDatabaseEntry(zipIn, stagedDbDir, normalizedName)
                                    recordDecompressed(written)
                                    foundSupportedEntry = true
                                }

                                normalizedName.startsWith("${BackupArchiveFormat.PREFS_DIR}/") &&
                                    normalizedName.endsWith(".json") -> {
                                    val storeName = normalizedName
                                        .removePrefix("${BackupArchiveFormat.PREFS_DIR}/")
                                        .removeSuffix(".json")
                                    if (BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES.contains(storeName)) {
                                        val bytes = zipIn.readBoundedBytes(BoundedStreams.MAX_JSON_ENTRY_BYTES)
                                        recordDecompressed(bytes.size.toLong())
                                        val snapshotJson = bytes.toString(Charsets.UTF_8)
                                        val snapshot =
                                            json.decodeFromString<SharedPreferencesSnapshot>(snapshotJson)
                                        stagedPrefs[storeName] = snapshot
                                        foundSupportedEntry = true
                                    } else {
                                        unsupportedZipEntriesBytes += zipEntry.size.coerceAtLeast(0L)
                                    }
                                }

                                BackupArchiveFormat.managedDirForEntry(normalizedName) != null -> {
                                    val managedDir = BackupArchiveFormat.managedDirForEntry(normalizedName)
                                        ?: error("Managed directory was null for $normalizedName")
                                    val written = stageManagedFileEntry(zipIn, stagedFilesDir, normalizedName, zipEntry.isDirectory)
                                    recordDecompressed(written)
                                    stagedManagedDirs.add(managedDir)
                                    foundSupportedEntry = true
                                }

                                else -> {
                                    LogUtil.i(
                                        TAG,
                                        "restoreFromBackupFile: Skipping unsupported entry $normalizedName (${zipEntry.size} bytes)",
                                    )
                                    unsupportedZipEntriesBytes += zipEntry.size.coerceAtLeast(0L)
                                }
                            }

                            zipIn.closeEntry()
                        }
                    }

                    if (!foundSupportedEntry) {
                        throw Exception("Backup file did not contain any supported entries")
                    }

                    val sanitizedSettings = settingsJson?.let { rawSettingsJson ->
                        try {
                            val parsedSettings = json.decodeFromString<Settings>(rawSettingsJson)
                            parsedSettings.sanitize().also { (_, cleanup) ->
                                settingsCleanupResult = cleanup
                            }
                        } catch (e: Exception) {
                            LogUtil.e(TAG, "restoreFromBackupFile: Failed to parse settings", e)
                            throw Exception("Failed to restore settings: ${e.message}")
                        }
                    }

                    var stagedCleanDb: File? = null
                    val stagedDatabase = File(stagedDbDir, BackupArchiveFormat.DB_ENTRY)
                    if (stagedDatabase.exists()) {
                        LogUtil.i(TAG, "restoreFromBackupFile: Starting database sanitization")
                        try {
                            val (cleanDb, result) = DatabaseSanitizer.sanitize(context, stagedDatabase)
                            sanitizationResult = result
                            stagedCleanDb = cleanDb

                            // If restoring a selective backup (where only a subset of tables were exported),
                            // copy any unselected live tables into cleanDb so the user's unselected live data
                            // (e.g. workspaces or chats) is NOT wiped when cleanDb replaces the live DB.
                            if (manifest != null && manifest.selectedContent.isNotEmpty()) {
                                val tablesInBackup = buildSet {
                                    if (BackupContentOption.CHATS.name in manifest.selectedContent) addAll(DatabaseSanitizer.CHAT_TABLES)
                                    if (BackupContentOption.WORKSPACES.name in manifest.selectedContent) addAll(DatabaseSanitizer.WORKSPACE_TABLES)
                                }
                                val liveDbFile = context.getDatabasePath(BackupArchiveFormat.DB_ENTRY)
                                if (liveDbFile.exists()) {
                                    val tablesToPreserve = DatabaseSanitizer.PORTABLE_TABLES.filter { it !in tablesInBackup }
                                    if (tablesToPreserve.isNotEmpty()) {
                                        if (!checkpointDatabase()) {
                                            LogUtil.w(TAG, "restoreFromBackupFile: WAL checkpoint returned busy before table copy")
                                        }
                                        DatabaseSanitizer.copyTablesBetweenDatabases(
                                            sourceDbFile = liveDbFile,
                                            targetDbFile = cleanDb,
                                            tables = tablesToPreserve,
                                        )
                                    }
                                }
                            }

                            resetWorkspaceShellStatesInStagedDb(cleanDb)
                            runCatching {
                                android.database.sqlite.SQLiteDatabase.openDatabase(
                                    cleanDb.path,
                                    null,
                                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                                ).use { db ->
                                    db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { cursor ->
                                        if (cursor.moveToFirst() && cursor.getInt(0) != 0) {
                                            throw java.io.IOException("Staged DB WAL checkpoint busy")
                                        }
                                    }
                                }
                            }.getOrThrow()
                            LogUtil.i(TAG, "restoreFromBackupFile: Staged database sanitized and shell states adjusted")
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            LogUtil.e(TAG, "restoreFromBackupFile: Failed to stage database", e)
                            throw Exception("Database sanitization failed: ${e.message}")
                        }
                    }

                    val finalSettings = sanitizedSettings?.let { (cleanedSettings, _) ->
                        if (manifest != null && manifest.selectedContent.isNotEmpty()) {
                            val live = settingsStore.settingsFlow.value
                            val selected = manifest.selectedContent.toSet()

                            val resolvedProviders = if (BackupContentOption.PROVIDERS.name in selected) {
                                if (BackupContentOption.API_KEYS.name in selected) {
                                    cleanedSettings.providers
                                } else {
                                    val liveMap = live.providers.associateBy { it.id }
                                    cleanedSettings.providers.map { restored ->
                                        val liveProv = liveMap[restored.id]
                                        if (liveProv != null) {
                                            val liveModelMap = liveProv.models.associateBy { it.id }
                                            val modelsWithPreservedOverwrites = restored.models.map { restoredModel ->
                                                val liveModel = liveModelMap[restoredModel.id]
                                                val restoredOverwrite = restoredModel.providerOverwrite
                                                val liveOverwrite = liveModel?.providerOverwrite
                                                if (restoredOverwrite != null && liveOverwrite != null) {
                                                    restoredModel.copy(
                                                        providerOverwrite = mergeProviderCredentials(
                                                            restoredOverwrite,
                                                            liveOverwrite,
                                                        )
                                                    )
                                                } else {
                                                    restoredModel
                                                }
                                            }
                                            when (restored) {
                                                is ProviderSetting.OpenAI -> (liveProv as? ProviderSetting.OpenAI)?.let {
                                                    restored.copy(apiKey = it.apiKey, apiKeyPool = it.apiKeyPool, models = modelsWithPreservedOverwrites)
                                                } ?: restored.copy(models = modelsWithPreservedOverwrites)
                                                is ProviderSetting.Google -> (liveProv as? ProviderSetting.Google)?.let {
                                                    restored.copy(apiKey = it.apiKey, privateKey = it.privateKey, apiKeyPool = it.apiKeyPool, models = modelsWithPreservedOverwrites)
                                                } ?: restored.copy(models = modelsWithPreservedOverwrites)
                                                is ProviderSetting.Claude -> (liveProv as? ProviderSetting.Claude)?.let {
                                                    restored.copy(apiKey = it.apiKey, apiKeyPool = it.apiKeyPool, models = modelsWithPreservedOverwrites)
                                                } ?: restored.copy(models = modelsWithPreservedOverwrites)
                                                is ProviderSetting.ComfyUI -> restored.copy(models = modelsWithPreservedOverwrites)
                                                is ProviderSetting.LiteRtLocal -> restored.copy(models = modelsWithPreservedOverwrites)
                                            }
                                        } else restored
                                    }
                                }
                            } else {
                                live.providers
                            }

                            val resolvedTtsProviders = if (BackupContentOption.PROVIDERS.name in selected) {
                                if (BackupContentOption.API_KEYS.name in selected) {
                                    cleanedSettings.ttsProviders
                                } else {
                                    val liveTtsMap = live.ttsProviders.associateBy { it.id }
                                    cleanedSettings.ttsProviders.map { restored ->
                                        val liveTts = liveTtsMap[restored.id]
                                        if (liveTts != null) {
                                            when (restored) {
                                                is TTSProviderSetting.OpenAI -> (liveTts as? TTSProviderSetting.OpenAI)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.Gemini -> (liveTts as? TTSProviderSetting.Gemini)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.MiniMax -> (liveTts as? TTSProviderSetting.MiniMax)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.ElevenLabs -> (liveTts as? TTSProviderSetting.ElevenLabs)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.Qwen -> (liveTts as? TTSProviderSetting.Qwen)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.Cartesia -> (liveTts as? TTSProviderSetting.Cartesia)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.FishAudio -> (liveTts as? TTSProviderSetting.FishAudio)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.PlayHT -> (liveTts as? TTSProviderSetting.PlayHT)?.let { restored.copy(apiKey = it.apiKey) } ?: restored
                                                is TTSProviderSetting.SystemTTS -> restored
                                            }
                                        } else restored
                                    }
                                }
                            } else {
                                live.ttsProviders
                            }

                            live.copy(
                                setupCompleted = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.setupCompleted else live.setupCompleted,
                                assistantId = if (BackupContentOption.CHARACTERS.name in selected || BackupContentOption.SETTINGS.name in selected) cleanedSettings.assistantId else live.assistantId,
                                assistants = if (BackupContentOption.CHARACTERS.name in selected) cleanedSettings.assistants else live.assistants,
                                assistantTags = if (BackupContentOption.CHARACTERS.name in selected) cleanedSettings.assistantTags else live.assistantTags,
                                recentlyUsedAssistants = if (BackupContentOption.CHARACTERS.name in selected) cleanedSettings.recentlyUsedAssistants else live.recentlyUsedAssistants,
                                providers = resolvedProviders,
                                ttsProviders = resolvedTtsProviders,
                                providerTags = if (BackupContentOption.PROVIDERS.name in selected) cleanedSettings.providerTags else live.providerTags,
                                lorebooks = if (BackupContentOption.LOREBOOKS.name in selected) cleanedSettings.lorebooks else live.lorebooks,
                                skills = if (BackupContentOption.SKILLS.name in selected) cleanedSettings.skills else live.skills,
                                mcpServers = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.mcpServers else live.mcpServers,
                                searchServices = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.searchServices else live.searchServices,
                                searchCommonOptions = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.searchCommonOptions else live.searchCommonOptions,
                                searchServiceSelected = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.searchServiceSelected else live.searchServiceSelected,
                                modes = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.modes else live.modes,
                                dynamicColor = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.dynamicColor else live.dynamicColor,
                                themeId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.themeId else live.themeId,
                                developerMode = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.developerMode else live.developerMode,
                                enableRagLogging = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.enableRagLogging else live.enableRagLogging,
                                displaySetting = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.displaySetting else live.displaySetting,
                                enableWebSearch = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.enableWebSearch else live.enableWebSearch,
                                favoriteModels = if (BackupContentOption.SETTINGS.name in selected || BackupContentOption.PROVIDERS.name in selected) cleanedSettings.favoriteModels else live.favoriteModels,
                                recentModelIds = if (BackupContentOption.SETTINGS.name in selected || BackupContentOption.PROVIDERS.name in selected) cleanedSettings.recentModelIds else live.recentModelIds,
                                chatModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.chatModelId else live.chatModelId,
                                titleModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.titleModelId else live.titleModelId,
                                titleThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.titleThinkingBudget else live.titleThinkingBudget,
                                summarizerModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.summarizerModelId else live.summarizerModelId,
                                summarizerThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.summarizerThinkingBudget else live.summarizerThinkingBudget,
                                subagentModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.subagentModelId else live.subagentModelId,
                                subagentThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.subagentThinkingBudget else live.subagentThinkingBudget,
                                imageGenerationModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.imageGenerationModelId else live.imageGenerationModelId,
                                titlePrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.titlePrompt else live.titlePrompt,
                                translateModeId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.translateModeId else live.translateModeId,
                                translatePrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.translatePrompt else live.translatePrompt,
                                suggestionModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.suggestionModelId else live.suggestionModelId,
                                suggestionThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.suggestionThinkingBudget else live.suggestionThinkingBudget,
                                suggestionPrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.suggestionPrompt else live.suggestionPrompt,
                                learningModePrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.learningModePrompt else live.learningModePrompt,
                                ocrModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.ocrModelId else live.ocrModelId,
                                ocrThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.ocrThinkingBudget else live.ocrThinkingBudget,
                                ocrPrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.ocrPrompt else live.ocrPrompt,
                                embeddingModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.embeddingModelId else live.embeddingModelId,
                                selectedTTSProviderId = if (BackupContentOption.SETTINGS.name in selected || BackupContentOption.PROVIDERS.name in selected) cleanedSettings.selectedTTSProviderId else live.selectedTTSProviderId,
                                selectedTTSVoiceId = if (BackupContentOption.SETTINGS.name in selected || BackupContentOption.PROVIDERS.name in selected) cleanedSettings.selectedTTSVoiceId else live.selectedTTSVoiceId,
                                ttsAutoplayMode = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.ttsAutoplayMode else live.ttsAutoplayMode,
                                sttModelId = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.sttModelId else live.sttModelId,
                                sttThinkingBudget = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.sttThinkingBudget else live.sttThinkingBudget,
                                sttPrompt = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.sttPrompt else live.sttPrompt,
                                webServerEnabled = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.webServerEnabled else live.webServerEnabled,
                                webServerPort = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.webServerPort else live.webServerPort,
                                webServerJwtEnabled = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.webServerJwtEnabled else live.webServerJwtEnabled,
                                webServerAccessPassword = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.webServerAccessPassword else live.webServerAccessPassword,
                                webServerBackgroundSetupShown = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.webServerBackgroundSetupShown else live.webServerBackgroundSetupShown,
                                chatStorage = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.chatStorage else live.chatStorage,
                                textSelectionConfig = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.textSelectionConfig else live.textSelectionConfig,
                                assistantOverlayConfig = if (BackupContentOption.SETTINGS.name in selected) cleanedSettings.assistantOverlayConfig else live.assistantOverlayConfig,
                                huggingFaceToken = if (BackupContentOption.API_KEYS.name in selected) cleanedSettings.huggingFaceToken else live.huggingFaceToken,
                                mcpOAuthTokens = if (BackupContentOption.API_KEYS.name in selected) cleanedSettings.mcpOAuthTokens else live.mcpOAuthTokens,
                            )
                        } else {
                            cleanedSettings
                        }
                    }

                    // Files last: never wipe live dirs until DB/settings have committed, and
                    // skip dirs that were listed in the manifest but missing from the zip.
                    val directoriesToRestore = resolveManagedDirsToRestore(
                        manifest = manifest,
                        stagedManagedDirs = stagedManagedDirs,
                        stagedFilesDir = stagedFilesDir,
                    )

                    // Live rollback snapshots
                    val liveSettings = settingsStore.settingsFlow.value
                    val livePrefsSnapshots = BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES.associateWith {
                        exportSharedPreferencesSnapshot(context, it)
                    }
                    val liveDbBackupDir = File(restoreTempDir, "live_db_backup")
                    val liveFilesBackupDir = File(restoreTempDir, "live_files_bak")
                    val finalDbFile = context.getDatabasePath(BackupArchiveFormat.DB_ENTRY)

                    var dbTouched = false
                    var settingsCommitted = false
                    var prefsCommitted = false
                    var managedFilesSession: ManagedFileRestoreSession? = null
                    var liveLlmModelsSnapshot: List<me.lucid.locallm.InstalledLocalModel>? = null
                    var liveSttModelsSnapshot: List<me.lucid.asr.local.InstalledSherpaModel>? = null

                    try {
                        // Step 1: Atomic Database Swap
                        if (stagedCleanDb != null && stagedCleanDb.exists()) {
                            if (!checkpointDatabase()) {
                                LogUtil.w(TAG, "restoreFromBackupFile: WAL checkpoint returned busy before database swap")
                            }
                            appDatabase.close()
                            liveDbBackupDir.mkdirs()
                            if (finalDbFile.exists()) {
                                finalDbFile.copyTo(File(liveDbBackupDir, BackupArchiveFormat.DB_ENTRY), overwrite = true)
                            }
                            val liveWal = File(finalDbFile.parentFile, BackupArchiveFormat.WAL_ENTRY)
                            if (liveWal.exists()) {
                                liveWal.copyTo(File(liveDbBackupDir, BackupArchiveFormat.WAL_ENTRY), overwrite = true)
                            }
                            val liveShm = File(finalDbFile.parentFile, BackupArchiveFormat.SHM_ENTRY)
                            if (liveShm.exists()) {
                                liveShm.copyTo(File(liveDbBackupDir, BackupArchiveFormat.SHM_ENTRY), overwrite = true)
                            }

                            dbTouched = true
                            context.deleteDatabase(BackupArchiveFormat.DB_ENTRY)
                            finalDbFile.parentFile?.mkdirs()
                            if (!stagedCleanDb.renameTo(finalDbFile)) {
                                finalDbFile.delete()
                                stagedCleanDb.copyTo(finalDbFile, overwrite = true)
                            }
                            File(finalDbFile.path + "-wal").delete()
                            File(finalDbFile.path + "-shm").delete()
                            context.deleteDatabase("rikka_hub_sanitized")
                        }

                        // Step 2: Settings Commit
                        if (finalSettings != null) {
                            settingsStore.update(finalSettings)
                            settingsCommitted = true
                            LogUtil.i(TAG, "restoreFromBackupFile: Settings restored before file commit")
                        }

                        // Step 3: SharedPreferences Commit
                        if (stagedPrefs.isNotEmpty()) {
                            restorePortableSharedPreferences(
                                stagedSnapshots = stagedPrefs,
                                manifest = manifest,
                            )
                            prefsCommitted = true
                        }

                        // Step 4: Managed Files Commit
                        if (directoriesToRestore.isNotEmpty()) {
                            managedFilesSession = restoreManagedFileDirectories(
                                liveFilesDir = context.filesDir,
                                stagedFilesDir = stagedFilesDir,
                                directoriesToRestore = directoriesToRestore,
                                liveBackupDir = liveFilesBackupDir,
                            )
                        }

                        // Step 5: Local Models Commit (staged in memory, committed after DB/settings swap)
                        if (stagedLlmModels != null && (manifest == null || manifest.selectedContent.isEmpty() || BackupContentOption.SETTINGS.name in manifest.selectedContent || BackupContentOption.PROVIDERS.name in manifest.selectedContent)) {
                            val store = me.lucid.locallm.LocalModelStore(context)
                            liveLlmModelsSnapshot = store.current()
                            stagedLlmModels?.forEach { store.upsert(it) }
                        }
                        if (stagedSttModels != null && (manifest == null || manifest.selectedContent.isEmpty() || BackupContentOption.SETTINGS.name in manifest.selectedContent || BackupContentOption.PROVIDERS.name in manifest.selectedContent)) {
                            val store = me.lucid.asr.local.SherpaModelStore(context)
                            liveSttModelsSnapshot = store.current()
                            stagedSttModels?.forEach { store.upsert(it) }
                        }

                        managedFilesSession?.cleanup()
                        LogUtil.i(TAG, "restoreFromBackupFile: Restore completed successfully")
                    } catch (error: Throwable) {
                        LogUtil.e(TAG, "restoreFromBackupFile: Commit failed, rolling back changes", error)
                        withContext(NonCancellable) {
                            if (managedFilesSession != null) {
                                runCatching {
                                    managedFilesSession.rollback()
                                    managedFilesSession.cleanup()
                                }
                            }
                            if (liveLlmModelsSnapshot != null) {
                                runCatching {
                                    val store = me.lucid.locallm.LocalModelStore(context)
                                    store.clear()
                                    liveLlmModelsSnapshot.forEach { store.upsert(it) }
                                }
                            }
                            if (liveSttModelsSnapshot != null) {
                                runCatching {
                                    val store = me.lucid.asr.local.SherpaModelStore(context)
                                    store.clear()
                                    liveSttModelsSnapshot.forEach { store.upsert(it) }
                                }
                            }
                            if (prefsCommitted) {
                                runCatching {
                                    livePrefsSnapshots.forEach { (storeName, snapshot) ->
                                        val prefs = context.applicationContext.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                                        restoreSharedPreferencesSnapshot(prefs, snapshot)
                                    }
                                }
                            }
                            if (settingsCommitted) {
                                runCatching { settingsStore.update(liveSettings) }
                            }
                            if (dbTouched) {
                                runCatching {
                                    context.deleteDatabase(BackupArchiveFormat.DB_ENTRY)
                                    val backupDb = File(liveDbBackupDir, BackupArchiveFormat.DB_ENTRY)
                                    if (backupDb.exists()) {
                                        finalDbFile.parentFile?.mkdirs()
                                        if (!backupDb.renameTo(finalDbFile)) {
                                            backupDb.copyTo(finalDbFile, overwrite = true)
                                        }
                                    }
                                    val backupWal = File(liveDbBackupDir, BackupArchiveFormat.WAL_ENTRY)
                                    if (backupWal.exists()) {
                                        val finalWal = File(finalDbFile.parentFile, BackupArchiveFormat.WAL_ENTRY)
                                        backupWal.copyTo(finalWal, overwrite = true)
                                    }
                                    val backupShm = File(liveDbBackupDir, BackupArchiveFormat.SHM_ENTRY)
                                    if (backupShm.exists()) {
                                        val finalShm = File(finalDbFile.parentFile, BackupArchiveFormat.SHM_ENTRY)
                                        backupShm.copyTo(finalShm, overwrite = true)
                                    }
                                }
                            }
                        }
                        throw error
                    }

                    val totalCleanupResult = settingsCleanupResult.copy(
                        unsupportedZipEntriesBytes = unsupportedZipEntriesBytes,
                    )
                    RestoreResult(
                        sanitization = sanitizationResult,
                        settingsCleanup = totalCleanupResult,
                        workspacesMetadata = manifest?.workspacesMetadata ?: emptyList(),
                    )
                } finally {
                    restoreTempDir.deleteRecursively()
                }
            }
        }

    private fun checkpointDatabase(): Boolean {
        for (attempt in 1..3) {
            val success = runCatching {
                appDatabase.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                    if (cursor.moveToFirst()) {
                        val busy = cursor.getInt(0)
                        val log = cursor.getInt(1)
                        val checkpointed = cursor.getInt(2)
                        LogUtil.i(TAG, "checkpointDatabase attempt $attempt: busy=$busy, log=$log, checkpointed=$checkpointed")
                        busy == 0
                    } else {
                        false
                    }
                }
            }.getOrElse { error ->
                LogUtil.w(TAG, "prepareBackupFile: WAL checkpoint attempt $attempt failed", error)
                false
            }
            if (success) return true
            if (attempt < 3) Thread.sleep(50)
        }
        return false
    }

    private fun snapshotDatabaseToFile(context: Context, targetFile: File): Boolean {
        if (!checkpointDatabase()) {
            LogUtil.w(TAG, "snapshotDatabaseToFile: Checkpoint returned busy/failed")
            return false
        }
        targetFile.parentFile?.mkdirs()
        return try {
            val db = appDatabase.openHelper.writableDatabase
            val escapedPath = targetFile.absolutePath.replace("'", "''")
            db.execSQL("VACUUM INTO '$escapedPath'")
            targetFile.exists() && targetFile.length() > 0
        } catch (e: Exception) {
            LogUtil.w(TAG, "VACUUM INTO failed or unsupported: ${e.message}")
            targetFile.delete()
            false
        }
    }

    private fun addDatabaseEntries(zipOut: ZipOutputStream) {
        val tempSnapshot = File(context.cacheDir, "db_snapshot_${System.currentTimeMillis()}.db")
        try {
            val success = snapshotDatabaseToFile(context, tempSnapshot)
            if (success && tempSnapshot.exists()) {
                addFileToZip(zipOut, tempSnapshot, BackupArchiveFormat.LEGACY_DB_ENTRY)
            } else {
                throw java.io.IOException("Database snapshot failed and database is busy")
            }
        } finally {
            if (tempSnapshot.exists()) {
                tempSnapshot.delete()
            }
        }
    }

    private fun addManagedFileEntries(zipOut: ZipOutputStream, dirs: List<String> = BackupArchiveFormat.MANAGED_FILE_DIRS) {
        dirs.forEach { dirName ->
            val directory = File(context.filesDir, dirName)
            val skip: (String, Boolean) -> Boolean =
                if (dirName == BackupArchiveFormat.WORKSPACES_DIR) {
                    { relative, isDir -> BackupArchiveFormat.isExcludedWorkspacePath(relative, isDir) }
                } else {
                    { _, _ -> false }
                }
            enumerateDirectoryEntries(directory, dirName, skip).forEach { entry ->
                try {
                    if (entry.isDirectory) {
                        addDirectoryToZip(zipOut, entry.entryName)
                    } else {
                        addFileToZip(zipOut, entry.source, entry.entryName)
                    }
                } catch (e: Exception) {
                    LogUtil.w(TAG, "addManagedFileEntries: Failed to zip entry ${entry.entryName} from source ${entry.source.absolutePath}", e)
                }
            }
        }
    }

    private fun addFilteredDatabaseEntries(zipOut: ZipOutputStream, filteredDbFile: File) {
        if (filteredDbFile.exists()) {
            addFileToZip(zipOut, filteredDbFile, BackupArchiveFormat.LEGACY_DB_ENTRY)
        }
        val walFile = File(filteredDbFile.parentFile, filteredDbFile.name + "-wal")
        if (walFile.exists()) {
            addFileToZip(zipOut, walFile, BackupArchiveFormat.WAL_ENTRY)
        }
        val shmFile = File(filteredDbFile.parentFile, filteredDbFile.name + "-shm")
        if (shmFile.exists()) {
            addFileToZip(zipOut, shmFile, BackupArchiveFormat.SHM_ENTRY)
        }
    }


    private fun stageDatabaseEntry(
        zipIn: ZipInputStream,
        stagedDbDir: File,
        entryName: String,
    ): Long {
        val targetName = when (entryName) {
            BackupArchiveFormat.LEGACY_DB_ENTRY, BackupArchiveFormat.DB_ENTRY -> BackupArchiveFormat.DB_ENTRY
            BackupArchiveFormat.WAL_ENTRY -> BackupArchiveFormat.WAL_ENTRY
            BackupArchiveFormat.SHM_ENTRY -> BackupArchiveFormat.SHM_ENTRY
            else -> error("Unsupported database entry: $entryName")
        }
        val targetFile = File(stagedDbDir, targetName)
        targetFile.parentFile?.mkdirs()
        return targetFile.sink().buffer().outputStream().use { outputStream ->
            zipIn.copyBounded(outputStream, BoundedStreams.MAX_FILE_ENTRY_BYTES)
        }
    }

    private fun stageManagedFileEntry(
        zipIn: ZipInputStream,
        stagedFilesDir: File,
        entryName: String,
        isDirectory: Boolean,
    ): Long {
        val targetFile = safeZipDestination(stagedFilesDir, entryName)
        if (isDirectory || entryName.endsWith('/')) {
            targetFile.mkdirs()
            return 0L
        }

        targetFile.parentFile?.mkdirs()
        return targetFile.sink().buffer().outputStream().use { outputStream ->
            zipIn.copyBounded(outputStream, BoundedStreams.MAX_FILE_ENTRY_BYTES)
        }
    }

    private fun restorePortableSharedPreferences(
        stagedSnapshots: Map<String, SharedPreferencesSnapshot>,
        manifest: BackupManifest?,
    ) {
        if (manifest != null && manifest.selectedContent.isNotEmpty() && BackupContentOption.SETTINGS.name !in manifest.selectedContent) {
            LogUtil.i(TAG, "restorePortableSharedPreferences: Skipping shared preferences restore because SETTINGS was not selected")
            return
        }
        val prefStoresToRestore = if (manifest?.formatVersion == BackupArchiveFormat.CURRENT_FORMAT_VERSION) {
            manifest.sharedPrefsStores
                .filter { BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES.contains(it) }
                .distinct()
        } else {
            stagedSnapshots.keys
                .filter { BackupArchiveFormat.PORTABLE_SHARED_PREF_STORES.contains(it) }
                .sorted()
        }

        prefStoresToRestore.forEach { storeName ->
            val prefs = context.applicationContext.getSharedPreferences(storeName, Context.MODE_PRIVATE)
            val snapshot = stagedSnapshots[storeName]
            if (snapshot != null) {
                restoreSharedPreferencesSnapshot(prefs, snapshot)
            }
        }
    }

    /**
     * The backup zip excludes each workspace's Linux rootfs, so after a restore
     * any workspace row still marked READY/INSTALLING would be a lie. Reset those
     * to DISABLED directly inside the staged SQLite database when the rootfs is
     * absent so Room is never queried mid-restore while closed.
     */
    private fun resetWorkspaceShellStatesInStagedDb(stagedDbFile: File) {
        if (!stagedDbFile.exists()) return
        val workspacesRoot = File(context.filesDir, BackupArchiveFormat.WORKSPACES_DIR)
        val now = System.currentTimeMillis()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(stagedDbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            val cursor = db.rawQuery("SELECT id, root, shell_status FROM workspaces", null)
            val updates = mutableListOf<String>()
            cursor.use { c ->
                val idIdx = c.getColumnIndex("id")
                val rootIdx = c.getColumnIndex("root")
                val statusIdx = c.getColumnIndex("shell_status")
                if (idIdx >= 0 && rootIdx >= 0 && statusIdx >= 0) {
                    while (c.moveToNext()) {
                        val id = c.getString(idIdx)
                        val root = c.getString(rootIdx)
                        val status = c.getString(statusIdx)
                        if (status == me.lucid.workspace.WorkspaceShellStatus.READY.name ||
                            status == me.lucid.workspace.WorkspaceShellStatus.INSTALLING.name
                        ) {
                            val linuxDir = File(File(workspacesRoot, root), "linux")
                            val present = runCatching { linuxDir.hasUsableRootfs() }.getOrDefault(false)
                            if (!present) {
                                updates.add(id)
                            }
                        }
                    }
                }
            }
            if (updates.isNotEmpty()) {
                db.beginTransaction()
                try {
                    val stmt = db.compileStatement("UPDATE workspaces SET shell_status = ?, updated_at = ? WHERE id = ?")
                    for (id in updates) {
                        stmt.bindString(1, me.lucid.workspace.WorkspaceShellStatus.DISABLED.name)
                        stmt.bindLong(2, now)
                        stmt.bindString(3, id)
                        stmt.executeUpdateDelete()
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Exception) {
            LogUtil.w(TAG, "resetWorkspaceShellStatesInStagedDb failed", e)
        } finally {
            db?.close()
        }
    }
}

private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
    file.source().buffer().inputStream().use { inputStream ->
        val zipEntry = ZipEntry(entryName)
        zipOut.putNextEntry(zipEntry)
        inputStream.copyTo(zipOut)
        zipOut.closeEntry()
        LogUtil.d(TAG, "addFileToZip: Added $entryName (${file.length()} bytes) to zip")
    }
}

private fun addDirectoryToZip(zipOut: ZipOutputStream, entryName: String) {
    val normalizedName = if (entryName.endsWith('/')) entryName else "$entryName/"
    val zipEntry = ZipEntry(normalizedName)
    zipOut.putNextEntry(zipEntry)
    zipOut.closeEntry()
}

private fun addVirtualFileToZip(zipOut: ZipOutputStream, name: String, content: String) {
    val zipEntry = ZipEntry(name)
    zipOut.putNextEntry(zipEntry)
    zipOut.write(content.toByteArray())
    zipOut.closeEntry()
    LogUtil.i(TAG, "addVirtualFileToZip: $name (${content.length} bytes)")
}

private fun mergeProviderCredentials(restored: ProviderSetting, live: ProviderSetting?): ProviderSetting {
    if (live == null) return restored
    return when (restored) {
        is ProviderSetting.OpenAI -> (live as? ProviderSetting.OpenAI)?.let {
            restored.copy(apiKey = it.apiKey, apiKeyPool = it.apiKeyPool)
        } ?: restored
        is ProviderSetting.Google -> (live as? ProviderSetting.Google)?.let {
            restored.copy(apiKey = it.apiKey, privateKey = it.privateKey, apiKeyPool = it.apiKeyPool)
        } ?: restored
        is ProviderSetting.Claude -> (live as? ProviderSetting.Claude)?.let {
            restored.copy(apiKey = it.apiKey, apiKeyPool = it.apiKeyPool)
        } ?: restored
        is ProviderSetting.ComfyUI -> restored
        is ProviderSetting.LiteRtLocal -> restored
    }
}

