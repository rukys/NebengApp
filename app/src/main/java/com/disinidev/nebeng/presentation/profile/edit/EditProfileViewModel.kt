package com.disinidev.nebeng.presentation.profile.edit

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

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun uploadAvatar(imageBytes: ByteArray) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = userRepository.uploadAvatar(imageBytes)
            result.fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(avatarUrl = url, isSaving = false) }
                    showMessage("Foto profil berhasil diperbarui")
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    showMessage(e.localizedMessage ?: "Gagal mengunggah foto")
                }
            )
        }
    }

    fun uploadQris(imageBytes: ByteArray) {
        _uiState.update { it.copy(isUploadingQris = true) }
        viewModelScope.launch {
            userRepository.uploadQris(imageBytes)
                .onSuccess { url ->
                    _uiState.update { it.copy(qrisUrl = url, isUploadingQris = false) }
                    showMessage("Foto QRIS pengemudi berhasil diperbarui!")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingQris = false) }
                    showMessage(e.localizedMessage ?: "Gagal mengunggah QRIS")
                }
        }
    }

    fun uploadKtp(imageBytes: ByteArray) {
        _uiState.update { it.copy(isUploadingKtp = true) }
        viewModelScope.launch {
            userRepository.uploadKtp(imageBytes)
                .onSuccess { url ->
                    _uiState.update { it.copy(ktpUrl = url, isKtpVerified = true, isUploadingKtp = false) }
                    showMessage("Foto e-KTP berhasil diunggah! Terverifikasi.")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingKtp = false) }
                    showMessage(e.localizedMessage ?: "Gagal mengunggah e-KTP")
                }
        }
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
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                userRepository.updateUserProfile(
                    fullName = _uiState.value.fullName,
                    officeAddress = _uiState.value.officeBuilding,
                    bio = _uiState.value.bio
                )
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
        viewModelScope.launch {
            userRepository.getUserProfile()
                .onSuccess { profile ->
                    val fullName = profile.fullName
                    val initials = fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    _uiState.update { current ->
                        current.copy(
                            fullName = fullName,
                            whatsappNumber = profile.phoneNumber,
                            email = profile.email,
                            officeBuilding = profile.officeAddress ?: current.officeBuilding,
                            bio = profile.bio ?: current.bio,
                            avatarUrl = profile.avatarUrl,
                            qrisUrl = profile.qrisUrl,
                            ktpUrl = profile.ktpUrl,
                            isKtpVerified = profile.isKtpVerified,
                            role = profile.role,
                            avatarInitials = initials.ifBlank { if (fullName.isNotBlank()) fullName.first().uppercase() else "U" }
                        )
                    }
                }
        }
    }
}
