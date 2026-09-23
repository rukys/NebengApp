package com.disinidev.nebeng.presentation.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String = ""
)

data class ChatUiState(
    val driverName: String = "Andi Pratama",
    val vehicleInfo: String = "Avanza Silver",
    val pin: String = "489 201",
    val inputMessage: String = "",
    val messages: List<ChatMessage> = emptyList()
)
