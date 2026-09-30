package com.disinidev.nebeng.presentation.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String = ""
)

data class ChatUiState(
    val driverName: String = "Pengemudi",
    val vehicleInfo: String = "-",
    val pin: String = "",
    val inputMessage: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isTripCompleted: Boolean = false
)
