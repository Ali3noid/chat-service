package com.example.models

import com.example.plugins.UUIDSerializer
import com.example.plugins.UUIDSetSerializer
import kotlinx.serialization.Serializable
import java.util.*
import java.util.UUID.randomUUID

@Serializable
data class ChatRoom(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val gameId: UUID,  // Changed from Long to UUID for consistency
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
//    @Serializable(with = UUIDSetSerializer::class)
    val playerIds: Set<@Serializable(with = UUIDSerializer::class) UUID>,  // Added to track players in the chat
    val status: Status = Status.ACTIVE
)

@Serializable
enum class Status {
    ACTIVE, ARCHIVED
}

@Serializable
data class Message(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val chatRoomId: UUID,  // Changed from chatRoom to chatRoomId for clarity
    @Serializable(with = UUIDSerializer::class)
    val senderId: UUID,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// New data class to represent a player
data class Player(
    val id: UUID,
    val username: String,
    // Add other relevant player information
)