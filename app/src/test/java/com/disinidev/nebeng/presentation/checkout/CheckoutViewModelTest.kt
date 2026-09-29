package com.disinidev.nebeng.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.domain.usecase.CreateBookingUseCase
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createBookingUseCase = mockk<CreateBookingUseCase>()
    private val rideRepository = mockk<RideRepository>(relaxed = true)

    private val sampleCarRide = RideItemUi(
        id = "ride_1",
        driverName = "Andi Pratama",
        driverGender = DriverGender.MALE,
        vehicleModel = "Toyota Avanza",
        vehicleType = VehicleType.CAR,
        departureTimeFormatted = "07:30",
        arrivalTimeFormatted = "07:55",
        availableSeats = 2,
        availableSeatsText = "Sisa 2 kursi",
        facilities = listOf("AC"),
        driverRating = 4.9,
        totalTrips = 120
    )

    private val sampleMotorRide = RideItemUi(
        id = "ride_2",
        driverName = "Reza Hendra",
        driverGender = DriverGender.MALE,
        vehicleModel = "Yamaha NMAX",
        vehicleType = VehicleType.MOTORCYCLE,
        departureTimeFormatted = "07:45",
        arrivalTimeFormatted = "08:05",
        availableSeats = 1,
        availableSeatsText = "1 slot",
        facilities = listOf("Helm SNI"),
        driverRating = 4.8,
        totalTrips = 85
    )

    @Before
    fun setUp() {
        coEvery {
            createBookingUseCase(any(), any())
        } answers {
            Result.success(
                BookingResult(
                    bookingId = "booking_confirmed_123",
                    pickupPin = "489 201",
                    driverName = "Andi Pratama",
                    vehicleModel = "Toyota Avanza",
                    vehiclePlate = "B 1234 ABC"
                )
            )
        }
        coEvery { rideRepository.getRideById("ride_1") } returns Result.success(sampleCarRide)
        coEvery { rideRepository.getRideById("ride_2") } returns Result.success(sampleMotorRide)
    }

    @Test
    fun `initial state for car loads ride and default seat`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle, createBookingUseCase, rideRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.ride)
        assertEquals("Andi Pratama", state.ride?.driverName)
        assertEquals(VehicleType.CAR, state.ride?.vehicleType)
        assertEquals("tengah_kiri", state.selectedSeat)
    }

    @Test
    fun `selectSeat updates selected seat`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle, createBookingUseCase, rideRepository)

        viewModel.selectSeat("depan_kiri")
        assertEquals("depan_kiri", viewModel.uiState.value.selectedSeat)

        viewModel.selectSeat("tengah_kanan")
        assertEquals("tengah_kanan", viewModel.uiState.value.selectedSeat)
    }

    @Test
    fun `initial state for motor loads motorcycle ride and default helmet option`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_2"))
        val viewModel = CheckoutViewModel(savedStateHandle, createBookingUseCase, rideRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.ride)
        assertEquals("Reza Hendra", state.ride?.driverName)
        assertEquals(VehicleType.MOTORCYCLE, state.ride?.vehicleType)
        assertEquals(HelmetOption.DRIVER_HELMET, state.helmetOption)
        assertEquals("pillion", state.selectedSeat)
    }

    @Test
    fun `selectHelmetOption updates helmet option`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_2"))
        val viewModel = CheckoutViewModel(savedStateHandle, createBookingUseCase, rideRepository)

        viewModel.selectHelmetOption(HelmetOption.BRING_OWN)
        assertEquals(HelmetOption.BRING_OWN, viewModel.uiState.value.helmetOption)

        viewModel.selectHelmetOption(HelmetOption.DRIVER_HELMET)
        assertEquals(HelmetOption.DRIVER_HELMET, viewModel.uiState.value.helmetOption)
    }

    @Test
    fun `confirmBooking sets isConfirmed to true and invokes callback with bookingId`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle, createBookingUseCase, rideRepository)
        advanceUntilIdle()

        var confirmedBookingId: String? = null
        viewModel.confirmBooking(
            onSuccess = { id ->
                confirmedBookingId = id
            }
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConfirmed)
        assertEquals("booking_confirmed_123", confirmedBookingId)
    }
}
