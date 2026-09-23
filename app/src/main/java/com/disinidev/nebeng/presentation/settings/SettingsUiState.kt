package com.disinidev.nebeng.presentation.settings

data class SettingsUiState(
    val fullName: String = "Budi Santoso",
    val email: String = "budi.santoso@email.com",
    val avatarInitials: String = "BS",
    val isVerified: Boolean = true,
    val isDocumentVerified: Boolean = true,
    val selectedLanguage: String = "Bahasa Indonesia",
    val isNotificationEnabled: Boolean = true,
    val showLogoutDialog: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)
