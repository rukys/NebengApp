package com.disinidev.nebeng.presentation.tripdone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDoneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _uiState = MutableStateFlow(TripDoneUiState(bookingId = bookingId))
    val uiState: StateFlow<TripDoneUiState> = _uiState.asStateFlow()

    init {
        loadBookingDetails()
    }

    private fun loadBookingDetails() {
        if (bookingId.isBlank()) return
        viewModelScope.launch {
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update {
                        it.copy(
                            driverName = booking.driverName,
                            vehicleModel = booking.vehicleModel,
                            licensePlate = booking.vehiclePlate
                        )
                    }
                }
        }
    }

    fun onRatingChanged(newRating: Int) {
        _uiState.update { it.copy(rating = newRating) }
    }

    fun onReviewTextChanged(text: String) {
        _uiState.update { it.copy(reviewText = text) }
    }

    fun submitRating(onSuccess: () -> Unit) {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                bookingRepository.rateTrip(
                    bookingId = bookingId,
                    rating = _uiState.value.rating,
                    review = _uiState.value.reviewText
                )
                _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }
}
