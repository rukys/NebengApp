package com.disinidev.nebeng.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import javax.inject.Inject

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@Serializable
private data class GoogleUserUpsertDto(
    val firebase_uid: String,
    val full_name: String,
    val email: String,
    val phone_number: String? = null,
    val whatsapp_number: String? = null,
    val avatar_url: String? = null
)

@Serializable
private data class UserExistCheckDto(
    val firebase_uid: String
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
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

    fun setGoogleLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isGoogleLoading = isLoading, errorMessage = null) }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isGoogleLoading = false, errorMessage = message) }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    syncGoogleUser(firebaseUser)
                }
                _uiState.update { it.copy(isGoogleLoading = false, isSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGoogleLoading = false,
                        errorMessage = e.localizedMessage ?: "Gagal masuk dengan akun Google"
                    )
                }
            }
        }
    }

    private suspend fun syncGoogleUser(user: FirebaseUser) {
        try {
            val existing = supabaseClient.postgrest["users"]
                .select {
                    filter { eq("firebase_uid", user.uid) }
                }
                .decodeSingleOrNull<UserExistCheckDto>()

            if (existing == null) {
                val phone = user.phoneNumber?.ifBlank { null }
                supabaseClient.postgrest["users"].insert(
                    GoogleUserUpsertDto(
                        firebase_uid = user.uid,
                        full_name = user.displayName?.ifBlank { null } ?: "Pengguna Google",
                        email = user.email ?: "",
                        phone_number = phone,
                        whatsapp_number = null,
                        avatar_url = user.photoUrl?.toString()
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "Supabase sync error: ${e.message}", e)
        }
    }
}
