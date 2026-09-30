package com.disinidev.nebeng.presentation.driver.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.usecase.UpdateDriverLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverRequestsUiState(
    val requests: List<DriverBookingRequest> = emptyList(),
    val isLoading: Boolean = false,
    val actionMessage: String? = null
)

@HiltViewModel
class DriverRequestsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val updateDriverLocationUseCase: UpdateDriverLocationUseCase,
    private val userRepository: UserRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverRequestsUiState())
    val uiState: StateFlow<DriverRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val driverId = userRepository.getCurrentUserUuid()
            val result = bookingRepository.getPendingRequests(driverId)
            result.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(requests = list, isLoading = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    fun acceptRequest(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.respondBookingRequest(bookingId, accept = true)
                .onSuccess {
                    // Initialize driver location entry in Supabase trip_locations using real GPS if available
                    val loc = if (locationClient.hasLocationPermission()) locationClient.getCurrentLocation() else null
                    val lat = loc?.latitude ?: -6.2245
                    val lng = loc?.longitude ?: 106.8048
                    updateDriverLocationUseCase(bookingId, lat, lng)

                    _uiState.update { state ->
                        state.copy(
                            requests = state.requests.filter { it.bookingId != bookingId },
                            actionMessage = "Permintaan tebengan diterima."
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            actionMessage = "Gagal menerima permintaan tebengan. Terjadi gangguan koneksi. Coba lagi dalam beberapa saat."
                        )
                    }
                }
        }
    }

    fun rejectRequest(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.respondBookingRequest(bookingId, accept = false)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            requests = state.requests.filter { it.bookingId != bookingId },
                            actionMessage = "Permintaan tebengan ditolak."
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            actionMessage = "Gagal menolak permintaan tebengan. Terjadi gangguan koneksi. Coba lagi dalam beberapa saat."
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
