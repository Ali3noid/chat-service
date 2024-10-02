package com.example.services

import io.ktor.websocket.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatRoomManager {
    private val chatRooms = ConcurrentHashMap<UUID, MutableSet<ChatConnection>>()

    fun joinRoom(chatRoomId: UUID, playerId: UUID, session: WebSocketSession) {
        val connection = ChatConnection(playerId, session)
        chatRooms.getOrPut(chatRoomId) { Collections.synchronizedSet(mutableSetOf()) }.add(connection)
    }

    fun leaveRoom(chatRoomId: UUID, playerId: UUID) {
        chatRooms[chatRoomId]?.removeIf { it.playerId == playerId }
    }

    suspend fun broadcast(chatRoomId: UUID, senderId: UUID, message: String) {
        chatRooms[chatRoomId]?.forEach { connection ->
            if (connection.playerId != senderId) {
                connection.session.send(Frame.Text(message))
            }
        }
    }
}

data class ChatConnection(val playerId: UUID, val session: WebSocketSession)