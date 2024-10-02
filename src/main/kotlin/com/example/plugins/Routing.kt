package com.example.plugins

import com.example.routes.chatRoutes
import com.example.routes.chatWebSocket
import com.example.services.ChatRoomManager
import com.example.services.ChatService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val chatService = ChatService()
    val chatRoomManager = ChatRoomManager()
    routing {
        chatRoutes(chatService)
        chatWebSocket(chatRoomManager, chatService)
    }
}
