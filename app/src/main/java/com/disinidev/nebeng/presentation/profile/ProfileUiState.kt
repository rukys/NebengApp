package com.disinidev.nebeng.presentation.profile

import com.disinidev.nebeng.domain.model.VehicleInfo

enum class ProfileRole {
    PASSENGER,
    DRIVER
}

data class ProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val avatarInitials: String = "",
    val selectedRole: ProfileRole = ProfileRole.PASSENGER,
    val rating: Float = 5.0f,
    val tripCount: Int = 0,
    val co2SavedKg: Int = 0,
    val isLoading: Boolean = false,
    val message: String? = null,
    val vehicles: List<VehicleInfo> = emptyList(),
    val isAddingVehicle: Boolean = false
)
