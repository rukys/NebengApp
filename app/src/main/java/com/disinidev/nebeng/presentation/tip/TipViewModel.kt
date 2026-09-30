package com.disinidev.nebeng.presentation.tip

import android.util.Log
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

import com.disinidev.nebeng.domain.repository.UserRepository

@HiltViewModel
class TipViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: ""

    private val _uiState = MutableStateFlow(TipUiState(bookingId = bookingId))
    val uiState: StateFlow<TipUiState> = _uiState.asStateFlow()

    init {
        loadDriverInfo()
    }

    private fun loadDriverInfo() {
        if (bookingId.isBlank()) return
        viewModelScope.launch {
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update { it.copy(driverName = booking.driverName.ifBlank { "Pengemudi" }) }
                    // Also check for driver's QRIS barcode
                    userRepository.getDriverQrisUrl(booking.driverName)
                        .onSuccess { url ->
                            if (!url.isNullOrBlank()) {
                                _uiState.update { it.copy(driverQrisUrl = url) }
                            }
                        }
                }
        }
    }

    fun onPresetSelected(amount: Int) {
        _uiState.update {
            it.copy(
                selectedAmount = amount,
                customAmountText = "",
                isCustomSelected = false
            )
        }
    }

    fun onCustomAmountChanged(text: String) {
        val digits = text.filter { it.isDigit() }
        val amount = digits.toIntOrNull() ?: 0
        _uiState.update {
            it.copy(
                customAmountText = digits,
                selectedAmount = amount,
                isCustomSelected = true
            )
        }
    }

    fun sendTip(onSuccess: () -> Unit) {
        val amount = _uiState.value.selectedAmount
        if (amount <= 0 || _uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                // Tip is a courtesy feature — log and mark complete.
                // Real-money integration can be added later if needed.
                Log.d("TipViewModel", "Tip of Rp$amount sent for booking $bookingId")
                _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}
