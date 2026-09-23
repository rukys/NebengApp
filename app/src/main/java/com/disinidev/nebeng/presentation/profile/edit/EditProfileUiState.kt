package com.disinidev.nebeng.presentation.profile.edit

data class EditProfileUiState(
    val fullName: String = "Budi Santoso",
    val whatsappNumber: String = "+62 812-3456-7890",
    val email: String = "budi.santoso@email.com",
    val officeBuilding: String = "PT Telkom Indonesia • SCBD Lot 8",
    val bio: String = "Komuter harian Tebet - SCBD. Suka obrolan santai, non-smoker, on-time.",
    val avatarInitials: String = "BS",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null
)
