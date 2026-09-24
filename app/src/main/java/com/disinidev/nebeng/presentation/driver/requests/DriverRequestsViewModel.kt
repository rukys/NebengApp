package com.disinidev.nebeng.presentation.driver.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import com.disinidev.nebeng.domain.usecase.UpdateDriverLocationUseCase
import com.google.firebase.auth.FirebaseAuth
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
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverRequestsUiState())
    val uiState: StateFlow<DriverRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        val driverId = firebaseAuth.currentUser?.uid ?: "driver-001"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
            // Initialize driver location entry in Supabase trip_locations
            updateDriverLocationUseCase(bookingId, -6.2245, 106.8048)

            _uiState.update { state ->
                state.copy(
                    requests = state.requests.filter { it.bookingId != bookingId },
                    actionMessage = "Permintaan penumpang diterima!"
                )
            }
        }
    }

    fun rejectRequest(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.respondBookingRequest(bookingId, accept = false)
            _uiState.update { state ->
                state.copy(
                    requests = state.requests.filter { it.bookingId != bookingId },
                    actionMessage = "Permintaan penumpang ditolak"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
