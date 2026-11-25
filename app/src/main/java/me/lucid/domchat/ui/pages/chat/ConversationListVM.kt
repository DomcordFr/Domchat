package me.lucid.domchat.ui.pages.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.service.ChatService
import me.lucid.domchat.utils.toLocalString
import me.lucid.domchat.utils.UpdateChecker
import java.time.LocalDate
import java.time.ZoneId
import kotlin.uuid.Uuid

class ConversationListVM(
    private val settingsStore: SettingsStore,
    private val conversationRepo: ConversationRepository,
    private val chatService: ChatService,
    val updateChecker: UpdateChecker,
) : ViewModel() {

    // 搜索关键词
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 聊天列表 (使用 Paging 分页加载，展示所有角色的全部会话)
    @OptIn(FlowPreview::class)
    val conversations: Flow<PagingData<ConversationListItem>> =
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    conversationRepo.getAllConversationsPaging()
                } else {
                    conversationRepo.searchAllConversationsPaging(query)
                }
            }
            .map { pagingData ->
                pagingData
                    .map { ConversationListItem.Item(it) }
                    .insertSeparators { before, after ->
                        when {
                            before == null && after is ConversationListItem.Item -> {
                                if (after.conversation.isPinned) {
                                    ConversationListItem.PinnedHeader
                                } else {
                                    ConversationListItem.DateHeader(
                                        date = after.conversation.updateAt
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate(),
                                        label = dateLabel(after.conversation.updateAt),
                                    )
                                }
                            }

                            before is ConversationListItem.Item && after is ConversationListItem.Item -> {
                                if (before.conversation.isPinned && !after.conversation.isPinned) {
                                    ConversationListItem.DateHeader(
                                        date = after.conversation.updateAt
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate(),
                                        label = dateLabel(after.conversation.updateAt),
                                    )
                                } else if (!after.conversation.isPinned) {
                                    val beforeDate = before.conversation.updateAt
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    val afterDate = after.conversation.updateAt
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()

                                    if (beforeDate != afterDate) {
                                        ConversationListItem.DateHeader(
                                            date = afterDate,
                                            label = dateLabel(after.conversation.updateAt),
                                        )
                                    } else {
                                        null
                                    }
                                } else {
                                    null
                                }
                            }

                            else -> null
                        }
                    }
            }
            .catch { e ->
                e.printStackTrace()
                emit(PagingData.empty())
            }
            .cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val conversationJobs = chatService
        .getConversationJobs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val conversationJobsSet: StateFlow<Set<Uuid>> = conversationJobs.map { it.keys }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptySet(),
    )

    val completedGenerationIds: StateFlow<Set<Uuid>> = chatService
        .generationDoneFlow
        .scan(emptySet<Uuid>()) { acc, id -> acc + id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val recentlyRestoredIds: StateFlow<Set<Uuid>> = chatService.recentlyRestoredIds

    val errorFlow = chatService.errorFlow

    val assistants: StateFlow<List<Assistant>> = settingsStore.settingsFlow
        .map { it.assistants }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteConversation(conversation: Conversation) {
        chatService.deleteConversation(conversation)
    }

    fun undoDeleteConversation(conversationId: Uuid): Boolean {
        return chatService.undoDeleteConversation(conversationId)
    }

    fun updatePinnedStatus(conversation: Conversation) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepo.togglePinStatus(conversation.id)
        }
    }

    fun updateConversationTitle(conversation: Conversation, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepo.updateTitle(
                conversationId = conversation.id,
                title = title,
                updateAt = conversation.updateAt,
            )
        }
    }

    fun generateTitle(conversation: Conversation, force: Boolean = false) {
        viewModelScope.launch {
            val conversationFull = withContext(Dispatchers.IO) {
                conversationRepo.getConversationById(conversation.id)
            } ?: return@launch
            chatService.generateTitle(conversation.id, conversationFull, force)
        }
    }

    private fun dateLabel(updateAt: java.time.Instant): String {
        val date = updateAt.atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> date.toLocalString(true)
        }
    }
}
