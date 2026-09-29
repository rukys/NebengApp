package com.disinidev.nebeng.presentation.profile.edit

data class EditProfileUiState(
    val fullName: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val officeBuilding: String = "",
    val bio: String = "",
    val avatarInitials: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null
)
