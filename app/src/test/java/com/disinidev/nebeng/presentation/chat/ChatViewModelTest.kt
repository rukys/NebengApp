package com.disinidev.nebeng.presentation.chat

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chatRepository = mockk<ChatRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val sampleMessages = listOf(
        ChatMessage(
            id = "msg_1",
            text = "Halo Mas Budi! Saya sudah jalan menuju titik jemput.",
            isFromMe = false,
            timestamp = "07:15"
        ),
        ChatMessage(
            id = "msg_2",
            text = "Siap Mas, saya tunggu di samping Lawson Barat ya.",
            isFromMe = true,
            timestamp = "07:16"
        ),
        ChatMessage(
            id = "msg_3",
            text = "Oke, sekitar 3 menit lagi sampai ya.",
            isFromMe = false,
            timestamp = "07:18"
        )
    )

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "user_123"
        coEvery { userRepository.getCurrentUserName() } returns "Budi Santoso"
        coEvery { chatRepository.observeMessages(any(), any()) } returns flowOf(sampleMessages)
        coEvery { chatRepository.sendMessage(any(), any(), any(), any()) } returns Result.success(mockk(relaxed = true))
    }

    @Test
    fun `initial state loads driver info and initial conversation messages`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "driverName" to "Andi Pratama",
                "vehicleInfo" to "Avanza Silver",
                "pin" to "489 201"
            )
        )
        val viewModel = ChatViewModel(savedStateHandle, chatRepository, userRepository)
        advanceUntilIdle()

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
        val viewModel = ChatViewModel(savedStateHandle, chatRepository, userRepository)

        viewModel.onInputChange("Sudah sampai ya mas")
        assertEquals("Sudah sampai ya mas", viewModel.uiState.value.inputMessage)
    }

    @Test
    fun `sendMessage invokes chatRepository and resets input`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "booking_123"))
        val viewModel = ChatViewModel(savedStateHandle, chatRepository, userRepository)

        viewModel.onInputChange("Saya pakai baju putih ya")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputMessage)
        coVerify {
            chatRepository.sendMessage(
                bookingId = "booking_123",
                senderId = "user_123",
                senderName = any(),
                text = "Saya pakai baju putih ya"
            )
        }
    }

    @Test
    fun `sendMessage with empty or blank text does not send message`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = ChatViewModel(savedStateHandle, chatRepository, userRepository)

        viewModel.onInputChange("   ")
        viewModel.sendMessage()
        advanceUntilIdle()

        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any(), any(), any()) }
    }
}
