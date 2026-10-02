package com.disinidev.nebeng.presentation.activity

import com.disinidev.nebeng.domain.repository.BookingActivityItem
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.UserActivities
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bookingRepository = mockk<BookingRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "user_123"
        coEvery { bookingRepository.getPendingRequests(any()) } returns Result.success(emptyList())
        coEvery { bookingRepository.getUserActivities(any()) } returns Result.success(
            UserActivities(
                activeTrip = BookingActivityItem(
                    id = "act-1",
                    origin = "Lawson Tebet Barat",
                    destination = "SCBD Pacific Place",
                    timeText = "Hari ini, 07:30",
                    vehicleType = "Mobil",
                    counterpartName = "Andi Pratama",
                    status = "DIPROSES",
                    pin = "489 201",
                    vehicleModel = "Toyota Avanza",
                    licensePlate = "B 1234 ABC"
                ),
                completedTrips = listOf(
                    BookingActivityItem(
                        id = "act-2",
                        origin = "Pancoran",
                        destination = "Kuningan",
                        timeText = "Kemarin, 08:30",
                        vehicleType = "Mobil",
                        counterpartName = "Budi Hartono",
                        status = "SELESAI",
                        vehicleModel = "Daihatsu Xenia",
                        licensePlate = "B 5678 DEF"
                    )
                ),
                canceledTrips = emptyList()
            )
        )
        viewModel = ActivityViewModel(bookingRepository, userRepository)
    }

    @Test
    fun `initial state contains ongoing filter, active trip, and completed history`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(ActivityFilter.ONGOING, state.selectedFilter)
        assertNotNull(state.activeTrip)
        assertEquals("489 201", state.activeTrip?.pin)
        assertEquals("Andi Pratama", state.activeTrip?.driverName)
        assertTrue(state.completedTrips.isNotEmpty())
        assertEquals("Pancoran", state.completedTrips.first().origin)
        assertEquals("Kuningan", state.completedTrips.first().destination)
        assertEquals("Kemarin, 08:30", state.completedTrips.first().timeText)
        assertEquals("Mobil", state.completedTrips.first().vehicleType)
    }

    @Test
    fun `onFilterSelected updates selectedFilter`() {
        viewModel.onFilterSelected(ActivityFilter.COMPLETED)
        assertEquals(ActivityFilter.COMPLETED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(ActivityFilter.CANCELED)
        assertEquals(ActivityFilter.CANCELED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(ActivityFilter.ONGOING)
        assertEquals(ActivityFilter.ONGOING, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `onSearchQueryChange updates search query in state`() {
        viewModel.onSearchQueryChange("Pancoran")
        assertEquals("Pancoran", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `toggleSearch toggles search state and resets query when deactivated`() {
        viewModel.toggleSearch(true)
        assertTrue(viewModel.uiState.value.isSearchActive)

        viewModel.onSearchQueryChange("Tebet")
        assertEquals("Tebet", viewModel.uiState.value.searchQuery)

        viewModel.toggleSearch(false)
        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `cancelActiveTrip success updates state with success message and refreshes`() = runTest {
        coEvery { bookingRepository.cancelBooking("act-1") } returns Result.success(Unit)

        viewModel.cancelActiveTrip("act-1")
        advanceUntilIdle()

        coVerify { bookingRepository.cancelBooking("act-1") }
        val state = viewModel.uiState.value
        assertEquals("Tebengan berhasil dibatalkan.", state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isCancelling)
    }

    @Test
    fun `cancelActiveTrip failure updates state with error message`() = runTest {
        coEvery { bookingRepository.cancelBooking("act-1") } returns Result.failure(RuntimeException("Network error"))

        viewModel.cancelActiveTrip("act-1")
        advanceUntilIdle()

        coVerify { bookingRepository.cancelBooking("act-1") }
        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Gagal membatalkan tebengan"))
        assertNull(state.successMessage)
        assertFalse(state.isCancelling)
    }
}
