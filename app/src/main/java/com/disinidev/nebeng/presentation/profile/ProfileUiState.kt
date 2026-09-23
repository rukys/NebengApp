package com.disinidev.nebeng.presentation.profile

enum class ProfileRole {
    PASSENGER,
    DRIVER
}

data class ProfileUiState(
    val fullName: String = "Budi Santoso",
    val phoneNumber: String = "+62 812-3456-7890",
    val email: String = "budi.santoso@email.com",
    val avatarUrl: String? = null,
    val avatarInitials: String = "B",
    val selectedRole: ProfileRole = ProfileRole.PASSENGER,
    val rating: Float = 4.9f,
    val tripCount: Int = 15,
    val co2SavedKg: Int = 42,
    val isLoading: Boolean = false,
    val message: String? = null
)
