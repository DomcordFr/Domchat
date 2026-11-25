package me.lucid.domchat.ui.pages.assistant

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.common.platform.PlatformLog
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.deletion.DESTRUCTIVE_UNDO_WINDOW_MS
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.data.repository.AppStorageRepository
import me.lucid.domchat.data.repository.MemoryRepository
import me.lucid.domchat.service.ChatService
import kotlin.uuid.Uuid
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

data class AssistantDeleteImpact(
    val conversationCount: Int,
    val memoryCount: Int,
)

class AssistantVM(
    private val settingsStore: SettingsStore,
    private val memoryRepository: MemoryRepository,
    private val conversationRepo: ConversationRepository,
    private val appScope: me.lucid.domchat.AppScope,
    private val appStorageRepository: AppStorageRepository,
    private val chatService: ChatService,
) : ViewModel() {
    val settings: StateFlow<Settings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, Settings.dummy())

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsStore.update(settings)
        }
    }

    fun updateAssistant(assistant: Assistant) {
        viewModelScope.launch {
            try {
                settingsStore.update { current ->
                    current.copy(
                        assistants = current.assistants.map { if (it.id == assistant.id) assistant else it }
                    )
                }
            } catch (e: Exception) {
                PlatformLog.e("AssistantVM", e.message ?: e.toString())
            }
        }
    }

    suspend fun addAssistant(assistant: Assistant) {
        try {
            settingsStore.update { current ->
                val newAssistant = if (assistant.name.isBlank()) {
                    assistant.copy(
                        name = "Generical",
                        avatar = Avatar.Resource(me.lucid.domchat.R.drawable.default_generical_pfp),
                        enableTimeAwareness = false,
                        systemPrompt = """
                            You are the best generic assistant, called {{char}}. {{char}} is a really nice guy. He doesn't use emojis though. Use the search tool when looking for factual info. You can have opinions if the user asks you for one. 

                            **Context:
                            - You are currently chatting to {{user}}
                            - You are running on {{model_name}}
                            - Date: {{cur_date}}

                            **Additional info:
                            - The UI supports LaTeX rendering
                            - The user is chatting to you trough an app called DomChat
                            - You are an AI/LLM and shouldn't hide this fact
                        """.trimIndent()
                        )
                } else {
                    assistant
                }
                val seededAssistant = newAssistant.copy(
                    maxSearchResultsRetained = newAssistant.maxSearchResultsRetained ?: 10
                )
                val existingIds = current.assistants.map { it.id }.toSet()
                val uniqueAssistant = if (seededAssistant.id in existingIds) {
                    seededAssistant.copy(id = Uuid.random())
                } else {
                    seededAssistant
                }
                current.copy(
                    assistants = current.assistants.plus(uniqueAssistant).distinctBy { it.id }
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            PlatformLog.e("AssistantVM", e.message ?: e.toString())
        }
    }

    suspend fun createConversation(assistantId: Uuid): Conversation {
        return chatService.createConversation(assistantId)
    }

    private val deletionJobs = java.util.concurrent.ConcurrentHashMap<kotlin.uuid.Uuid, kotlinx.coroutines.Job>()

    suspend fun loadDeleteImpact(assistant: Assistant): AssistantDeleteImpact = withContext(Dispatchers.IO) {
        // Room reads must never propagate a corrupt-db/query exception to the caller.
        val conversationCount = try {
            conversationRepo.getConversationCountOfAssistant(assistant.id)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            0
        }
        val memoryCount = try {
            memoryRepository.getMemoryCountOfAssistant(assistant.id.toString())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            0
        }
        AssistantDeleteImpact(
            conversationCount = conversationCount,
            memoryCount = memoryCount,
        )
    }

    fun removeAssistant(assistant: Assistant) {
        if (assistant.id == me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID) return
        // Cancel any existing job for this assistant
        deletionJobs[assistant.id]?.cancel()

        val job = appScope.launch {
            try {
                val chatCount = withContext(Dispatchers.IO) {
                    conversationRepo.getConversationCountOfAssistant(assistant.id)
                }
                settingsStore.update { settings ->
                    val fallbackAssistantId = if (settings.assistantId == assistant.id) {
                        settings.assistants.firstOrNull { it.id != assistant.id && !it.isDeleted }?.id
                            ?: me.lucid.domchat.data.datastore.DEFAULT_ASSISTANT_ID
                    } else {
                        settings.assistantId
                    }
                    val updatedAssistants = if (chatCount > 0) {
                        settings.assistants.map {
                            if (it.id == assistant.id) it.copy(isDeleted = true) else it
                        }
                    } else {
                        settings.assistants.filter { it.id != assistant.id }
                    }
                    settings.copy(
                        assistantId = fallbackAssistantId,
                        assistants = updatedAssistants,
                        recentlyUsedAssistants = settings.recentlyUsedAssistants.filter { it != assistant.id },
                    )
                }
                if (chatCount == 0) {
                    // Delayed purge for unused character so Undo restores instantly
                    kotlinx.coroutines.delay(DESTRUCTIVE_UNDO_WINDOW_MS)
                    val latestCount = withContext(Dispatchers.IO) {
                        conversationRepo.getConversationCountOfAssistant(assistant.id)
                    }
                    if (latestCount == 0) {
                        memoryRepository.deleteMemoriesOfAssistant(assistant.id.toString())
                        appStorageRepository.deleteFilesIfUnreferenced(assistant.collectMediaFileRefs())
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                PlatformLog.e("AssistantVM", "Deferred deletion failed for ${assistant.id}: ${e.message ?: e.toString()}")
            }
            deletionJobs.remove(assistant.id)
        }
        deletionJobs[assistant.id] = job
    }

    fun undoRemoveAssistant(assistant: Assistant): Boolean {
        val job = deletionJobs.remove(assistant.id)
        job?.cancel()
        val restored = assistant.copy(isDeleted = false)

        viewModelScope.launch {
            // Restore to settings
            try {
                settingsStore.update { settings ->
                    val hasExisting = settings.assistants.any { it.id == assistant.id }
                    val updated = if (hasExisting) {
                        settings.assistants.map {
                            if (it.id == assistant.id) it.copy(isDeleted = false) else it
                        }
                    } else {
                        settings.assistants.plus(restored)
                    }
                    settings.copy(assistants = updated)
                }
            } catch (e: Exception) {
                PlatformLog.e("AssistantVM", e.message ?: e.toString())
            }
        }
        return true
    }

    fun copyAssistant(assistant: Assistant) {
        viewModelScope.launch {
            val copiedAssistant = assistant.copy(
                id = kotlin.uuid.Uuid.random(),
                name = "${assistant.name} (Clone)",
                avatar = if(assistant.avatar is Avatar.Image) Avatar.Dummy else assistant.avatar,
            )
            try {
                settingsStore.update { current ->
                    current.copy(
                        assistants = current.assistants.plus(copiedAssistant)
                    )
                }
            } catch (e: Exception) {
                PlatformLog.e("AssistantVM", e.message ?: e.toString())
            }
        }
    }

    /**
     * Returns the latest conversation [updateAt] for the given assistant, or null if no
     * conversations exist. Runs on Dispatchers.IO.
     */
    suspend fun getAssistantLastUpdateAt(assistantId: Uuid): Instant? {
        return withContext(Dispatchers.IO) {
            conversationRepo.getConversationsOfAssistant(assistantId)
                .first()
                .maxByOrNull { it.updateAt }?.updateAt
        }
    }

    suspend fun getAllAssistantsLastUpdateAt(): Map<Uuid, Instant> =
        conversationRepo.getAllAssistantsLastUpdateAt()

    fun getMemories(assistant: Assistant) =
        memoryRepository.getMemoriesOfAssistantFlow(assistant.id.toString())

    suspend fun loadGroupNames(context: Context): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            context.getSharedPreferences(HUB_GROUP_PREFS, Context.MODE_PRIVATE)
                .getString(PREFS_KEY_GROUPS, null)?.let {
                    me.lucid.domchat.utils.JsonInstant.decodeFromString<Map<String, String>>(it)
                } ?: emptyMap()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyMap()
        }
    }

    suspend fun loadGroupOrder(context: Context): List<String> = withContext(Dispatchers.IO) {
        try {
            context.getSharedPreferences(HUB_GROUP_PREFS, Context.MODE_PRIVATE)
                .getString(PREFS_KEY_ORDER, null)?.let {
                    me.lucid.domchat.utils.JsonInstant.decodeFromString<List<String>>(it)
                } ?: emptyList()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    suspend fun loadCharAssignments(context: Context): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            context.getSharedPreferences(HUB_GROUP_PREFS, Context.MODE_PRIVATE)
                .getString(PREFS_KEY_ASSIGNMENTS, null)?.let {
                    me.lucid.domchat.utils.JsonInstant.decodeFromString<Map<String, String>>(it)
                } ?: emptyMap()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyMap()
        }
    }

    suspend fun saveGroups(
        context: Context,
        groups: Map<String, String>,
        order: List<String>,
        assignments: Map<String, String>,
    ) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(HUB_GROUP_PREFS, Context.MODE_PRIVATE).edit()
            prefs.putString(PREFS_KEY_GROUPS, me.lucid.domchat.utils.JsonInstant.encodeToString(groups))
            prefs.putString(PREFS_KEY_ORDER, me.lucid.domchat.utils.JsonInstant.encodeToString(order))
            prefs.putString(PREFS_KEY_ASSIGNMENTS, me.lucid.domchat.utils.JsonInstant.encodeToString(assignments))
            prefs.apply()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            /* best-effort persistence */
        }
    }
}

const val HUB_GROUP_PREFS = "character_hub_groups"
const val PREFS_KEY_GROUPS = "group_names"
const val PREFS_KEY_ASSIGNMENTS = "char_assignments"
const val PREFS_KEY_ORDER = "group_order"

private fun Assistant.collectMediaFileRefs(): List<String> {
    return buildList {
        (avatar as? Avatar.Image)?.url?.let(::add)
        background?.let(::add)
    }
}
