package com.disinidev.nebeng.presentation.driver.offer

import com.disinidev.nebeng.domain.repository.LocationSearchRepository
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
    private lateinit var viewModel: OfferRideViewModel

    @Before
    fun setUp() {
        viewModel = OfferRideViewModel(createRideUseCase, locationSearchRepository)
    }

    @Test
    fun `initial state has defaults`() {
        val state = viewModel.uiState.value
        assertEquals("car", state.vehicleType)
        assertEquals(3, state.availableSeats)
        assertEquals("Toyota Avanza Silver", state.vehicleModel)
        assertEquals("B 1234 ABC", state.vehiclePlate)
    }

    @Test
    fun `onVehicleTypeChange to motorcycle sets seats to 1`() {
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
