package me.lucid.domchat.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.lucid.domchat.data.db.entity.ConversationEntity
import me.lucid.domchat.data.repository.LightConversationEntity

@Dao
interface ConversationDAO {
    @Query("SELECT * FROM conversationentity ORDER BY is_pinned DESC, update_at DESC")
    fun getAll(): Flow<List<ConversationEntity>>

    @Query("SELECT id, assistant_id as assistantId, title, is_pinned as isPinned, create_at as createAt, update_at as updateAt, is_consolidated as isConsolidated, is_fork as isFork, assistant_message_count as assistantMessageCount FROM conversationentity ORDER BY is_pinned DESC, update_at DESC")
    fun getAllLight(): Flow<List<LightConversationEntity>>

    @Query("SELECT id, assistant_id as assistantId, title, is_pinned as isPinned, create_at as createAt, update_at as updateAt, is_consolidated as isConsolidated, is_fork as isFork, assistant_message_count as assistantMessageCount FROM conversationentity ORDER BY is_pinned DESC, update_at DESC")
    fun getAllConversationsPaging(): PagingSource<Int, LightConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE assistant_id = :assistantId ORDER BY is_pinned DESC, update_at DESC")
    fun getConversationsOfAssistant(assistantId: String): Flow<List<ConversationEntity>>

    @Query("SELECT id, assistant_id as assistantId, title, is_pinned as isPinned, create_at as createAt, update_at as updateAt, is_consolidated as isConsolidated, is_fork as isFork, assistant_message_count as assistantMessageCount FROM conversationentity WHERE assistant_id = :assistantId ORDER BY is_pinned DESC, update_at DESC")
    fun getConversationsOfAssistantPaging(assistantId: String): PagingSource<Int, LightConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE assistant_id = :assistantId ORDER BY is_pinned DESC, update_at DESC LIMIT :limit")
    suspend fun getRecentConversationsOfAssistant(assistantId: String, limit: Int): List<ConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE assistant_id = :assistantId AND is_consolidated = 0 ORDER BY update_at ASC LIMIT :limit")
    suspend fun getPendingMemoryConversations(assistantId: String, limit: Int): List<ConversationEntity>

    @Query(
        """
        SELECT conversationentity.* FROM conversationentity
        WHERE assistant_id = :assistantId
          AND is_consolidated = 1
          AND NOT EXISTS (
              SELECT 1 FROM ChatEpisodeEntity
              WHERE ChatEpisodeEntity.conversation_id = conversationentity.id
          )
        ORDER BY update_at DESC
        LIMIT :limit
        """
    )
    suspend fun getConsolidatedConversationsMissingEpisode(
        assistantId: String,
        limit: Int,
    ): List<ConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE title LIKE '%' || :searchText || '%' ORDER BY is_pinned DESC, update_at DESC")
    fun searchConversations(searchText: String): Flow<List<ConversationEntity>>

    @Query("SELECT id, assistant_id as assistantId, title, is_pinned as isPinned, create_at as createAt, update_at as updateAt, is_consolidated as isConsolidated, is_fork as isFork, assistant_message_count as assistantMessageCount FROM conversationentity WHERE title LIKE '%' || :searchText || '%' ORDER BY is_pinned DESC, update_at DESC")
    fun searchConversationsPaging(searchText: String): PagingSource<Int, LightConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE assistant_id = :assistantId AND title LIKE '%' || :searchText || '%' ORDER BY is_pinned DESC, update_at DESC")
    fun searchConversationsOfAssistant(assistantId: String, searchText: String): Flow<List<ConversationEntity>>

    @Query("SELECT id, assistant_id as assistantId, title, is_pinned as isPinned, create_at as createAt, update_at as updateAt, is_consolidated as isConsolidated, is_fork as isFork, assistant_message_count as assistantMessageCount FROM conversationentity WHERE assistant_id = :assistantId AND title LIKE '%' || :searchText || '%' ORDER BY is_pinned DESC, update_at DESC")
    fun searchConversationsOfAssistantPaging(assistantId: String, searchText: String): PagingSource<Int, LightConversationEntity>

    @Query("SELECT * FROM conversationentity WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM conversationentity WHERE id = :id)")
    suspend fun conversationExists(id: String): Boolean

    @Insert
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Delete
    suspend fun delete(conversation: ConversationEntity)

    @Query("UPDATE conversationentity SET is_pinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: String, isPinned: Boolean)

    @Query("UPDATE conversationentity SET is_consolidated = :isConsolidated WHERE id = :id")
    suspend fun updateConsolidatedStatus(id: String, isConsolidated: Boolean)

    @Query("UPDATE conversationentity SET title = :title, update_at = :updateAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updateAt: Long)

    @Query("UPDATE conversationentity SET assistant_message_count = :count WHERE id = :id")
    suspend fun updateAssistantMessageCount(id: String, count: Int)

    @Query("SELECT id, nodes, assistant_message_count as assistantMessageCount FROM conversationentity")
    suspend fun getAllNodesForCountBackfill(): List<ConversationCountRow>

    // Stats queries for MenuVM optimization
    @Query("SELECT COUNT(*) FROM conversationentity")
    fun getConversationCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM conversationentity WHERE assistant_id = :assistantId")
    suspend fun getConversationCountOfAssistant(assistantId: String): Int

    @Query("SELECT DISTINCT assistant_id FROM conversationentity")
    suspend fun getDistinctAssistantIds(): List<String>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM conversationentity
            WHERE assistant_message_count > 0
            LIMIT 1
        )
        """
    )
    suspend fun hasUserAssistantConversation(): Boolean

    // Batch query for backfill tasks to prevent OOM
    @Query("SELECT * FROM conversationentity ORDER BY update_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getBackfillDataBatch(limit: Int, offset: Int): List<ConversationEntity>

    @Query("SELECT assistant_id as assistantId, MAX(update_at) as lastUpdateAt FROM conversationentity GROUP BY assistant_id")
    suspend fun getAllAssistantsLastUpdateAt(): List<AssistantLastUpdate>
}

data class AssistantLastUpdate(
    val assistantId: String,
    val lastUpdateAt: Long?,
)

data class ConversationCountRow(
    val id: String,
    val nodes: String,
    val assistantMessageCount: Int,
)
