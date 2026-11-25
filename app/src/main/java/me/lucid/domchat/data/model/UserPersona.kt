package me.lucid.domchat.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserPersona(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "User",
    val pronouns: String = "",
    val description: String = "",
    val avatar: Avatar = Avatar.Dummy,
    @SerialName("enabled_lorebook_ids")
    val enabledLorebookIds: Set<String> = emptySet(),
    val isDefault: Boolean = false
)
