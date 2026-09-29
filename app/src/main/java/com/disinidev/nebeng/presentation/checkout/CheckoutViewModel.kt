package com.disinidev.nebeng.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.domain.usecase.CreateBookingUseCase
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createBookingUseCase: CreateBookingUseCase,
    private val rideRepository: RideRepository
) : ViewModel() {

    private val rideId: String = savedStateHandle.get<String>("rideId") ?: ""

    private val _uiState = MutableStateFlow(
        CheckoutUiState(
            isLoading = true
        )
    )
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadRideDetails()
    }

    private fun loadRideDetails() {
        if (rideId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "ID Tebengan tidak valid") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rideRepository.getRideById(rideId)
                .onSuccess { ride ->
                    val defaultSeat = if (ride?.vehicleType == VehicleType.MOTORCYCLE) "pillion" else "tengah_kiri"
                    _uiState.update { it.copy(ride = ride, selectedSeat = defaultSeat, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal memuat detail tebengan. Periksa koneksi internet."
                        )
                    }
                }
        }
    }

    fun selectSeat(seatId: String) {
        _uiState.update { it.copy(selectedSeat = seatId) }
    }

    fun selectHelmetOption(option: HelmetOption) {
        _uiState.update { it.copy(helmetOption = option) }
    }

    fun confirmBooking(
        onSuccess: (bookingId: String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val seat = _uiState.value.selectedSeat
            val result = createBookingUseCase(rideId, seat)
            result.fold(
                onSuccess = { booking ->
                    _uiState.update { it.copy(isLoading = false, isConfirmed = true) }
                    onSuccess(booking.bookingId)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Gagal memesan tebengan. Terjadi gangguan koneksi. Coba lagi dalam beberapa saat."
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    onError(msg)
                }
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
