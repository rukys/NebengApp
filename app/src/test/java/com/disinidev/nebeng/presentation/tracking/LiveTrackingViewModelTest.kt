package com.disinidev.nebeng.presentation.tracking

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.domain.model.TripLocation
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.usecase.ObserveDriverLocationUseCase
import com.disinidev.nebeng.domain.usecase.UpdateDriverLocationUseCase
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
class LiveTrackingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bookingRepository = mockk<BookingRepository>()
    private val observeDriverLocationUseCase = mockk<ObserveDriverLocationUseCase>()
    private val updateDriverLocationUseCase = mockk<UpdateDriverLocationUseCase>()
    private val locationClient = mockk<LocationClient>()

    @Before
    fun setUp() {
        coEvery {
            bookingRepository.getBookingById(any())
        } answers {
            val id = firstArg<String>()
            val isMotor = id.contains("ride_2")
            Result.success(
                BookingResult(
                    bookingId = id,
                    pickupPin = if (isMotor) "215 889" else "489 201",
                    driverName = if (isMotor) "Reza Hendra" else "Andi Pratama",
                    vehicleModel = if (isMotor) "Yamaha NMAX Hitam" else "Toyota Avanza Silver",
                    vehiclePlate = if (isMotor) "B 5678 XYZ" else "B 1234 ABC",
                    seatPosition = if (isMotor) "pillion" else "front_left"
                )
            )
        }
        every { observeDriverLocationUseCase(any()) } returns emptyFlow()
        coEvery { updateDriverLocationUseCase(any(), any(), any()) } returns Result.success(Unit)
        every { locationClient.hasLocationPermission() } returns false
        coEvery { locationClient.getCurrentLocation() } returns null
    }

    private fun createViewModel(bookingId: String): LiveTrackingViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to bookingId))
        return LiveTrackingViewModel(
            savedStateHandle = savedStateHandle,
            bookingRepository = bookingRepository,
            observeDriverLocationUseCase = observeDriverLocationUseCase,
            updateDriverLocationUseCase = updateDriverLocationUseCase,
            locationClient = locationClient
        )
    }

    @Test
    fun `initial state for car booking loads driver and car info and completes simulation`() = runTest {
        val viewModel = createViewModel("booking_ride_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Andi Pratama", state.driverName)
        assertEquals("Toyota Avanza Silver", state.vehicleModel)
        assertEquals("B 1234 ABC", state.vehiclePlate)
        assertEquals(VehicleType.CAR, state.vehicleType)
        assertEquals("489 201", state.bookingPin)
        assertEquals(0, state.etaMinutes)
        assertEquals(0, state.distanceMeters)
        assertEquals(1.0f, state.progress)
        assertTrue(state.isArrived)
        assertFalse(state.isEmergencyDialogOpen)
    }

    @Test
    fun `initial state for motor booking loads motorcycle info`() = runTest {
        val viewModel = createViewModel("booking_ride_2")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Reza Hendra", state.driverName)
        assertEquals(VehicleType.MOTORCYCLE, state.vehicleType)
        assertEquals("Yamaha NMAX Hitam", state.vehicleModel)
        assertEquals("215 889", state.bookingPin)
        assertTrue(state.isArrived)
    }

    @Test
    fun `showEmergencyDialog updates state`() {
        val viewModel = createViewModel("booking_ride_1")

        viewModel.showEmergencyDialog(true)
        assertTrue(viewModel.uiState.value.isEmergencyDialogOpen)

        viewModel.showEmergencyDialog(false)
        assertFalse(viewModel.uiState.value.isEmergencyDialogOpen)
    }

    @Test
    fun `remote driver location updates distance and eta accurately`() = runTest {
        val loc = TripLocation(
            id = "loc-remote",
            bookingId = "booking_ride_1",
            lat = -6.2215, // Same as pickup
            lng = 106.8065,
            updatedAt = Instant.now()
        )
        every { observeDriverLocationUseCase("booking_ride_1") } returns flowOf(loc)

        val viewModel = createViewModel("booking_ride_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.distanceMeters)
        assertEquals(0, state.etaMinutes)
        assertTrue(state.isArrived)
        assertEquals("Driver Telah Tiba di Titik Jemput!", state.statusText)
    }
}
