package com.disinidev.nebeng.presentation.vehicle

import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType

data class VehicleManagementUiState(
    val vehicles: List<VehicleInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    // Add form fields
    val formBrand: String = "",
    val formModel: String = "",
    val formPlate: String = "",
    val formColor: String = "",
    val formYear: String = "",
    val formType: VehicleType = VehicleType.CAR,
    // Inline validation
    val formError: String? = null
)
