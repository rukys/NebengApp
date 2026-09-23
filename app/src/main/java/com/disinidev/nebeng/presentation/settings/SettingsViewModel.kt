package com.disinidev.nebeng.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@Serializable
private data class UserSettingsDto(
    val id: String? = null,
    val full_name: String? = null,
    val email: String? = null,
    val ktp_verified: Boolean? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                _uiState.update { it.copy(showLogoutDialog = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Gagal keluar: ${e.message}") }
            }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun toggleNotification(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationEnabled = enabled) }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            viewModelScope.launch {
                try {
                    val profile = supabaseClient.postgrest["users"]
                        .select {
                            filter {
                                eq("firebase_uid", uid)
                            }
                        }
                        .decodeSingleOrNull<UserSettingsDto>()

                    if (profile != null) {
                        val fullName = profile.full_name ?: currentUser.displayName ?: "Budi Santoso"
                        val initials = fullName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()

                        _uiState.update { current ->
                            current.copy(
                                fullName = fullName,
                                email = profile.email ?: currentUser.email ?: current.email,
                                avatarInitials = if (initials.isNotBlank()) initials else "BS",
                                isVerified = true,
                                isDocumentVerified = profile.ktp_verified ?: true
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Fallback to default Budi Santoso
                }
            }
        }
    }
}
