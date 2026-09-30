package com.disinidev.nebeng.presentation.chat

import com.disinidev.nebeng.domain.repository.ChatRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chatRepository = mockk<ChatRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val sampleConversations = listOf(
        ConversationItem(
            id = "c_1",
            title = "Andi Pratama",
            subtitle = "Avanza Silver • Menuju lokasi",
            timestamp = "07:18",
            avatarInitials = "AP",
            isActiveRide = true,
            isGroup = false,
            driverName = "Andi Pratama",
            vehicleInfo = "Avanza Silver",
            pin = "489 201"
        ),
        ConversationItem(
            id = "c_2",
            title = "Sarah M. (Vario Hitam)",
            subtitle = "Perjalanan Kemarin",
            timestamp = "Kemarin",
            avatarInitials = "SM",
            isActiveRide = false,
            isGroup = false
        ),
        ConversationItem(
            id = "c_3",
            title = "Komuter Tebet - Sudirman",
            subtitle = "Rute Harian • 8 Anggota",
            timestamp = "06:45",
            avatarInitials = "KT",
            isActiveRide = false,
            isGroup = true
        ),
        ConversationItem(
            id = "c_4",
            title = "Reza Hendra",
            subtitle = "NMAX Merah • Tebet Barat",
            timestamp = "22 Sep",
            avatarInitials = "RH",
            isActiveRide = false,
            isGroup = false
        )
    )

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "user_123"
        coEvery { chatRepository.getUserConversations("user_123") } returns Result.success(sampleConversations)
    }

    @Test
    fun `initial state loads all conversations and default ALL filter`() = runTest {
        val viewModel = ConversationsViewModel(chatRepository, userRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.ALL, state.selectedFilter)
        assertEquals(4, state.conversations.size)
        assertEquals("Andi Pratama", state.conversations[0].title)
        assertTrue(state.conversations[0].isActiveRide)
    }

    @Test
    fun `filtering by ACTIVE shows only active ride conversations`() = runTest {
        val viewModel = ConversationsViewModel(chatRepository, userRepository)
        advanceUntilIdle()

        viewModel.onFilterSelected(ConversationFilter.ACTIVE)

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.ACTIVE, state.selectedFilter)
        assertEquals(1, state.conversations.size)
        assertEquals("Andi Pratama", state.conversations[0].title)
    }

    @Test
    fun `filtering by GROUP shows only group route conversations`() = runTest {
        val viewModel = ConversationsViewModel(chatRepository, userRepository)
        advanceUntilIdle()

        viewModel.onFilterSelected(ConversationFilter.GROUP)

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.GROUP, state.selectedFilter)
        assertEquals(1, state.conversations.size)
        assertEquals("Komuter Tebet - Sudirman", state.conversations[0].title)
        assertTrue(state.conversations[0].isGroup)
    }

    @Test
    fun `search query filters conversations by title or subtitle`() = runTest {
        val viewModel = ConversationsViewModel(chatRepository, userRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Sarah")

        val state = viewModel.uiState.value
        assertEquals(1, state.conversations.size)
        assertEquals("Sarah M. (Vario Hitam)", state.conversations[0].title)
    }
}
