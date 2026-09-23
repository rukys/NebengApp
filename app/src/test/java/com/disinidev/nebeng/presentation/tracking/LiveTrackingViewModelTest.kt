package com.disinidev.nebeng.presentation.tracking

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveTrackingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state for car booking loads driver and car info`() {
        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "booking_ride_1"))
        val viewModel = LiveTrackingViewModel(savedStateHandle)

        val state = viewModel.uiState.value
        assertEquals("Andi P.", state.driverName)
        assertEquals("Avanza Silver", state.vehicleModel)
        assertEquals("B 1234 ABC", state.vehiclePlate)
        assertEquals(VehicleType.CAR, state.vehicleType)
        assertEquals("489 201", state.bookingPin)
        assertEquals(3, state.etaMinutes)
        assertEquals(450, state.distanceMeters)
        assertFalse(state.isEmergencyDialogOpen)
    }

    @Test
    fun `initial state for motor booking loads motorcycle info`() {
        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "booking_ride_2"))
        val viewModel = LiveTrackingViewModel(savedStateHandle)

        val state = viewModel.uiState.value
        assertEquals("Reza H.", state.driverName)
        assertEquals(VehicleType.MOTORCYCLE, state.vehicleType)
        assertEquals("Yamaha NMAX Hitam", state.vehicleModel)
        assertEquals("215 889", state.bookingPin)
    }

    @Test
    fun `showEmergencyDialog updates state`() {
        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "booking_ride_1"))
        val viewModel = LiveTrackingViewModel(savedStateHandle)

        viewModel.showEmergencyDialog(true)
        assertTrue(viewModel.uiState.value.isEmergencyDialogOpen)

        viewModel.showEmergencyDialog(false)
        assertFalse(viewModel.uiState.value.isEmergencyDialogOpen)
    }
}
