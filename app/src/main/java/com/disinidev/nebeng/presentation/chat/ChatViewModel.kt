package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.SavedStateHandle
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
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val driverName: String = savedStateHandle.get<String>("driverName") ?: "Pengemudi"
    val vehicleInfo: String = savedStateHandle.get<String>("vehicleInfo") ?: "-"
    val pin: String = savedStateHandle.get<String>("pin") ?: ""
    val bookingId: String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _uiState = MutableStateFlow(
        ChatUiState(
            driverName = driverName,
            vehicleInfo = vehicleInfo,
            pin = pin,
            messages = emptyList()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            val myUuid = userRepository.getCurrentUserUuid()
            chatRepository.observeMessages(bookingId, myUuid).collect { list ->
                _uiState.update { it.copy(messages = list) }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputMessage = text) }
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputMessage.trim()
        if (currentInput.isEmpty()) return

        _uiState.update { it.copy(inputMessage = "") }

        viewModelScope.launch {
            val myUuid = userRepository.getCurrentUserUuid()
            val myName = userRepository.getCurrentUserName()
            chatRepository.sendMessage(
                bookingId = bookingId,
                senderId = myUuid,
                senderName = myName,
                text = currentInput
            )
        }
    }
}
