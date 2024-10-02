package com.example.models

import org.jetbrains.exposed.sql.Table

object ChatRooms : Table() {
    val id = uuid("id")
    val gameId = uuid("game_id")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    val status = varchar("status", 20)
    override val primaryKey = PrimaryKey(id)
}

object ChatRoomPlayers : Table() {
    val chatRoomId = uuid("chat_room_id").references(ChatRooms.id)
    val playerId = uuid("player_id")
    override val primaryKey = PrimaryKey(chatRoomId, playerId)
}

object Messages : Table() {
    val id = uuid("id")
    val chatRoomId = uuid("chat_room_id").references(ChatRooms.id)
    val senderId = uuid("sender_id")
    val content = text("content")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}