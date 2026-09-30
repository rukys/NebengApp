package com.disinidev.nebeng.presentation.profile.edit

data class EditProfileUiState(
    val fullName: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val officeBuilding: String = "",
    val bio: String = "",
    val avatarInitials: String = "",
    val avatarUrl: String? = null,
    val qrisUrl: String? = null,
    val ktpUrl: String? = null,
    val isKtpVerified: Boolean = false,
    val role: String = "both",
    val isSaving: Boolean = false,
    val isUploadingQris: Boolean = false,
    val isUploadingKtp: Boolean = false,
    val message: String? = null
)

