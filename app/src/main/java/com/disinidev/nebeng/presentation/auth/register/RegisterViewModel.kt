package com.disinidev.nebeng.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
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

@Serializable
private data class UserRegisterDto(
    val firebase_uid: String,
    val full_name: String,
    val phone_number: String,
    val email: String,
    val whatsapp_number: String
)

@Serializable
private data class GoogleUserRegisterDto(
    val firebase_uid: String,
    val full_name: String,
    val email: String,
    val phone_number: String? = null,
    val whatsapp_number: String? = null,
    val avatar_url: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
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
            try {
                val formattedPhone = formatIndonesianPhone(state.phoneNumber)
                val cleanEmail = state.email.trim()

                // 1. Create user in Firebase Auth
                val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, state.password).await()
                val uid = authResult.user?.uid ?: throw IllegalStateException("Gagal mendapatkan User ID Firebase")

                // 2. Insert initial user record in Supabase
                try {
                    supabaseClient.postgrest["users"].insert(
                        UserRegisterDto(
                            firebase_uid = uid,
                            full_name = state.fullName.trim(),
                            phone_number = formattedPhone,
                            email = cleanEmail,
                            whatsapp_number = formattedPhone
                        )
                    )
                } catch (e: Exception) {
                    // Fallback jika Supabase insert error offline/RLS
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        phoneNumber = formattedPhone,
                        isSuccess = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
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
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    syncGoogleUser(firebaseUser)
                }
                _uiState.update { it.copy(isGoogleLoading = false, isGoogleSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGoogleLoading = false,
                        errorMessage = e.localizedMessage ?: "Gagal mendaftar dengan Google"
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
                .decodeSingleOrNull<GoogleUserRegisterDto>()

            if (existing == null) {
                val phone = user.phoneNumber?.ifBlank { null }
                supabaseClient.postgrest["users"].insert(
                    GoogleUserRegisterDto(
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
            android.util.Log.e("RegisterViewModel", "Supabase sync error: ${e.message}", e)
        }
    }
}
