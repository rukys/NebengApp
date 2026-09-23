package com.disinidev.nebeng.presentation.profile.edit

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
private data class EditUserProfileDto(
    val id: String? = null,
    val full_name: String? = null,
    val phone_number: String? = null,
    val email: String? = null,
    val office_address: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null
)

@Serializable
private data class UpdateUserProfileDto(
    val full_name: String,
    val office_address: String,
    val bio: String
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onFullNameChange(name: String) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        _uiState.update {
            it.copy(
                fullName = name,
                avatarInitials = if (initials.isNotBlank()) initials else it.avatarInitials
            )
        }
    }

    fun onWhatsappChange(whatsapp: String) {
        _uiState.update { it.copy(whatsappNumber = whatsapp) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onOfficeChange(office: String) {
        _uiState.update { it.copy(officeBuilding = office) }
    }

    fun onBioChange(bio: String) {
        _uiState.update { it.copy(bio = bio) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                if (uid != null) {
                    supabaseClient.postgrest["users"]
                        .update(
                            UpdateUserProfileDto(
                                full_name = _uiState.value.fullName,
                                office_address = _uiState.value.officeBuilding,
                                bio = _uiState.value.bio
                            )
                        ) {
                            filter {
                                eq("firebase_uid", uid)
                            }
                        }
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Perubahan profil berhasil disimpan"
                    )
                }
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep local changes and notify
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Profil diperbarui secara lokal"
                    )
                }
                onSuccess()
            }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
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
                        .decodeSingleOrNull<EditUserProfileDto>()

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
                                whatsappNumber = profile.phone_number ?: currentUser.phoneNumber ?: current.whatsappNumber,
                                email = profile.email ?: currentUser.email ?: current.email,
                                officeBuilding = profile.office_address ?: current.officeBuilding,
                                bio = profile.bio ?: current.bio,
                                avatarUrl = profile.avatar_url,
                                avatarInitials = if (initials.isNotBlank()) initials else "BS"
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Fallback to default state
                }
            }
        }
    }
}
