package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var allConversations: List<ConversationItem> = emptyList()

    private val _uiState = MutableStateFlow(
        ConversationsUiState()
    )
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun refresh() {
        loadConversations(isRefresh = true)
    }

    fun loadConversations(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }
            val userUuid = userRepository.getCurrentUserUuid()
            val result = chatRepository.getUserConversations(userUuid)
            result.fold(
                onSuccess = { list ->
                    allConversations = list
                    _uiState.update { state ->
                        state.copy(
                            conversations = filterConversations(state.searchQuery, state.selectedFilter),
                            isRefreshing = false,
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
                }
            )
        }
    }

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
