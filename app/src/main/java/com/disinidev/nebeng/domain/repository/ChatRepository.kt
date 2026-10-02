package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.presentation.chat.ChatMessage
import com.disinidev.nebeng.presentation.chat.ConversationItem
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getMessages(bookingId: String, currentUserId: String): Result<List<ChatMessage>>
    fun observeMessages(bookingId: String, currentUserId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(
        bookingId: String,
        senderId: String,
        senderName: String,
        text: String
    ): Result<ChatMessage>
    suspend fun getUserConversations(userUuid: String): Result<List<ConversationItem>>
    fun setActiveChat(bookingId: String?)
    suspend fun unsubscribeChat(bookingId: String)
}
