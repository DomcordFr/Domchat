package me.lucid.domchat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.lucid.domchat.data.db.entity.ConversationAttachmentRefEntity

@Dao
interface ConversationAttachmentRefDao {
    @Query("SELECT * FROM conversation_attachment_ref")
    fun observeAll(): Flow<List<ConversationAttachmentRefEntity>>

    @Query("SELECT * FROM conversation_attachment_ref")
    suspend fun getAll(): List<ConversationAttachmentRefEntity>

    @Query("SELECT conversation_id FROM conversation_attachment_ref WHERE attachment_id = :attachmentId")
    suspend fun getConversationIdsForAttachment(attachmentId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ConversationAttachmentRefEntity>)

    @Query("DELETE FROM conversation_attachment_ref WHERE conversation_id = :conversationId")
    suspend fun deleteConversationRefs(conversationId: String)

    @Query("DELETE FROM conversation_attachment_ref WHERE attachment_id = :attachmentId")
    suspend fun deleteAttachmentRefs(attachmentId: String)
}
