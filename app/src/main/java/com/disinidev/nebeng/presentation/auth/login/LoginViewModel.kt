package com.disinidev.nebeng.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
            try {
                val email = if (state.identifier.contains("@")) {
                    state.identifier.trim()
                } else {
                    "${state.identifier.trim()}@nebeng.id"
                }

                firebaseAuth.signInWithEmailAndPassword(email, state.password).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
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
        }
    }
}
