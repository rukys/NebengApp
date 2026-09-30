package com.disinidev.nebeng.presentation.settings

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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
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
                userRepository.logout()
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
                            email = profile.email,
                            avatarInitials = initials.ifBlank { if (fullName.isNotBlank()) fullName.first().uppercase() else "U" },
                            isVerified = true,
                            isDocumentVerified = profile.isKtpVerified
                        )
                    }
                }
        }
    }
}
