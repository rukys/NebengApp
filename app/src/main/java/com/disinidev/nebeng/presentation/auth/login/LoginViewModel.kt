package com.disinidev.nebeng.presentation.auth.login

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

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = LoginUiState()
    }

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.identifier.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email atau nomor ponsel tidak boleh kosong") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kata sandi tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithEmail(state.identifier, state.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    val errorMsg = when {
                        e.message?.contains("credential", ignoreCase = true) == true ||
                        e.message?.contains("user-not-found", ignoreCase = true) == true ||
                        e.message?.contains("wrong-password", ignoreCase = true) == true ->
                            "Akun belum terdaftar atau kata sandi salah. Silakan klik 'Daftar sekarang' di bawah."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Koneksi internet bermasalah. Periksa jaringan Anda."
                        else -> e.localizedMessage ?: "Gagal masuk. Periksa email/nomor dan sandi Anda."
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
                    _uiState.update { it.copy(isGoogleLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = e.localizedMessage ?: "Gagal masuk dengan akun Google"
                        )
                    }
                }
            )
        }
    }
}
