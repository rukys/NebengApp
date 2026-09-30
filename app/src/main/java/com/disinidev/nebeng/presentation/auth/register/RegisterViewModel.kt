package com.disinidev.nebeng.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val termsAgreed: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isGoogleSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onTermsAgreedChange(value: Boolean) {
        _uiState.update { it.copy(termsAgreed = value, errorMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama lengkap harus diisi") }
            return
        }
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid") }
            return
        }
        if (state.phoneNumber.isBlank() || state.phoneNumber.length < 9) {
            _uiState.update { it.copy(errorMessage = "Nomor WhatsApp tidak valid") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Kata sandi minimal 8 karakter") }
            return
        }
        if (!state.termsAgreed) {
            _uiState.update { it.copy(errorMessage = "Anda harus menyetujui Syarat Layanan & Kebijakan Privasi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val formattedPhone = formatIndonesianPhone(state.phoneNumber)
            val cleanEmail = state.email.trim()

            val result = authRepository.registerWithEmail(
                email = cleanEmail,
                password = state.password,
                fullName = state.fullName.trim(),
                phoneNumber = formattedPhone
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phoneNumber = formattedPhone,
                            isSuccess = true
                        )
                    }
                },
                onFailure = { e ->
                    val errorMsg = when {
                        e.message?.contains("email-already-in-use", ignoreCase = true) == true ||
                        e.message?.contains("already in use", ignoreCase = true) == true ->
                            "Email sudah terdaftar. Silakan masuk atau gunakan email lain."
                        e.message?.contains("weak-password", ignoreCase = true) == true ->
                            "Kata sandi terlalu lemah. Gunakan minimal 8 karakter."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Koneksi internet bermasalah. Periksa jaringan Anda."
                        else -> e.localizedMessage ?: "Pendaftaran gagal. Silakan coba lagi."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    private fun formatIndonesianPhone(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return when {
            cleaned.startsWith("62") -> "+$cleaned"
            cleaned.startsWith("0") -> "+62${cleaned.substring(1)}"
            else -> "+62$cleaned"
        }
    }

    fun setGoogleLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isGoogleLoading = isLoading, errorMessage = null) }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isGoogleLoading = false, errorMessage = message) }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isGoogleLoading = false, isGoogleSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = e.localizedMessage ?: "Gagal mendaftar dengan Google"
                        )
                    }
                }
            )
        }
    }
}
