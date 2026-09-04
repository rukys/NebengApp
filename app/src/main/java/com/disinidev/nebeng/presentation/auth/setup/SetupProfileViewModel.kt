package com.disinidev.nebeng.presentation.auth.setup

import androidx.lifecycle.SavedStateHandle
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

data class SetupProfileUiState(
    val fullName: String = "Budi Santoso",
    val phoneNumber: String = "+62 812-3456-7890",
    val email: String = "budi.santoso@email.com",
    val workplace: String = "PT Telkom Indonesia • SCBD Lot 8",
    val bio: String = "Komuter harian Tebet - SCBD. Suka obrolan santai, non-smoker, on-time.",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@Serializable
private data class ProfileUpsertDto(
    val firebase_uid: String,
    val full_name: String,
    val phone_number: String,
    val email: String? = null,
    val office_address: String? = null,
    val bio: String? = null
)

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firebaseAuth: FirebaseAuth,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupProfileUiState())
    val uiState: StateFlow<SetupProfileUiState> = _uiState.asStateFlow()

    init {
        val phone = savedStateHandle.get<String>("phoneNumber")
        val name = savedStateHandle.get<String>("fullName")
        val email = savedStateHandle.get<String>("email")

        _uiState.update {
            it.copy(
                phoneNumber = if (!phone.isNullOrBlank()) phone else it.phoneNumber,
                fullName = if (!name.isNullOrBlank()) name else it.fullName,
                email = if (!email.isNullOrBlank()) email else it.email
            )
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onWorkplaceChange(value: String) {
        _uiState.update { it.copy(workplace = value, errorMessage = null) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value, errorMessage = null) }
    }

    fun onAvatarSelected(uri: String) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama lengkap harus diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val userId = firebaseAuth.currentUser?.uid

                if (userId != null) {
                    try {
                        supabaseClient.postgrest["users"].upsert(
                            ProfileUpsertDto(
                                firebase_uid = userId,
                                full_name = state.fullName,
                                phone_number = state.phoneNumber,
                                email = state.email.ifBlank { null },
                                office_address = state.workplace.ifBlank { null },
                                bio = state.bio.ifBlank { null }
                            )
                        ) {
                            onConflict = "firebase_uid"
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SetupProfile", "Supabase upsert error: ${e.message}", e)
                    }
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Gagal menyimpan profil"
                    )
                }
            }
        }
    }
}
