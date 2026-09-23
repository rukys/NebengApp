package com.disinidev.nebeng.presentation.chat

import com.disinidev.nebeng.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state loads all conversations and default ALL filter`() {
        val viewModel = ConversationsViewModel()

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.ALL, state.selectedFilter)
        assertEquals(4, state.conversations.size)
        assertEquals("Andi Pratama", state.conversations[0].title)
        assertTrue(state.conversations[0].isActiveRide)
    }

    @Test
    fun `filtering by ACTIVE shows only active ride conversations`() {
        val viewModel = ConversationsViewModel()

        viewModel.onFilterSelected(ConversationFilter.ACTIVE)

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.ACTIVE, state.selectedFilter)
        assertEquals(1, state.conversations.size)
        assertEquals("Andi Pratama", state.conversations[0].title)
    }

    @Test
    fun `filtering by GROUP shows only group route conversations`() {
        val viewModel = ConversationsViewModel()

        viewModel.onFilterSelected(ConversationFilter.GROUP)

        val state = viewModel.uiState.value
        assertEquals(ConversationFilter.GROUP, state.selectedFilter)
        assertEquals(1, state.conversations.size)
        assertEquals("Komuter Tebet - Sudirman", state.conversations[0].title)
        assertTrue(state.conversations[0].isGroup)
    }

    @Test
    fun `search query filters conversations by title or subtitle`() {
        val viewModel = ConversationsViewModel()

        viewModel.onSearchQueryChange("Sarah")

        val state = viewModel.uiState.value
        assertEquals(1, state.conversations.size)
        assertEquals("Sarah M. (Vario Hitam)", state.conversations[0].title)

        viewModel.onSearchQueryChange("Lawson")
        // Not matching
        assertTrue(viewModel.uiState.value.conversations.isEmpty())
    }
}
