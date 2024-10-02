package com.example.services

import com.example.models.*
import com.example.plugins.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ChatService {
    suspend fun createChatRoom(gameId: UUID, playerIds: Set<UUID>): ChatRoom = dbQuery {
        ChatRoom(gameId = gameId, playerIds = playerIds).also {
            insertChatRoom(it)
            insertPlayers(it.id, playerIds)
        }
    }

    private fun insertChatRoom(chatRoom: ChatRoom) {
        ChatRooms.insert {
            it[id] = chatRoom.id
            it[gameId] = chatRoom.gameId
            it[createdAt] = chatRoom.createdAt
            it[updatedAt] = chatRoom.updatedAt
            it[status] = chatRoom.status.name
        }
    }

    private fun insertPlayers(chatRoomId: UUID, playerIds: Set<UUID>) {
        playerIds.forEach { playerId ->
            ChatRoomPlayers.insert {
                it[ChatRoomPlayers.chatRoomId] = chatRoomId
                it[ChatRoomPlayers.playerId] = playerId
            }
        }
    }

    suspend fun sendMessage(chatRoomId: UUID, senderId: UUID, content: String): Message = dbQuery {
        if (!validateAccess(chatRoomId, senderId)) {
            throw IllegalAccessException("Player does not have access to this chat room")
        }
        Message(chatRoomId = chatRoomId, senderId = senderId, content = content).also {
            insertMessage(it)
        }
    }

    fun insertMessage(message: Message) {
        Messages.insert {
            it[id] = message.id
            it[chatRoomId] = message.chatRoomId
            it[senderId] = message.senderId
            it[content] = message.content
            it[timestamp] = message.timestamp
        }
    }

    suspend fun getChatRoomMessages(chatRoomId: UUID, playerId: UUID): List<Message> = dbQuery {
        if (!validateAccess(chatRoomId, playerId)) {
            throw IllegalAccessException("Player does not have access to this chat room")
        }

        Messages
            .select { Messages.chatRoomId eq chatRoomId }
            .map { row ->
                Message(
                    id = row[Messages.id],
                    chatRoomId = row[Messages.chatRoomId],
                    senderId = row[Messages.senderId],
                    content = row[Messages.content],
                    timestamp = row[Messages.timestamp]
                )
            }
    }

    suspend fun archiveChatRoom(gameId: UUID) = dbQuery {
        ChatRooms.update({ ChatRooms.gameId eq gameId }) {
            it[status] = Status.ARCHIVED.name
            it[updatedAt] = System.currentTimeMillis()
        }
    }

    private suspend fun validateAccess(chatRoomId: UUID, playerId: UUID): Boolean = dbQuery {
        ChatRoomPlayers.select {
            (ChatRoomPlayers.chatRoomId eq chatRoomId) and (ChatRoomPlayers.playerId eq playerId)
        }.count() > 0
    }

    suspend fun addPlayerToChatRoom(chatRoomId: UUID, playerId: UUID) = dbQuery {
        ChatRoomPlayers.insert {
            it[ChatRoomPlayers.chatRoomId] = chatRoomId
            it[ChatRoomPlayers.playerId] = playerId
        }
        ChatRooms.update({ ChatRooms.id eq chatRoomId }) {
            it[updatedAt] = System.currentTimeMillis()
        }
    }

    suspend fun removePlayerFromChatRoom(chatRoomId: UUID, playerId: UUID) = dbQuery {
        ChatRoomPlayers.deleteWhere {
            (ChatRoomPlayers.chatRoomId eq chatRoomId) and (ChatRoomPlayers.playerId eq playerId)
        }
        ChatRooms.update({ ChatRooms.id eq chatRoomId }) {
            it[updatedAt] = System.currentTimeMillis()
        }
    }
}

