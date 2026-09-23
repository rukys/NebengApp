package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor() : ViewModel() {

    private val allConversations = listOf(
        ConversationItem(
            id = "conv_1",
            title = "Andi Pratama",
            subtitle = "Oke, posisi sekitar 3 mnt lagi sampai.",
            timestamp = "07:22",
            avatarInitials = "AP",
            isGroup = false,
            isActiveRide = true,
            driverName = "Andi Pratama",
            vehicleInfo = "Toyota Avanza Silver • B 1234 ABC",
            pin = "489 201"
        ),
        ConversationItem(
            id = "conv_2",
            title = "Sarah M. (Vario Hitam)",
            subtitle = "Sama-sama mas, hati-hati di kantor!",
            timestamp = "Kemarin",
            avatarInitials = "SM",
            isGroup = false,
            isActiveRide = false,
            driverName = "Sarah M.",
            vehicleInfo = "Honda Vario Hitam • B 6789 DEF",
            pin = "332 109"
        ),
        ConversationItem(
            id = "conv_3",
            title = "Komuter Tebet - Sudirman",
            subtitle = "Dimas: Ada yang jalan jam 08:00?",
            timestamp = "28 Agu",
            avatarInitials = "",
            isGroup = true,
            isActiveRide = false,
            driverName = "Komuter Tebet - Sudirman",
            vehicleInfo = "Grup Komuter Tebet",
            pin = "-"
        ),
        ConversationItem(
            id = "conv_4",
            title = "Reza Hendra",
            subtitle = "Siap mas, helm sudah saya sediakan.",
            timestamp = "25 Agu",
            avatarInitials = "RH",
            isGroup = false,
            isActiveRide = false,
            driverName = "Reza Hendra",
            vehicleInfo = "Yamaha NMAX Hitam • B 5678 XYZ",
            pin = "215 889"
        )
    )

    private val _uiState = MutableStateFlow(
        ConversationsUiState(
            conversations = allConversations
        )
    )
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                conversations = filterConversations(query, state.selectedFilter)
            )
        }
    }

    fun onFilterSelected(filter: ConversationFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                conversations = filterConversations(state.searchQuery, filter)
            )
        }
    }

    private fun filterConversations(
        query: String,
        filter: ConversationFilter
    ): List<ConversationItem> {
        return allConversations.filter { item ->
            val matchesFilter = when (filter) {
                ConversationFilter.ALL -> true
                ConversationFilter.ACTIVE -> item.isActiveRide
                ConversationFilter.GROUP -> item.isGroup
            }
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.subtitle.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }
}
