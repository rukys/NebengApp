package com.disinidev.nebeng.presentation.settings

data class SettingsUiState(
    val fullName: String = "",
    val email: String = "",
    val avatarInitials: String = "",
    val isVerified: Boolean = false,
    val isDocumentVerified: Boolean = false,
    val selectedLanguage: String = "Bahasa Indonesia",
    val isNotificationEnabled: Boolean = true,
    val showLogoutDialog: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)
