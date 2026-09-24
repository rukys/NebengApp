package com.disinidev.nebeng.presentation.driver.requests

import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import com.disinidev.nebeng.domain.usecase.UpdateDriverLocationUseCase
import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverRequestsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bookingRepository = mockk<BookingRepository>()
    private val updateDriverLocationUseCase = mockk<UpdateDriverLocationUseCase>()
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firebaseUser = mockk<FirebaseUser>()
    private val locationClient = mockk<LocationClient>(relaxed = true)

    private val sampleRequest = DriverBookingRequest(
        bookingId = "req-1",
        rideId = "ride-1",
        passengerId = "p-1",
        passengerName = "Budi Santoso",
        pickupAddress = "Lawson Tebet",
        dropoffAddress = "SCBD Lot 8",
        seatPosition = "Depan Kiri",
        pickupPin = "123 456"
    )

    @Before
    fun setUp() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "driver-123"
        coEvery { bookingRepository.getPendingRequests("driver-123") } returns Result.success(listOf(sampleRequest))
        coEvery { bookingRepository.respondBookingRequest(any(), any()) } returns Result.success(Unit)
        coEvery { updateDriverLocationUseCase(any(), any(), any()) } returns Result.success(Unit)
    }

    @Test
    fun `loadRequests populates requests list`() = runTest {
        val viewModel = DriverRequestsViewModel(bookingRepository, updateDriverLocationUseCase, firebaseAuth, locationClient)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.requests.size)
        assertEquals("Budi Santoso", state.requests.first().passengerName)
    }

    @Test
    fun `acceptRequest calls repository and initializes driver location`() = runTest {
        val viewModel = DriverRequestsViewModel(bookingRepository, updateDriverLocationUseCase, firebaseAuth, locationClient)
        advanceUntilIdle()

        viewModel.acceptRequest("req-1")
        advanceUntilIdle()

        coVerify(exactly = 1) { bookingRepository.respondBookingRequest("req-1", accept = true) }
        coVerify(exactly = 1) { updateDriverLocationUseCase("req-1", any(), any()) }
        assertEquals(0, viewModel.uiState.value.requests.size)
        assertEquals("Permintaan penumpang diterima!", viewModel.uiState.value.actionMessage)
    }

    @Test
    fun `rejectRequest calls repository with accept false`() = runTest {
        val viewModel = DriverRequestsViewModel(bookingRepository, updateDriverLocationUseCase, firebaseAuth, locationClient)
        advanceUntilIdle()

        viewModel.rejectRequest("req-1")
        advanceUntilIdle()

        coVerify(exactly = 1) { bookingRepository.respondBookingRequest("req-1", accept = false) }
        assertEquals(0, viewModel.uiState.value.requests.size)
    }
}
