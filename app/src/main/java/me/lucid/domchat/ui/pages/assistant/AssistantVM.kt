package me.lucid.domchat.ui.pages.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            settingsStore.update { current ->
                current.copy(
                    assistants = current.assistants.map { if (it.id == assistant.id) assistant else it }
                )
            }
        }
    }

    fun addAssistant(assistant: Assistant) {
        viewModelScope.launch {
            val settings = settings.value
            val newAssistant = if (assistant.name.isBlank()) {
                assistant.copy(
                    name = "Generical",
                    avatar = Avatar.Resource(me.lucid.domchat.R.drawable.default_generical_pfp),
                    enableTimeAwareness = true,
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
            val existingIds = settings.assistants.map { it.id }.toSet()
            val uniqueAssistant = if (seededAssistant.id in existingIds) {
                seededAssistant.copy(id = Uuid.random())
            } else {
                seededAssistant
            }
            val deduplicatedList = settings.assistants.plus(uniqueAssistant).distinctBy { it.id }
            settingsStore.update(
                settings.copy(
                    assistants = deduplicatedList
                )
            )
        }
    }

    suspend fun createConversation(assistantId: Uuid): Conversation {
        return chatService.createConversation(assistantId)
    }

    private val deletionJobs = java.util.concurrent.ConcurrentHashMap<kotlin.uuid.Uuid, kotlinx.coroutines.Job>()

    suspend fun loadDeleteImpact(assistant: Assistant): AssistantDeleteImpact = withContext(Dispatchers.IO) {
        AssistantDeleteImpact(
            conversationCount = conversationRepo.getConversationCountOfAssistant(assistant.id),
            memoryCount = memoryRepository.getMemoryCountOfAssistant(assistant.id.toString()),
        )
    }

    fun removeAssistant(assistant: Assistant) {
        // Cancel any existing job for this assistant
        deletionJobs[assistant.id]?.cancel()

        viewModelScope.launch {
            // Optimistic update: Remove from settings immediately
            settingsStore.update { settings ->
                settings.copy(
                    assistants = settings.assistants.filter { it.id != assistant.id }
                )
            }
        }

        // Start delayed deletion of data — same window as the undo toast.
        val job = appScope.launch {
            kotlinx.coroutines.delay(DESTRUCTIVE_UNDO_WINDOW_MS)
            val conversations = conversationRepo.getConversationsOfAssistant(assistant.id).first()
            conversations.forEach { conversation ->
                chatService.tombstoneConversation(conversation.id)
            }
            memoryRepository.deleteMemoriesOfAssistant(assistant.id.toString())
            conversationRepo.deleteConversationOfAssistant(assistant.id)
            appStorageRepository.deleteFilesIfUnreferenced(assistant.collectMediaFileRefs())
            deletionJobs.remove(assistant.id)
        }
        deletionJobs[assistant.id] = job
    }

    fun undoRemoveAssistant(assistant: Assistant): Boolean {
        val job = deletionJobs.remove(assistant.id) ?: return false
        job.cancel()

        viewModelScope.launch {
            // Restore to settings
            settingsStore.update { settings ->
                if (settings.assistants.none { it.id == assistant.id }) {
                    settings.copy(
                        assistants = settings.assistants.plus(assistant)
                    )
                } else {
                    settings
                }
            }
        }
        return true
    }

    fun copyAssistant(assistant: Assistant) {
        viewModelScope.launch {
            val settings = settings.value
            val copiedAssistant = assistant.copy(
                id = kotlin.uuid.Uuid.random(),
                name = "${assistant.name} (Clone)",
                avatar = if(assistant.avatar is Avatar.Image) Avatar.Dummy else assistant.avatar,
            )
            settingsStore.update(
                settings.copy(
                    assistants = settings.assistants.plus(copiedAssistant)
                )
            )
        }
    }

    fun getMemories(assistant: Assistant) =
        memoryRepository.getMemoriesOfAssistantFlow(assistant.id.toString())
}

private fun Assistant.collectMediaFileRefs(): List<String> {
    return buildList {
        (avatar as? Avatar.Image)?.url?.let(::add)
        background?.let(::add)
    }
}
