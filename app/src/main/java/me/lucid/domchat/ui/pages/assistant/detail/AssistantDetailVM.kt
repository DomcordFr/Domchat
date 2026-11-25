package me.lucid.domchat.ui.pages.assistant.detail

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.lucid.domchat.data.ai.rag.EmbeddingAvailability
import me.lucid.domchat.data.ai.rag.EmbeddingService
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.getCurrentAssistant
import me.lucid.domchat.data.db.dao.ChatEpisodeDAO
import me.lucid.domchat.data.db.entity.ChatEpisodeEntity
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.AssistantMemory
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.Tag
import me.lucid.domchat.data.repository.AppStorageRepository
import me.lucid.domchat.data.repository.MemoryRepository
import kotlin.uuid.Uuid

private const val TAG = "AssistantDetailVM"

data class MemoryRetrievalDebugState(
    val query: String = "",
    val hasRun: Boolean = false,
    val isRunning: Boolean = false,
    val results: List<Pair<AssistantMemory, Float>> = emptyList(),
    val configuredThreshold: Float = 0f,
    val totalMemories: Int = 0,
    val currentEmbeddings: Int = 0,
    val includesCore: Boolean = true,
    val includesEpisodes: Boolean = true,
    val error: String? = null,
)

class AssistantDetailVM(
    private val id: String,
    private val settingsStore: SettingsStore,
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: me.lucid.domchat.data.repository.ConversationRepository,
    private val chatEpisodeDAO: ChatEpisodeDAO,
    private val providerManager: me.lucid.ai.provider.ProviderManager,
    private val appStorageRepository: AppStorageRepository,
    private val embeddingService: EmbeddingService,
) : ViewModel() {
    private val assistantId = runCatching { Uuid.parse(id) }
        .onFailure { Log.w(TAG, "Invalid assistant id route parameter: $id", it) }
        .getOrElse { me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID }

    val settings: StateFlow<Settings> =
        settingsStore.settingsFlow.stateIn(viewModelScope, SharingStarted.Lazily, Settings.dummy())

    val mcpServerConfigs = settingsStore
        .settingsFlow.map { settings ->
            settings.mcpServers
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
        )

    val assistant: StateFlow<Assistant> = settingsStore
        .settingsFlow
        .map { settings ->
            settings.assistants.find { it.id == assistantId } ?: Assistant(id = assistantId)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = Assistant()
        )

    val isNewAssistant: StateFlow<Boolean> = settingsStore
        .settingsFlow
        .map { settings ->
            settings.assistants.none { it.id == assistantId }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = true
        )

    private val _memorySearchQuery = MutableStateFlow("")
    val memorySearchQuery = _memorySearchQuery.asStateFlow()

    fun updateMemorySearchQuery(query: String) {
        _memorySearchQuery.value = query
    }

    val memories = combine(
        memoryRepository.getMemoriesOfAssistantFlow(assistantId.toString()),
        chatEpisodeDAO.getEpisodesOfAssistantFlow(assistantId.toString()),
        _memorySearchQuery
    ) { coreMemories, episodes, query ->
        val core = coreMemories
        val episodic = episodes.map { 
            AssistantMemory(
                id = -it.id, // Negative ID to distinguish from core memories
                content = it.content, 
                type = 1, // EPISODIC
                hasEmbedding = !it.embedding.isNullOrBlank() || it.embeddingBlob != null,
                embeddingModelId = it.embeddingModelId,
                timestamp = it.startTime,
                significance = it.significance
            ) 
        }
        val allMemories = core + episodic
        if (query.isBlank()) {
            allMemories
        } else {
            allMemories.filter { it.content.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
    )

    // Current embedding model ID for this assistant (for detecting model mismatch)
    val currentEmbeddingModelId: StateFlow<String> = combine(
        assistant,
        settings
    ) { assistant, settings ->
        (assistant.embeddingModelId ?: settings.embeddingModelId).toString()
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = ""
    )

    val embeddingStatus: StateFlow<String?> = combine(assistant, settings) { currentAssistant, _ ->
        when (val availability = embeddingService.getAvailability(currentAssistant.id.toString())) {
            is EmbeddingAvailability.Available -> null
            is EmbeddingAvailability.Unavailable -> availability.reason
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val episodes = chatEpisodeDAO.getEpisodesOfAssistantFlow(assistantId.toString())
        .stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
        )

    val episodeStats = combine(episodes, memories) { episodeList, memoryList ->
        val totalEpisodes = episodeList.size
        val avgSig = if (totalEpisodes > 0) {
            episodeList.sumOf { it.significance }.toDouble() / totalEpisodes
        } else {
            0.0
        }
        val coreCount = memoryList.count { it.type == 0 } // 0 is CORE
        EpisodeStats(totalEpisodes, avgSig, coreCount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, EpisodeStats(0, 0.0, 0))

    val providers = settingsStore
        .settingsFlow
        .map { settings ->
            settings.providers
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
        )

    val tags = settingsStore
        .settingsFlow
        .map { settings ->
            settings.assistantTags
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
        )

    // Draft state retained across back/forward navigation to prevent reload loss
    private val _draftAssistant = MutableStateFlow<Assistant?>(null)
    val draftAssistant: StateFlow<Assistant?> = _draftAssistant.asStateFlow()

    fun loadDraft() {
        val current = assistant.value
        if (_draftAssistant.value == null) {
            _draftAssistant.value = current
        }
    }

    fun updateDraft(assistant: Assistant) {
        _draftAssistant.value = assistant
    }

    fun commitDraft() {
        _draftAssistant.value?.let { draft ->
            update(draft)
            _draftAssistant.value = null
        }
    }

    fun discardDraft() {
        _draftAssistant.value = null
    }

    fun updateTags(tagIds: List<Uuid>, tags: List<Tag>) {
        viewModelScope.launch {
            settingsStore.update { current ->
                val updatedAssistant = assistant.value.copy(tags = tagIds.toList())
                val updatedAssistants = current.assistants.map {
                    if (it.id == updatedAssistant.id) updatedAssistant else it
                }
                current.copy(
                    assistantTags = tags,
                    assistants = updatedAssistants
                )
            }
            
            Log.d(TAG, "updateTags: ${tagIds.joinToString(",")}")
            
            // Now cleanup unused tags using atomic update
            cleanupUnusedTagsInternal()
        }
    }

    private suspend fun cleanupUnusedTagsInternal() {
        settingsStore.update { settings ->
            val validTagIds = settings.assistantTags.map { it.id }.toSet()
            val cleanedAssistants = settings.assistants.map { assistant ->
                val validTags = assistant.tags.filter { tagId ->
                    validTagIds.contains(tagId)
                }
                if (validTags.size != assistant.tags.size) {
                    assistant.copy(tags = validTags)
                } else {
                    assistant
                }
            }
            val usedTagIds = cleanedAssistants.flatMap { it.tags }.toSet()
            val cleanedTags = settings.assistantTags.filter { tag ->
                usedTagIds.contains(tag.id)
            }
            if (cleanedAssistants != settings.assistants || cleanedTags.size != settings.assistantTags.size) {
                settings.copy(
                    assistants = cleanedAssistants,
                    assistantTags = cleanedTags
                )
            } else {
                settings
            }
        }
    }

    fun cleanupUnusedTags() {
        viewModelScope.launch {
            cleanupUnusedTagsInternal()
        }
    }

    fun update(assistant: Assistant) {
        viewModelScope.launch {
            val oldAssistant = settingsStore.settingsFlow.value.assistants.find { it.id == assistant.id }
            if (oldAssistant != null) {
                checkAvatarDelete(old = oldAssistant, new = assistant)
                checkBackgroundDelete(old = oldAssistant, new = assistant)
            }
            var removedRefs = emptyList<String>()
            settingsStore.update { currentSettings ->
                val old = currentSettings.assistants.find { it.id == assistant.id }
                if (old != null) {
                    removedRefs = old.collectRemovedMediaRefs(new = assistant)
                }
                currentSettings.copy(
                    assistants = if (old != null) {
                        currentSettings.assistants.map {
                            if (it.id == assistant.id) assistant else it
                        }
                    } else {
                        currentSettings.assistants + assistant
                    }
                )
            }
            if (removedRefs.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    appStorageRepository.deleteFilesIfUnreferenced(removedRefs)
                }
            }
        }
    }

    fun addNewAssistant(assistant: Assistant) {
        viewModelScope.launch {
            val seededAssistant = assistant.copy(
                maxSearchResultsRetained = assistant.maxSearchResultsRetained ?: 10
            )
            settingsStore.update { currentSettings ->
                val existingIds = currentSettings.assistants.map { it.id }.toSet()
                val uniqueAssistant = if (seededAssistant.id in existingIds) {
                    seededAssistant.copy(id = Uuid.random())
                } else {
                    seededAssistant
                }
                currentSettings.copy(
                    assistants = currentSettings.assistants.plus(uniqueAssistant).distinctBy { it.id }
                )
            }
        }
    }

    fun addMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            memoryRepository.addMemory(
                assistantId = assistantId.toString(),
                content = memory.content
            )
        }
    }

    fun updateMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            if (memory.id < 0) {
                memoryRepository.updateEpisodeContent(id = -memory.id, content = memory.content)
            } else {
                memoryRepository.updateContent(id = memory.id, content = memory.content)
            }
        }
    }

    fun deleteMemory(memory: AssistantMemory) {
        viewModelScope.launch {
            if (memory.id < 0) {
                memoryRepository.deleteEpisode(id = -memory.id)
            } else if (memory.id > 0) {
                memoryRepository.deleteMemory(id = memory.id)
            }
        }
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    private val _embeddingProgress = MutableStateFlow<EmbeddingProgress?>(null)
    val embeddingProgress = _embeddingProgress.asStateFlow()

    // Check if any memories need embedding or have stale embeddings from a different model
    val needsEmbeddingRegeneration: StateFlow<Boolean> = combine(
        memories, currentEmbeddingModelId, embeddingStatus
    ) { memories, currentModelId, status ->
        status == null && memories.any { memory ->
            !memory.hasEmbedding ||
            (memory.embeddingModelId != null && memory.embeddingModelId != currentModelId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = false
    )

    private val _retrievalDebugState = MutableStateFlow(MemoryRetrievalDebugState())
    val retrievalDebugState = _retrievalDebugState.asStateFlow()

    fun testRetrieval(query: String) {
        viewModelScope.launch {
            val currentAssistant = assistant.value
            _retrievalDebugState.value = MemoryRetrievalDebugState(
                query = query,
                isRunning = true,
                configuredThreshold = currentAssistant.ragSimilarityThreshold,
                includesCore = currentAssistant.ragIncludeCore,
                includesEpisodes = currentAssistant.ragIncludeEpisodes,
            )
            try {
                val limit = if (currentAssistant.ragLimit > 50) {
                    9999
                } else if (currentAssistant.ragLimit > 0) {
                    currentAssistant.ragLimit
                } else {
                    10 // Default for debugging
                }.coerceAtMost(200)
                
                val memorySnapshot = memoryRepository.getCombinedMemoriesOfAssistant(assistantId.toString())
                val results = memoryRepository.retrieveRelevantMemoriesWithScores(
                    assistantId = assistantId.toString(),
                    query = query,
                    limit = limit.coerceAtLeast(50),
                    // A debugger must expose candidates below the live cutoff. Applying the
                    // production threshold here made a healthy zero-match result look like a dead button.
                    similarityThreshold = 0f,
                    includeCore = currentAssistant.ragIncludeCore,
                    includeEpisodes = currentAssistant.ragIncludeEpisodes
                )
                val currentModelId = runCatching {
                    embeddingService.getEmbeddingModelId(assistantId.toString())
                }.getOrNull()
                _retrievalDebugState.value = MemoryRetrievalDebugState(
                    query = query,
                    hasRun = true,
                    results = results,
                    configuredThreshold = currentAssistant.ragSimilarityThreshold,
                    totalMemories = memorySnapshot.size,
                    currentEmbeddings = memorySnapshot.count { memory ->
                        currentModelId != null && memory.hasEmbedding &&
                            memory.embeddingModelId == currentModelId
                    },
                    includesCore = currentAssistant.ragIncludeCore,
                    includesEpisodes = currentAssistant.ragIncludeEpisodes,
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to test retrieval", e)
                _snackbarMessage.value = "Retrieval failed: ${e.message}"
                _retrievalDebugState.value = _retrievalDebugState.value.copy(
                    hasRun = true,
                    isRunning = false,
                    error = e.message ?: e::class.simpleName ?: "Unknown retrieval error",
                )
            }
        }
    }

    fun clearRetrievalResults() {
        _retrievalDebugState.value = MemoryRetrievalDebugState()
    }

    fun regenerateEmbeddings() {
        viewModelScope.launch {
            try {
                _embeddingProgress.value = EmbeddingProgress(0, 1, true)
                
                val (success, failure) = memoryRepository.regenerateEmbeddings(
                    assistantId = assistantId.toString(),
                    onProgress = { current, total ->
                        _embeddingProgress.value = EmbeddingProgress(current, total, true)
                    }
                )
                
                _embeddingProgress.value = null
                if (failure > 0) {
                    _snackbarMessage.value = "Completed: $success success, $failure failed. Check your API key or Model settings."
                } else if (success > 0) {
                    _snackbarMessage.value = "Successfully generated $success embeddings."
                } else {
                    _snackbarMessage.value = "No embeddings needed regeneration."
                }
                Log.i(TAG, "Regenerated embeddings: $success success, $failure failed")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _embeddingProgress.value = null
                _snackbarMessage.value = "Error: ${e.message}"
                Log.e(TAG, "Failed to regenerate embeddings", e)
            }
        }
    }

    suspend fun checkAvatarDelete(old: Assistant, new: Assistant) {
        // Cleanup now happens after the settings update through orphan-aware storage checks.
    }

    suspend fun checkBackgroundDelete(old: Assistant, new: Assistant) {
        // Cleanup now happens after the settings update through orphan-aware storage checks.
    }

    // Token Estimation Logic
    fun estimateTokens(text: String): Int = text.length / 4

    val averageMessageLength = conversationRepository.getAverageMessageLength(assistantId)
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    val averageMemoryLength = memoryRepository.getAverageMemoryLength(assistantId.toString())
        .stateIn(viewModelScope, SharingStarted.Lazily, 150)

    val systemPromptTokenCount = assistant.map {
        estimateTokens(it.systemPrompt)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val smartMinTokenUsage = combine(
        assistant,
        averageMessageLength,
        averageMemoryLength
    ) { assistant, avgMsgLen, avgMemLen ->
        val sysPrompt = estimateTokens(assistant.systemPrompt)
        
        // Dynamic estimates based on history
        val avgMsgTokens = (avgMsgLen / 4).coerceAtLeast(10)
        val avgMemTokens = (avgMemLen / 4).coerceAtLeast(10)

        val minHistory = avgMsgTokens * 2 // At least 2 messages
        val minMemory = if (assistant.enableMemory) avgMemTokens * 2 else 0 // At least 2 memories
        val buffer = 200
        sysPrompt + minHistory + minMemory + buffer
    }.stateIn(viewModelScope, SharingStarted.Lazily, 1000)

    val estimatedMemoryCapacity = combine(
        assistant,
        smartMinTokenUsage,
        averageMemoryLength
    ) { assistant, minUsage, avgMemLen ->
        val total = assistant.maxTokenUsage
        val available = (total - minUsage).coerceAtLeast(0)
        val avgMemTokens = (avgMemLen / 4).coerceAtLeast(10)
        
        // If RAG is enabled, how many memories can we fit in the remaining space?
        // This is a rough upper bound for the slider
        (available / avgMemTokens).coerceAtLeast(5) // Minimum 5
    }.stateIn(viewModelScope, SharingStarted.Lazily, 10)

    val estimatedAllocation = combine(
        assistant,
        averageMessageLength,
        averageMemoryLength
    ) { assistant, avgMsgLen, avgMemLen ->
        val total = assistant.maxTokenUsage
        val sysPrompt = estimateTokens(assistant.systemPrompt)
        val remaining = total - sysPrompt

        if (remaining <= 0) {
            "System prompt consumes all tokens!"
        } else {
            val avgMsgTokens = (avgMsgLen / 4).coerceAtLeast(10)
            val avgMemTokens = (avgMemLen / 4).coerceAtLeast(10)

            // Calculate how many messages OR memories can fit in the remaining space
            val estHistoryMsgs = remaining / avgMsgTokens
            val estMemories = remaining / avgMemTokens

            "Est. History: ~$estHistoryMsgs msgs or Memories: ~$estMemories"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "Calculating...")

    // Validation for Export UI
    val hasMemories = combine(memories, episodes) { m, e ->
        m.isNotEmpty() || e.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val hasLorebooks = assistant.map { 
        it.enabledLorebookIds.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
}

data class EmbeddingProgress(
    val current: Int,
    val total: Int,
    val isRunning: Boolean
)

private fun Assistant.collectRemovedMediaRefs(
    new: Assistant,
): List<String> {
    return buildList {
        val oldAvatarUrl = (avatar as? Avatar.Image)?.url
        val newAvatarUrl = (new.avatar as? Avatar.Image)?.url
        if (oldAvatarUrl != null && oldAvatarUrl != newAvatarUrl) {
            add(oldAvatarUrl)
        }
        if (background != null && background != new.background) {
            add(background)
        }
    }
}

data class EpisodeStats(
    val totalEpisodes: Int,
    val averageSignificance: Double,
    val coreMemoryCount: Int
)
