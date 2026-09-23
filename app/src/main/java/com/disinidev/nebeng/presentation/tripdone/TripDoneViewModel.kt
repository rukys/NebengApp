package com.disinidev.nebeng.presentation.tripdone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDoneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: "booking-001"

    private val _uiState = MutableStateFlow(TripDoneUiState(bookingId = bookingId))
    val uiState: StateFlow<TripDoneUiState> = _uiState.asStateFlow()

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
                // Future enhancement: persist rating & review into Supabase bookings table
                _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }
}
