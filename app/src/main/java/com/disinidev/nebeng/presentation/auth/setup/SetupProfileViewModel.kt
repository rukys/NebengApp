package com.disinidev.nebeng.presentation.auth.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class SetupProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val workplace: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
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

    fun uploadAvatar(imageBytes: ByteArray) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = userRepository.uploadAvatar(imageBytes)
            result.fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(avatarUri = url, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Gagal mengunggah foto") }
                }
            )
        }
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
                val result = userRepository.setupUserProfile(
                    fullName = state.fullName,
                    phoneNumber = state.phoneNumber,
                    email = state.email,
                    officeAddress = state.workplace,
                    bio = state.bio,
                    avatarUrl = state.avatarUri
                )
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.localizedMessage ?: "Gagal menyimpan profil"
                            )
                        }
                    }
                )
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
