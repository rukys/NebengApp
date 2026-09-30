package com.disinidev.nebeng.presentation.routine

import com.disinidev.nebeng.domain.model.RoutineCommute

data class RoutineCommuteUiState(
    val isLoading: Boolean = false,
    val routines: List<RoutineCommute> = emptyList(),
    val isAddSheetVisible: Boolean = false,
    val originInput: String = "",
    val destinationInput: String = "",
    val departureTimeInput: String = "07:30",
    val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val selectedVehicleType: String = "car",
    val autoBook: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
