package com.example.routes

import com.example.models.CreateChatRoomRequest
import com.example.models.Message
import com.example.models.SendMessageRequest
import com.example.plugins.dbQuery
import com.example.services.ChatRoomManager
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import com.example.services.ChatService
import io.ktor.http.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.util.UUID

fun Route.chatRoutes(chatService: ChatService) {

    post("/rooms") {
        val request = call.receive<CreateChatRoomRequest>()
        val chatRoom = chatService.createChatRoom(request.gameId, request.playerIds)
        call.respond(HttpStatusCode.Created, chatRoom)
    }

    post("/rooms/{chatRoomId}/messages") {
        val chatRoomId = UUID.fromString(call.parameters["chatRoomId"])
        val request = call.receive<SendMessageRequest>()
        val message = chatService.sendMessage(chatRoomId, request.senderId, request.content)
        call.respond(HttpStatusCode.Created, message)
    }

    get("/rooms/{chatRoomId}/messages") {
        val chatRoomId = UUID.fromString(call.parameters["chatRoomId"])
        val playerId = UUID.fromString(call.request.queryParameters["playerId"])
        val messages = chatService.getChatRoomMessages(chatRoomId, playerId)
        call.respond(messages)
    }
}

fun Route.chatWebSocket(chatRoomManager: ChatRoomManager, chatService: ChatService) {
    webSocket("/chat/{chatRoomId}") {
        val chatRoomId = UUID.fromString(call.parameters["chatRoomId"])
        val playerId = UUID.fromString(call.request.queryParameters["playerId"])

        try {
            chatRoomManager.joinRoom(chatRoomId, playerId, this)

            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    saveMessage(chatService, chatRoomId, playerId, text)
                    chatRoomManager.broadcast(chatRoomId, playerId, text)
                }
            }
        } finally {
            chatRoomManager.leaveRoom(chatRoomId, playerId)
        }
    }
}

private suspend fun saveMessage(
    chatService: ChatService,
    chatRoomId: UUID,
    playerId: UUID,
    text: String
) = dbQuery { // TODO czy to tak sie powinno robic? Patrzę i mnie wzdryga :(
    chatService.insertMessage(
        Message(
            chatRoomId = chatRoomId,
            senderId = playerId,
            content = text,
        )
    )
}