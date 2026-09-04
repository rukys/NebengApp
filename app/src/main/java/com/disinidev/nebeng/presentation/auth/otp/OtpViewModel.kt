package com.disinidev.nebeng.presentation.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpUiState(
    val phoneNumber: String = "+62 812-3456-7890",
    val otpCode: String = "",
    val countdownSeconds: Int = 60,
    val canResend: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        val phone = savedStateHandle.get<String>("phoneNumber")
        if (!phone.isNullOrBlank()) {
            _uiState.update { it.copy(phoneNumber = phone) }
        }
        startCountdown()
    }

    fun setPhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onOtpChange(code: String) {
        _uiState.update { it.copy(otpCode = code, errorMessage = null) }
        if (code.length == 6) {
            verifyOtp()
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (state.otpCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "Masukkan 6 digit kode keamanan") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Simulasi verifikasi OTP / Firebase Phone Auth
                delay(1000)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Kode verifikasi salah atau telah kadaluarsa"
                    )
                }
            }
        }
    }

    fun resendOtp(viaSms: Boolean = false) {
        if (!_uiState.value.canResend && _uiState.value.countdownSeconds > 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(800)
            _uiState.update { it.copy(isLoading = false) }
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(countdownSeconds = 60, canResend = false) }
        countdownJob = viewModelScope.launch {
            for (sec in 59 downTo 0) {
                delay(1000)
                _uiState.update {
                    it.copy(
                        countdownSeconds = sec,
                        canResend = sec == 0
                    )
                }
            }
        }
    }
}
