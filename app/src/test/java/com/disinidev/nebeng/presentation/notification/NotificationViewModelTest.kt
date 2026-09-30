package com.disinidev.nebeng.presentation.notification

import com.disinidev.nebeng.domain.model.Notification
import com.disinidev.nebeng.domain.model.NotificationCategory
import com.disinidev.nebeng.domain.repository.NotificationRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val notificationRepository = mockk<NotificationRepository>()
    private lateinit var viewModel: NotificationViewModel

    private val testNotifications = listOf(
        Notification(
            id = "notif_1",
            userId = "user_1",
            category = NotificationCategory.TRIP,
            title = "Driver On The Way",
            body = "Driver is coming",
            actionUrl = "tracking/123",
            isRead = false,
            createdAt = Instant.now()
        ),
        Notification(
            id = "notif_2",
            userId = "user_1",
            category = NotificationCategory.TRIP,
            title = "Trip Completed",
            body = "Rate your driver",
            actionUrl = null,
            isRead = true,
            createdAt = Instant.now()
        ),
        Notification(
            id = "notif_3",
            userId = "user_1",
            category = NotificationCategory.SYSTEM,
            title = "Account Verified",
            body = "Your profile is verified",
            actionUrl = null,
            isRead = true,
            createdAt = Instant.now()
        )
    )

    @Before
    fun setUp() {
        coEvery { notificationRepository.getNotifications() } returns Result.success(testNotifications)
        coEvery { notificationRepository.markAsRead(any()) } returns Result.success(Unit)
        coEvery { notificationRepository.markAllAsRead() } returns Result.success(Unit)
        viewModel = NotificationViewModel(notificationRepository)
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
        coVerify { notificationRepository.markAsRead(unreadNotif.id) }
    }

    @Test
    fun `markAllAsRead marks all notifications as read`() = runTest {
        advanceUntilIdle()
        viewModel.markAllAsRead()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notifications.all { it.isRead })
        coVerify { notificationRepository.markAllAsRead() }
    }
}
