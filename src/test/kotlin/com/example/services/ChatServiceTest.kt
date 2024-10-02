package com.example.services

import com.example.models.Chats
import com.example.models.ChatParticipants
import com.example.models.Messages
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChatServiceTest {

    private lateinit var database: Database
    private lateinit var chatService: ChatService

    @Before
    fun setUp() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction(database) {
            SchemaUtils.create(Chats, ChatParticipants, Messages)
        }

        chatService = ChatService()
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Chats, ChatParticipants, Messages)
        }
    }

    @Test
    fun testCreateChat() = runBlocking {
        // given
        val gameId = 1L
        val playerIds = setOf(101L, 102L)

        // when
        val chatId = chatService.createChat(gameId, playerIds)

        // then
        assertNotNull(chatId)

        transaction {
            val savedChat = Chats.select { Chats.id eq chatId }.singleOrNull()
            assertNotNull(savedChat)
            assertEquals(gameId, savedChat[Chats.gameId])

            val participants = ChatParticipants.select { ChatParticipants.chatId eq chatId }.count()
            assertEquals(playerIds.size.toLong(), participants)
        }
    }

//    @Test
//    fun testGetChat() = runBlocking {
//        val gameId = 2L
//        val playerIds = setOf(201L, 202L)
//
//        val createdChat = chatService.createChat(gameId, playerIds)
//        val retrievedChat = chatService.getChat(createdChat.id)
//
//        assertNotNull(retrievedChat)
//        assertEquals(createdChat.id, retrievedChat.id)
//        assertEquals(gameId, retrievedChat.gameId)
//        assertEquals(playerIds, retrievedChat.playerIds)
//    }
//
//    @Test
//    fun testAddMessage() = runBlocking {
//        val gameId = 3L
//        val playerIds = setOf(301L, 302L)
//        val senderId = playerIds.first()
//
//        val chat = chatService.createChat(gameId, playerIds)
//        val message = chatService.addMessage(chat.id, senderId, "Test message")
//
//        assertNotNull(message)
//        assertEquals(senderId, message.senderId)
//        assertEquals("Test message", message.content)
//
//        transaction {
//            val savedMessage = Messages.select { Messages.id eq message.id }.singleOrNull()
//            assertNotNull(savedMessage)
//            assertEquals("Test message", savedMessage[Messages.content])
//        }
//    }
//
//    @Test
//    fun testCanAccessChat() = runBlocking {
//        val gameId = 4L
//        val playerIds = setOf(401L, 402L)
//        val nonParticipantId = 403L
//
//        val chatId = chatService.createChat(gameId, playerIds)
//
//        playerIds.forEach { playerId ->
//            assert(chatService.canAccessChat(chatId, playerId))
//        }
//
//        assert(!chatService.canAccessChat(chatId, nonParticipantId))
//    }
}