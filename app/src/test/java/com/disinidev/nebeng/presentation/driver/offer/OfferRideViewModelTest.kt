package com.disinidev.nebeng.presentation.driver.offer

import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.repository.VehicleRepository
import com.disinidev.nebeng.domain.usecase.CreateRideUseCase
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
class OfferRideViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createRideUseCase = mockk<CreateRideUseCase>()
    private val locationSearchRepository = mockk<LocationSearchRepository>(relaxed = true)
    private val vehicleRepository = mockk<VehicleRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: OfferRideViewModel

    private val sampleVehicle = VehicleInfo(
        id = "v_1",
        brand = "Honda",
        model = "HR-V",
        plate = "B 9999 XYZ",
        type = VehicleType.CAR,
        color = "Hitam",
        year = 2023,
        isVerified = true
    )

    private val sampleMotorcycle = VehicleInfo(
        id = "v_2",
        brand = "Yamaha",
        model = "NMAX Hitam",
        plate = "B 8888 ABC",
        type = VehicleType.MOTORCYCLE,
        color = "Hitam",
        year = 2022,
        isVerified = true
    )

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "driver_123"
        coEvery { vehicleRepository.getDriverVehicles("driver_123") } returns Result.success(listOf(sampleVehicle, sampleMotorcycle))
        viewModel = OfferRideViewModel(createRideUseCase, locationSearchRepository, vehicleRepository, userRepository)
    }

    @Test
    fun `initial state loads saved vehicle and sets defaults`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("car", state.vehicleType)
        assertEquals("Honda HR-V", state.vehicleModel)
        assertEquals("B 9999 XYZ", state.vehiclePlate)
        assertEquals("v_1", state.selectedVehicleId)
    }

    @Test
    fun `onVehicleTypeChange to motorcycle sets seats to 1`() = runTest {
        advanceUntilIdle()
        viewModel.onVehicleTypeChange("motorcycle")
        val state = viewModel.uiState.value
        assertEquals("motorcycle", state.vehicleType)
        assertEquals(1, state.availableSeats)
        assertEquals("Yamaha NMAX Hitam", state.vehicleModel)
    }

    @Test
    fun `incrementSeats and decrementSeats change seat count`() {
        viewModel.decrementSeats()
        assertEquals(2, viewModel.uiState.value.availableSeats)

        viewModel.incrementSeats()
        assertEquals(3, viewModel.uiState.value.availableSeats)
    }

    @Test
    fun `publishRide success triggers callback`() = runTest {
        coEvery { createRideUseCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success("ride-123")

        var successCalled = false
        viewModel.onPickupChange("Tebet")
        viewModel.onDropoffChange("SCBD")
        viewModel.publishRide {
            successCalled = true
        }

        advanceUntilIdle()

        assertTrue(successCalled)
        assertTrue(viewModel.uiState.value.isSuccess)
    }
}
