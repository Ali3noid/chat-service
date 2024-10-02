package com.example.models

import java.util.*

data class SendMessageRequest(
    val senderId: UUID,
    val content: String
)