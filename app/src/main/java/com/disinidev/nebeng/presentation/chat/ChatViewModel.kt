package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val driverName: String = savedStateHandle.get<String>("driverName") ?: "Andi Pratama"
    private val vehicleInfo: String = savedStateHandle.get<String>("vehicleInfo") ?: "Avanza Silver"
    private val pin: String = savedStateHandle.get<String>("pin") ?: "489 201"

    private val _uiState = MutableStateFlow(
        ChatUiState(
            driverName = driverName,
            vehicleInfo = vehicleInfo,
            pin = pin,
            messages = listOf(
                ChatMessage(
                    id = "1",
                    text = "Halo Mas Budi! Saya sudah jalan menuju titik jemput.",
                    isFromMe = false
                ),
                ChatMessage(
                    id = "2",
                    text = "Siap Mas, saya tunggu di samping Lawson Barat ya.",
                    isFromMe = true
                ),
                ChatMessage(
                    id = "3",
                    text = "Oke, posisi sekitar 3 menit lagi sampai.",
                    isFromMe = false
                )
            )
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputMessage = text) }
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputMessage.trim()
        if (currentInput.isEmpty()) return

        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = currentInput,
            isFromMe = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + newMessage,
                inputMessage = ""
            )
        }
    }
}
