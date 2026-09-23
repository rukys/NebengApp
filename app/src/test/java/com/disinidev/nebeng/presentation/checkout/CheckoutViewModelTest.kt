package com.disinidev.nebeng.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state for car loads ride and default seat`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle)

        val state = viewModel.uiState.value
        assertNotNull(state.ride)
        assertEquals("Andi Pratama", state.ride?.driverName)
        assertEquals(VehicleType.CAR, state.ride?.vehicleType)
        assertEquals("tengah_kiri", state.selectedSeat)
    }

    @Test
    fun `selectSeat updates selected seat`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle)

        viewModel.selectSeat("depan_kiri")
        assertEquals("depan_kiri", viewModel.uiState.value.selectedSeat)

        viewModel.selectSeat("tengah_kanan")
        assertEquals("tengah_kanan", viewModel.uiState.value.selectedSeat)
    }

    @Test
    fun `initial state for motor loads motorcycle ride and default helmet option`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_2"))
        val viewModel = CheckoutViewModel(savedStateHandle)

        val state = viewModel.uiState.value
        assertNotNull(state.ride)
        assertEquals("Reza Hendra", state.ride?.driverName)
        assertEquals(VehicleType.MOTORCYCLE, state.ride?.vehicleType)
        assertEquals(HelmetOption.DRIVER_HELMET, state.helmetOption)
    }

    @Test
    fun `selectHelmetOption updates helmet option`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_2"))
        val viewModel = CheckoutViewModel(savedStateHandle)

        viewModel.selectHelmetOption(HelmetOption.BRING_OWN)
        assertEquals(HelmetOption.BRING_OWN, viewModel.uiState.value.helmetOption)

        viewModel.selectHelmetOption(HelmetOption.DRIVER_HELMET)
        assertEquals(HelmetOption.DRIVER_HELMET, viewModel.uiState.value.helmetOption)
    }

    @Test
    fun `confirmBooking sets isConfirmed to true`() {
        val savedStateHandle = SavedStateHandle(mapOf("rideId" to "ride_1"))
        val viewModel = CheckoutViewModel(savedStateHandle)

        viewModel.confirmBooking()
        assertTrue(viewModel.uiState.value.isConfirmed)
    }
}
