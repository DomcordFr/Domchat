package me.lucid.domchat.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserPersona(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "User",
    val pronouns: String = "",
    val description: String = "",
    val avatar: Avatar = Avatar.Dummy,
    val isDefault: Boolean = false
)
