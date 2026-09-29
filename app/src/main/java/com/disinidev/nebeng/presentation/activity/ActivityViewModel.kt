package com.disinidev.nebeng.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    fun onFilterSelected(filter: ActivityFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearch(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery
            )
        }
    }

    fun refresh() {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userUuid = userRepository.getCurrentUserUuid()
                bookingRepository.getUserActivities(userUuid)
                    .onSuccess { activities ->
                        val activeTripModel = activities.activeTrip?.let { item ->
                            val statusTitle = when (item.status) {
                                "pending" -> "MENUNGGU KONFIRMASI"
                                "confirmed" -> "PENJEMPUTAN SEGERA"
                                "picked_up" -> "DALAM PERJALANAN"
                                else -> "AKTIF"
                            }
                            ActiveTrip(
                                bookingId = item.id,
                                statusText = statusTitle,
                                pin = item.pin,
                                driverName = item.counterpartName,
                                vehicleModel = item.vehicleModel,
                                licensePlate = item.licensePlate,
                                pickupAddress = item.origin,
                                dropoffAddress = item.destination,
                                vehicleType = if (item.vehicleType.equals("motor", ignoreCase = true)) "motorcycle" else "car"
                            )
                        }

                        val completedItems = activities.completedTrips.map { item ->
                            TripHistoryItem(
                                id = item.id,
                                origin = item.origin.split(",").firstOrNull() ?: item.origin,
                                destination = item.destination.split(",").firstOrNull() ?: item.destination,
                                timeText = item.timeText,
                                vehicleType = item.vehicleType,
                                driverName = item.counterpartName,
                                status = "SELESAI"
                            )
                        }

                        val cancelledItems = activities.canceledTrips.map { item ->
                            TripHistoryItem(
                                id = item.id,
                                origin = item.origin.split(",").firstOrNull() ?: item.origin,
                                destination = item.destination.split(",").firstOrNull() ?: item.destination,
                                timeText = item.timeText,
                                vehicleType = item.vehicleType,
                                driverName = item.counterpartName,
                                status = "DIBATALKAN"
                            )
                        }

                        _uiState.update { state ->
                            state.copy(
                                activeTrip = activeTripModel,
                                completedTrips = completedItems,
                                canceledTrips = cancelledItems,
                                isLoading = false
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat aktivitas tebengan. Coba muat ulang halaman.") }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat aktivitas tebengan. Coba muat ulang halaman.") }
            }
        }
    }

    fun cancelActiveTrip(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null, successMessage = null) }
            bookingRepository.cancelBooking(bookingId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            successMessage = "Tebengan berhasil dibatalkan."
                        )
                    }
                    loadTrips()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = "Gagal membatalkan tebengan. Terjadi gangguan koneksi. Coba lagi dalam beberapa saat."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
