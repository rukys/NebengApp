package com.disinidev.nebeng.presentation.notification

import com.disinidev.nebeng.domain.model.NotificationCategory
import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setUp() {
        viewModel = NotificationViewModel(firebaseAuth, supabaseClient)
    }

    @Test
    fun `initial state loads notifications and calculates counts`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(NotificationFilter.ALL, state.selectedFilter)
        assertEquals(3, state.totalCount)
        assertEquals(2, state.tripCount)
        assertEquals(1, state.systemCount)
        assertEquals(3, state.notifications.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onSelectFilter with TRIP filters only trip notifications`() = runTest {
        advanceUntilIdle()
        viewModel.onSelectFilter(NotificationFilter.TRIP)

        val state = viewModel.uiState.value
        assertEquals(NotificationFilter.TRIP, state.selectedFilter)
        assertEquals(2, state.notifications.size)
        assertTrue(state.notifications.all { it.category == NotificationCategory.TRIP })
    }

    @Test
    fun `onSelectFilter with SYSTEM filters only system notifications`() = runTest {
        advanceUntilIdle()
        viewModel.onSelectFilter(NotificationFilter.SYSTEM)

        val state = viewModel.uiState.value
        assertEquals(NotificationFilter.SYSTEM, state.selectedFilter)
        assertEquals(1, state.notifications.size)
        assertTrue(state.notifications.all { it.category == NotificationCategory.SYSTEM })
    }

    @Test
    fun `markAsRead updates target notification isRead to true`() = runTest {
        advanceUntilIdle()
        val unreadNotif = viewModel.uiState.value.notifications.first { !it.isRead }

        viewModel.markAsRead(unreadNotif.id)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.notifications.first { it.id == unreadNotif.id }
        assertTrue(updated.isRead)
    }

    @Test
    fun `markAllAsRead marks all notifications as read`() = runTest {
        advanceUntilIdle()
        viewModel.markAllAsRead()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notifications.all { it.isRead })
    }
}
