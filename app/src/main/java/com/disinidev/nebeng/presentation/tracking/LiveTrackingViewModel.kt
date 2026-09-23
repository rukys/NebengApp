package com.disinidev.nebeng.presentation.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.disinidev.nebeng.domain.model.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: "booking_ride_1"

    private val _uiState = MutableStateFlow(createInitialState(bookingId))
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    fun showEmergencyDialog(show: Boolean) {
        _uiState.update { it.copy(isEmergencyDialogOpen = show) }
    }

    private fun createInitialState(id: String): LiveTrackingUiState {
        return if (id.contains("ride_2")) {
            LiveTrackingUiState(
                bookingId = id,
                driverName = "Reza H.",
                vehicleModel = "Yamaha NMAX Hitam",
                vehiclePlate = "B 5678 XYZ",
                vehicleType = VehicleType.MOTORCYCLE,
                etaMinutes = 2,
                distanceMeters = 300,
                pickupLocation = "Jemput: Halte Gelora",
                destinationLocation = "SCBD Lot 8 (Tujuan)",
                bookingPin = "215 889",
                statusText = "Driver Sedang Menjemput"
            )
        } else {
            LiveTrackingUiState(
                bookingId = id,
                driverName = "Andi P.",
                vehicleModel = "Avanza Silver",
                vehiclePlate = "B 1234 ABC",
                vehicleType = VehicleType.CAR,
                etaMinutes = 3,
                distanceMeters = 450,
                pickupLocation = "Jemput: Pintu Barat Lawson",
                destinationLocation = "SCBD Lot 8 (Tujuan)",
                bookingPin = "489 201",
                statusText = "Driver Sedang Menjemput"
            )
        }
    }
}
