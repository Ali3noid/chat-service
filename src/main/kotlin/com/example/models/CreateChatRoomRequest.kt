package com.example.models

import com.example.plugins.UUIDSerializer
import com.example.plugins.UUIDSetSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateChatRoomRequest(
    @Serializable(with = UUIDSerializer::class)
    val gameId: UUID,

//    @Serializable(with = UUIDSetSerializer::class)
    val playerIds: Set<@Serializable(with = UUIDSerializer::class) UUID>
)
