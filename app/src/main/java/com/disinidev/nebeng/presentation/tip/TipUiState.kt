package com.disinidev.nebeng.presentation.tip

data class TipUiState(
    val bookingId: String = "",
    val driverName: String = "Pengemudi",
    val selectedAmount: Int = 0,
    val customAmountText: String = "",
    val isCustomSelected: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)
