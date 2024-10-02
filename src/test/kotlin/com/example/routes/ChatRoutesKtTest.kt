package com.example.routes

import com.example.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.test.*

class ChatRoutesKtTest {

    @Test
    fun testPostChat() = testApplication {
        application {
            module()
        }

        val gameId = 1L
        val playerIds = setOf(101L, 102L)

        val requestBody = Json.encodeToString(JsonObject(mapOf(
            "gameId" to JsonPrimitive(gameId),
            "playerIds" to JsonArray(playerIds.map { JsonPrimitive(it) })
        )))

        val response = client.post("/chat") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        assertTrue(responseBody.containsKey("id"))
        assertEquals(gameId, responseBody["gameId"]?.jsonPrimitive?.long)

        val responsePlayerIds = responseBody["playerIds"]?.jsonArray?.map { it.jsonPrimitive.long }?.toSet()
        assertEquals(playerIds, responsePlayerIds)

        assertTrue(responseBody.containsKey("messages"))
        assertTrue(responseBody["messages"]?.jsonArray?.isEmpty() == true)
    }
}