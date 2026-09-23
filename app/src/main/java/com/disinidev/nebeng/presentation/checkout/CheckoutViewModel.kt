package com.disinidev.nebeng.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class HelmetOption {
    DRIVER_HELMET,
    BRING_OWN
}

data class CheckoutUiState(
    val ride: RideItemUi? = null,
    val selectedSeat: String = "tengah_kiri",
    val helmetOption: HelmetOption = HelmetOption.DRIVER_HELMET,
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rideId: String = savedStateHandle.get<String>("rideId") ?: "ride_1"

    private val _uiState = MutableStateFlow(
        CheckoutUiState(
            ride = getRideById(rideId)
        )
    )
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun selectSeat(seatId: String) {
        _uiState.update { it.copy(selectedSeat = seatId) }
    }

    fun selectHelmetOption(option: HelmetOption) {
        _uiState.update { it.copy(helmetOption = option) }
    }

    fun confirmBooking() {
        _uiState.update { it.copy(isConfirmed = true) }
    }

    private fun getRideById(id: String): RideItemUi {
        val mockRides = listOf(
            RideItemUi(
                id = "ride_1",
                driverName = "Andi Pratama",
                driverGender = DriverGender.MALE,
                vehicleModel = "Toyota Avanza Silver • B 1234 ABC",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "07:30 WIB",
                arrivalTimeFormatted = "07:55",
                availableSeats = 2,
                availableSeatsText = "Sisa 2 kursi",
                facilities = listOf("Sisa 2 kursi", "AC Dingin", "Non-Smoking"),
                driverRating = 4.9,
                totalTrips = 120
            ),
            RideItemUi(
                id = "ride_2",
                driverName = "Reza Hendra",
                driverGender = DriverGender.MALE,
                vehicleModel = "Yamaha NMAX Hitam • B 5678 XYZ",
                vehicleType = VehicleType.MOTORCYCLE,
                departureTimeFormatted = "07:45 WIB",
                arrivalTimeFormatted = "08:05",
                availableSeats = 1,
                availableSeatsText = "1 slot",
                facilities = listOf("1 slot", "Helm SNI & Jas Hujan"),
                driverRating = 4.8,
                totalTrips = 85
            ),
            RideItemUi(
                id = "ride_3",
                driverName = "Bambang S.",
                driverGender = DriverGender.MALE,
                vehicleModel = "Toyota Innova Hitam • B 9981 BCD",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "08:00 WIB",
                arrivalTimeFormatted = "08:25",
                availableSeats = 3,
                availableSeatsText = "Sisa 3 kursi",
                facilities = listOf("Sisa 3 kursi", "Bagasi Luas"),
                driverRating = 4.9,
                totalTrips = 210
            )
        )
        return mockRides.find { it.id == id } ?: mockRides.first()
    }
}
