package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state loads driver info and initial conversation messages`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "driverName" to "Andi Pratama",
                "vehicleInfo" to "Avanza Silver",
                "pin" to "489 201"
            )
        )
        val viewModel = ChatViewModel(savedStateHandle)

        val state = viewModel.uiState.value
        assertEquals("Andi Pratama", state.driverName)
        assertEquals("Avanza Silver", state.vehicleInfo)
        assertEquals("489 201", state.pin)
        assertEquals(3, state.messages.size)
        assertEquals("Halo Mas Budi! Saya sudah jalan menuju titik jemput.", state.messages[0].text)
        assertEquals(false, state.messages[0].isFromMe)
        assertEquals("Siap Mas, saya tunggu di samping Lawson Barat ya.", state.messages[1].text)
        assertEquals(true, state.messages[1].isFromMe)
    }

    @Test
    fun `onInputChange updates inputMessage in state`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = ChatViewModel(savedStateHandle)

        viewModel.onInputChange("Sudah sampai ya mas")
        assertEquals("Sudah sampai ya mas", viewModel.uiState.value.inputMessage)
    }

    @Test
    fun `sendMessage appends new message and resets input`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = ChatViewModel(savedStateHandle)

        val initialCount = viewModel.uiState.value.messages.size

        viewModel.onInputChange("Saya pakai baju putih ya")
        viewModel.sendMessage()

        val updatedState = viewModel.uiState.value
        assertEquals(initialCount + 1, updatedState.messages.size)
        assertEquals("", updatedState.inputMessage)
        val lastMessage = updatedState.messages.last()
        assertEquals("Saya pakai baju putih ya", lastMessage.text)
        assertTrue(lastMessage.isFromMe)
    }

    @Test
    fun `sendMessage with empty or blank text does not add message`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = ChatViewModel(savedStateHandle)

        val initialCount = viewModel.uiState.value.messages.size

        viewModel.onInputChange("   ")
        viewModel.sendMessage()

        assertEquals(initialCount, viewModel.uiState.value.messages.size)
    }
}
