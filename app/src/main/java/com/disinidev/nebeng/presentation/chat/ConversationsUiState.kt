package com.disinidev.nebeng.presentation.chat

enum class ConversationFilter(val label: String) {
    ALL("Semua (4)"),
    ACTIVE("Tebengan Aktif"),
    GROUP("Grup Rute")
}

data class ConversationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val avatarInitials: String = "",
    val isGroup: Boolean = false,
    val isActiveRide: Boolean = false,
    val driverName: String = "",
    val vehicleInfo: String = "",
    val pin: String = ""
)

data class ConversationsUiState(
    val searchQuery: String = "",
    val selectedFilter: ConversationFilter = ConversationFilter.ALL,
    val conversations: List<ConversationItem> = emptyList()
)
